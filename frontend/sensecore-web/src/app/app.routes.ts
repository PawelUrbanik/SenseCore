import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'devices' },
  {
    path: 'devices',
    loadChildren: () => import('./features/devices/devices.routes').then(m => m.DEVICES_ROUTES)
  },
  {
    path: '**',
    loadComponent: () => import('./features/not-found/not-found.page').then(m => m.NotFoundPage)
  }
];
