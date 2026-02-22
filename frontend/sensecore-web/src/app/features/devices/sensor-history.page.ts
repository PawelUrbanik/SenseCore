import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MAT_DATE_LOCALE,
  MatDateFormats,
  MatNativeDateModule
} from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { EChartsCoreOption } from 'echarts/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

import { QueryApiService } from '../../api/query-api.service';
import { TelemetryReadingDto } from '../../api/query-api.models';
import { formatTimestamp, toIsoFromDateAndTime, toLocalTimeInputValue } from '../../shared/time';

const SENSOR_HISTORY_DATE_FORMATS: MatDateFormats = {
  parse: {
    dateInput: { day: '2-digit', month: '2-digit', year: 'numeric' }
  },
  display: {
    dateInput: { day: '2-digit', month: '2-digit', year: 'numeric' },
    monthYearLabel: { month: 'short', year: 'numeric' },
    dateA11yLabel: { day: '2-digit', month: 'long', year: 'numeric' },
    monthYearA11yLabel: { month: 'long', year: 'numeric' }
  }
};

@Component({
  selector: 'app-sensor-history-page',
  templateUrl: './sensor-history.page.html',
  styleUrl: './sensor-history.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    NgxEchartsDirective,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule
  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'en-GB' },
    { provide: MAT_DATE_FORMATS, useValue: SENSOR_HISTORY_DATE_FORMATS }
  ]
})
export class SensorHistoryPage {
  private readonly api = inject(QueryApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dateAdapter = inject<DateAdapter<Date>>(DateAdapter);
  private historySub: Subscription | null = null;
  private refreshTimer: ReturnType<typeof setInterval> | null = null;

  readonly deviceId = toSignal(
    this.route.paramMap.pipe(map(params => params.get('deviceId') ?? '')),
    { initialValue: '' }
  );

  readonly sensorType = toSignal(
    this.route.paramMap.pipe(map(params => params.get('sensorType') ?? '')),
    { initialValue: '' }
  );

  readonly readings = signal<TelemetryReadingDto[]>([]);
  readonly loading = signal(false);
  readonly refreshing = signal(false);
  readonly error = signal<string | null>(null);
  readonly autoRefreshEnabled = signal(false);

  readonly form = new FormGroup({
    fromDate: new FormControl<Date | null>(null, { validators: [Validators.required] }),
    fromTime: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    toDate: new FormControl<Date | null>(null, { validators: [Validators.required] }),
    toTime: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    limit: new FormControl(200, { nonNullable: true, validators: [Validators.min(1), Validators.max(2000)] }),
    refreshSeconds: new FormControl(30, {
      nonNullable: true,
      validators: [Validators.min(5), Validators.max(3600)]
    })
  });

  readonly chartOptions = computed<EChartsCoreOption>(() => {
    const values = this.readings().map(reading => [reading.timestamp, reading.value]);

    return {
      grid: { left: 32, right: 24, top: 24, bottom: 32 },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'time',
        axisLabel: { hideOverlap: true }
      },
      yAxis: {
        type: 'value',
        scale: true
      },
      series: [
        {
          type: 'line',
          smooth: true,
          data: values
        }
      ]
    };
  });

  readonly hasReadings = computed(() => this.readings().length > 0);
  readonly historyColumns = ['timestamp', 'value', 'unit'];

  constructor() {
    this.dateAdapter.setLocale('en-GB');

    const now = new Date();
    const from = new Date(now.getTime() - 24 * 60 * 60 * 1000);
    const to = new Date(now.getTime() + 60 * 60 * 1000);

    this.form.patchValue({
      fromDate: from,
      fromTime: toLocalTimeInputValue(from),
      toDate: to,
      toTime: toLocalTimeInputValue(to)
    });

    this.destroyRef.onDestroy(() => {
      this.historySub?.unsubscribe();
      this.stopAutoRefresh();
    });

    effect(() => {
      if (this.deviceId() && this.sensorType()) {
        this.loadHistory();
      }
    });
  }

  toggleAutoRefresh(): void {
    const enabled = !this.autoRefreshEnabled();
    this.autoRefreshEnabled.set(enabled);

    if (enabled) {
      this.startAutoRefresh();
      if (this.autoRefreshEnabled()) {
        this.form.controls.refreshSeconds.disable({ emitEvent: false });
      }
    } else {
      this.stopAutoRefresh();
      this.form.controls.refreshSeconds.enable({ emitEvent: false });
    }
  }

  startAutoRefresh(): void {
    this.stopAutoRefresh();

    if (this.form.controls.refreshSeconds.invalid) {
      this.form.controls.refreshSeconds.markAsTouched();
      this.autoRefreshEnabled.set(false);
      this.form.controls.refreshSeconds.enable({ emitEvent: false });
      return;
    }

    const intervalSeconds = this.form.controls.refreshSeconds.value;

    this.refreshTimer = setInterval(() => {
      if (!this.loading() && !this.refreshing()) {
        this.loadHistory({ silent: true });
      }
    }, intervalSeconds * 1000);
  }

  stopAutoRefresh(): void {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
      this.refreshTimer = null;
    }
  }

  loadHistory(options?: { silent?: boolean }): void {
    if (!this.deviceId() || !this.sensorType()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const fromIso = toIsoFromDateAndTime(this.form.controls.fromDate.value, this.form.controls.fromTime.value);
    const toIso = toIsoFromDateAndTime(this.form.controls.toDate.value, this.form.controls.toTime.value);

    if (!fromIso || !toIso) {
      this.error.set('Invalid date range');
      return;
    }

    const silent = !!options?.silent && this.hasReadings();

    this.historySub?.unsubscribe();
    this.loading.set(!silent);
    this.refreshing.set(silent);
    this.error.set(null);

    this.historySub = this.api.getHistory(
      this.deviceId(),
      this.sensorType(),
      fromIso,
      toIso,
      this.form.controls.limit.value
    ).subscribe({
      next: readings => {
        this.readings.set(readings);
        this.loading.set(false);
        this.refreshing.set(false);
      },
      error: err => {
        if (err instanceof HttpErrorResponse && err.status === 404) {
          this.readings.set([]);
          this.error.set(null);
          this.loading.set(false);
          this.refreshing.set(false);
          return;
        }

        const message = err?.error?.message || err?.message || 'Request failed';
        this.error.set(message);
        this.loading.set(false);
        this.refreshing.set(false);
      }
    });
  }

  formatRowTimestamp(timestamp: string): string {
    return formatTimestamp(timestamp);
  }
}
