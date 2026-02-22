import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';

import { QueryApiService } from '../../api/query-api.service';
import { DeviceDto } from '../../api/query-api.models';
import { formatTimestamp } from '../../shared/time';

@Component({
  selector: 'app-devices-page',
  templateUrl: './devices.page.html',
  styleUrl: './devices.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule
  ]
})
export class DevicesPage {
  private readonly api = inject(QueryApiService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private devicesSub: Subscription | null = null;

  readonly devices = signal<DeviceDto[] | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly lastUpdated = signal<string | null>(null);
  readonly nameFilter = signal('');
  readonly statusFilter = signal('');

  readonly hasDevices = computed(() => (this.devices() ?? []).length > 0);
  readonly availableStatuses = computed(() => {
    const statuses = new Set((this.devices() ?? []).map(device => device.status));
    return [...statuses].sort((a, b) => a.localeCompare(b));
  });
  readonly filteredDevices = computed(() => {
    const normalizedName = this.nameFilter().trim().toLowerCase();
    const selectedStatus = this.statusFilter().trim();

    return (this.devices() ?? []).filter(device => {
      const matchesName = !normalizedName || device.deviceId.toLowerCase().includes(normalizedName);
      const matchesStatus = !selectedStatus || device.status === selectedStatus;
      return matchesName && matchesStatus;
    });
  });
  readonly hasFilteredDevices = computed(() => this.filteredDevices().length > 0);
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

  openDeviceDetails(deviceId: string): void {
    void this.router.navigate(['/devices', deviceId]);
  }

  setNameFilter(value: string): void {
    this.nameFilter.set(value);
  }

  setStatusFilter(value: string): void {
    this.statusFilter.set(value);
  }
}
