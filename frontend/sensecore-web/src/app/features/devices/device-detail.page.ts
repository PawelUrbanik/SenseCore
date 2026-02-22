import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

import { QueryApiService } from '../../api/query-api.service';
import { DeviceDto } from '../../api/query-api.models';
import { SENSOR_OPTIONS } from '../../shared/sensors';

@Component({
  selector: 'app-device-detail-page',
  templateUrl: './device-detail.page.html',
  styleUrl: './device-detail.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule
  ]
})
export class DeviceDetailPage {
  private readonly api = inject(QueryApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private deviceSub: Subscription | null = null;

  readonly sensorOptions = SENSOR_OPTIONS;

  readonly deviceId = signal('');

  readonly device = signal<DeviceDto | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly sensorTypeControl = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required]
  });

  readonly sensorTypeValue = signal(this.sensorTypeControl.value);

  readonly canNavigate = computed(() => this.sensorTypeValue().trim().length > 0 && !!this.deviceId());

  constructor() {
    const routeSub = this.route.paramMap.subscribe(params => {
      this.deviceId.set(params.get('deviceId') ?? '');
      if (this.deviceId()) {
        this.loadDevice();
      }
    });

    const sensorSub = this.sensorTypeControl.valueChanges.subscribe(value => {
      this.sensorTypeValue.set(value);
    });

    this.destroyRef.onDestroy(() => {
      this.deviceSub?.unsubscribe();
      routeSub.unsubscribe();
      sensorSub.unsubscribe();
    });
  }

  loadDevice(): void {
    this.deviceSub?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);

    this.deviceSub = this.api.getDevices().subscribe({
      next: devices => {
        const current = devices.find(item => item.deviceId === this.deviceId()) ?? null;
        this.device.set(current);
        this.loading.set(false);
      },
      error: err => {
        const message = err?.error?.message || err?.message || 'Request failed';
        this.error.set(message);
        this.loading.set(false);
      }
    });
  }

  openSensor(): void {
    if (!this.canNavigate()) {
      this.sensorTypeControl.markAsTouched();
      return;
    }

    const sensorType = this.sensorTypeControl.value.trim();
    this.router.navigate(['/devices', this.deviceId(), 'sensors', sensorType]);
  }
}
