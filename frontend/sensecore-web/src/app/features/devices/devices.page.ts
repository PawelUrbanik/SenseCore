import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';

import { QueryApiService } from '../../api/query-api.service';
import { DeviceDto } from '../../api/query-api.models';
import { formatTimestamp } from '../../shared/time';

@Component({
  selector: 'app-devices-page',
  templateUrl: './devices.page.html',
  styleUrl: './devices.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, MatButtonModule, MatCardModule, MatProgressSpinnerModule, MatTableModule]
})
export class DevicesPage {
  private readonly api = inject(QueryApiService);
  private readonly destroyRef = inject(DestroyRef);
  private devicesSub: Subscription | null = null;

  readonly devices = signal<DeviceDto[] | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly lastUpdated = signal<string | null>(null);

  readonly hasDevices = computed(() => (this.devices() ?? []).length > 0);
  readonly lastUpdatedLabel = computed(() => formatTimestamp(this.lastUpdated()));
  readonly displayedColumns = ['deviceId', 'status', 'createdAt'];

  constructor() {
    this.destroyRef.onDestroy(() => this.devicesSub?.unsubscribe());
    this.loadDevices();
  }

  loadDevices(): void {
    this.devicesSub?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);

    this.devicesSub = this.api.getDevices().subscribe({
      next: devices => {
        this.devices.set(devices);
        this.lastUpdated.set(new Date().toISOString());
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
