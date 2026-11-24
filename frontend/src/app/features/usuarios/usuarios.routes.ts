import { Routes } from '@angular/router';

export const USUARIOS_ROUTES: Routes = [
  {
    path: '',
    title: 'Usuarios | Biblioteca Sebana',
    loadComponent: () => import('./lista/usuarios-lista').then((m) => m.UsuariosLista),
  },
];
