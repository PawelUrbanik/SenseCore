import { Component, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('SenseCore');
  protected readonly devices = signal<DeviceDto[] | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly lastLoadedAt = signal<string | null>(null);

  constructor(private readonly http: HttpClient) {}

  loadDevices(): void {
    this.loading.set(true);
    this.error.set(null);

    this.http.get<DeviceDto[]>('/api/devices').subscribe({
      next: devices => {
        this.devices.set(devices);
        this.lastLoadedAt.set(new Date().toLocaleString());
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

interface DeviceDto {
  deviceId: string;
  status: string;
  createdAt: string;
}
