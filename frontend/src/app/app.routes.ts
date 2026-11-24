import { Routes } from '@angular/router';
import { authGuard, guestGuard, rolGuard } from './core/guards/auth.guards';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Ingresar | Biblioteca Sebana',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/layout').then((m) => m.Layout),
    children: [
      {
        path: '',
        title: 'Inicio | Biblioteca Sebana',
        loadComponent: () => import('./features/inicio/inicio').then((m) => m.Inicio),
      },
      {
        path: 'usuarios',
        canActivate: [rolGuard('ADMIN')],
        loadChildren: () => import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
