import { ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

import { QueryApiService } from '../../api/query-api.service';
import { TelemetryReadingDto } from '../../api/query-api.models';
import { formatTimestamp } from '../../shared/time';

@Component({
  selector: 'app-sensor-readings-page',
  templateUrl: './sensor-readings.page.html',
  styleUrl: './sensor-readings.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, MatButtonModule, MatCardModule]
})
export class SensorReadingsPage {
  private readonly api = inject(QueryApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private latestSub: Subscription | null = null;

  readonly deviceId = toSignal(
    this.route.paramMap.pipe(map(params => params.get('deviceId') ?? '')),
    { initialValue: '' }
  );

  readonly sensorType = toSignal(
    this.route.paramMap.pipe(map(params => params.get('sensorType') ?? '')),
    { initialValue: '' }
  );

  readonly latest = signal<TelemetryReadingDto | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly formattedTimestamp = computed(() => formatTimestamp(this.latest()?.timestamp ?? null));

  constructor() {
    this.destroyRef.onDestroy(() => this.latestSub?.unsubscribe());

    effect(() => {
      if (this.deviceId() && this.sensorType()) {
        this.loadLatest();
      }
    });
  }

  loadLatest(): void {
    if (!this.deviceId() || !this.sensorType()) {
      return;
    }

    this.latestSub?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);

    this.latestSub = this.api.getLatest(this.deviceId(), this.sensorType()).subscribe({
      next: reading => {
        this.latest.set(reading);
        this.loading.set(false);
      },
      error: err => {
        const message = err?.error?.message || err?.message || 'Request failed';
        this.error.set(message);
        this.loading.set(false);
      }
    });
  }
}
