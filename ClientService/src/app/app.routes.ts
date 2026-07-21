import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [guestGuard],
    children: [
      { path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./features/auth/register.component').then(m => m.RegisterComponent) },
      { path: 'verify-email', title: 'Verify email · IntroTech', loadComponent: () => import('./features/auth/verify-email.component').then(m => m.VerifyEmailComponent) },
      { path: '', pathMatch: 'full', redirectTo: 'login' }
    ]
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/app-shell.component').then(m => m.AppShellComponent),
    children: [
      { path: 'dashboard', title: 'Dashboard · IntroTech', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'workers', title: 'Workers · IntroTech', loadComponent: () => import('./features/workers/workers.component').then(m => m.WorkersComponent) },
      { path: 'workers/:id', title: 'Worker details · IntroTech', loadComponent: () => import('./features/workers/worker-detail.component').then(m => m.WorkerDetailComponent) },
      { path: 'schedules', title: 'Schedules · IntroTech', loadComponent: () => import('./features/schedules/schedules.component').then(m => m.SchedulesComponent) },
      { path: 'payments', title: 'Payments · IntroTech', loadComponent: () => import('./features/payments/payments.component').then(m => m.PaymentsComponent) },
      { path: 'reports', title: 'Reports · IntroTech', loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent) },
      { path: 'settings', title: 'Settings · IntroTech', loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent) },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' }
    ]
  },
  { path: '**', loadComponent: () => import('./features/not-found.component').then(m => m.NotFoundComponent) }
];
