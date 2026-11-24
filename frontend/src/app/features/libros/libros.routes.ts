import { Routes } from '@angular/router';

export const LIBROS_ROUTES: Routes = [
  {
    path: '',
    title: 'Libros | Biblioteca Sebana',
    loadComponent: () => import('./lista/libro_lista').then((m) => m.LibrosLista),
  },
  {
    path: 'nuevo',
    title: 'Nuevo libro | Biblioteca Sebana',
    loadComponent: () => import('./form/libro_form').then((m) => m.LibroForm),
  },
  {
    path: ':id',
    title: 'Editar libro | Biblioteca Sebana',
    loadComponent: () => import('./form/libro_form').then((m) => m.LibroForm),
  },
];
