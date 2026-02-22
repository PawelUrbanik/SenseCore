export interface SensorOption {
  type: string;
  label: string;
  active: boolean;
}

export const SENSOR_OPTIONS: SensorOption[] = [
  { type: 'temperature', label: 'temperature', active: true },
  { type: 'humidity', label: 'humidity', active: false },
  { type: 'pressure', label: 'pressure', active: false },
  { type: 'co2', label: 'co2', active: false }
];
