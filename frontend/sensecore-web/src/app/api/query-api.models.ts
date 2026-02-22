export interface DeviceDto {
  deviceId: string;
  status: string;
  createdAt: string;
}

export interface TelemetryReadingDto {
  deviceId: string;
  sensorType: string;
  value: number;
  unit: string;
  timestamp: string;
}
