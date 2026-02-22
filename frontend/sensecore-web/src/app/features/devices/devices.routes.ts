import { Routes } from '@angular/router';

export const DEVICES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./devices.page').then(m => m.DevicesPage)
  },
  {
    path: ':deviceId/sensors/:sensorType',
    children: [
      {
        path: '',
        loadComponent: () => import('./sensor-readings.page').then(m => m.SensorReadingsPage)
      },
      {
        path: 'history',
        loadComponent: () => import('./sensor-history.page').then(m => m.SensorHistoryPage)
      }
    ]
  },
  {
    path: ':deviceId',
    loadComponent: () => import('./device-detail.page').then(m => m.DeviceDetailPage)
  }
];
