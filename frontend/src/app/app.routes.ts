import { Routes } from '@angular/router';
import { guestGuard, rolGuard } from './core/guards/auth.guards';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Ingresar | Biblioteca Sebana',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: '',
    loadComponent: () => import('./layout/layout').then((m) => m.Layout),
    children: [
      {
        path: '',
        pathMatch: 'full',
        title: 'Catálogo | Biblioteca Sebana',
        loadComponent: () =>
          import('./features/prestamos/catalogo/catalogo_libros').then((m) => m.CatalogoLibros),
      },
      {
        path: 'catalogo',
        pathMatch: 'full',
        redirectTo: '',
      },
      {
        path: 'mis-prestamos',
        title: 'Mis préstamos | Biblioteca Sebana',
        canActivate: [rolGuard('USUARIO')],
        loadComponent: () =>
          import('./features/prestamos/mis-prestamos/mis_prestamos').then((m) => m.MisPrestamos),
      },
      {
        path: 'admin',
        canActivate: [rolGuard('ADMIN')],
        children: [
          {
            path: 'usuarios',
            loadChildren: () =>
              import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES),
          },
          {
            path: 'prestamos',
            title: 'Gestión de préstamos | Biblioteca Sebana',
            loadComponent: () =>
              import('./features/prestamos/admin/admin_prestamos').then((m) => m.AdminPrestamos),
          },
          {
            path: 'libros',
            title: 'Libros | Biblioteca Sebana',
            loadChildren: () =>
              import('./features/libros/libros.routes').then((m) => m.LIBROS_ROUTES),
          },
        ],
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
