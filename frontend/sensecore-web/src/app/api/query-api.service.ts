import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { DeviceDto, TelemetryReadingDto } from './query-api.models';

@Injectable({ providedIn: 'root' })
export class QueryApiService {
  private readonly http = inject(HttpClient);

  getDevices(): Observable<DeviceDto[]> {
    return this.http.get<DeviceDto[]>('/api/devices');
  }

  getLatest(deviceId: string, sensorType: string): Observable<TelemetryReadingDto> {
    const params = new HttpParams()
      .set('deviceId', deviceId)
      .set('sensorType', sensorType);

    return this.http.get<TelemetryReadingDto>('/api/telemetry/latest', { params });
  }

  getHistory(
    deviceId: string,
    sensorType: string,
    fromIso: string,
    toIso: string,
    limit: number
  ): Observable<TelemetryReadingDto[]> {
    const params = new HttpParams()
      .set('deviceId', deviceId)
      .set('sensorType', sensorType)
      .set('from', fromIso)
      .set('to', toIso)
      .set('limit', String(limit));

    return this.http.get<TelemetryReadingDto[]>('/api/telemetry/history', { params });
  }
}
