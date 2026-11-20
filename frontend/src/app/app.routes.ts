import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Ingresar | Biblioteca Sebana',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  { path: '**', redirectTo: 'login' },
];
