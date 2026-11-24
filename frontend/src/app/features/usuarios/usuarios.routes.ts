import { Routes } from '@angular/router';

export const USUARIOS_ROUTES: Routes = [
  {
    path: '',
    title: 'Usuarios | Biblioteca Sebana',
    loadComponent: () => import('./lista/usuarios-lista').then((m) => m.UsuariosLista),
  },
  {
    path: 'nuevo',
    title: 'Nuevo usuario | Biblioteca Sebana',
    loadComponent: () => import('./form/usuario-form').then((m) => m.UsuarioForm),
  },
  {
    path: ':id',
    title: 'Editar usuario | Biblioteca Sebana',
    loadComponent: () => import('./form/usuario-form').then((m) => m.UsuarioForm),
  },
];
