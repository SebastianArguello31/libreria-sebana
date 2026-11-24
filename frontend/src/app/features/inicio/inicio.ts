import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-inicio',
  template: `
    <h1>Hola, {{ usuario()?.nombreCompleto }}</h1>
    <p class="ayuda">
      @if (esAdmin()) {
        Desde el menú podés gestionar los usuarios de la biblioteca.
      } @else {
        Bienvenido a la Biblioteca Sebana.
      }
    </p>
  `,
  styles: `
    h1 { margin: 0; font-family: var(--serif); font-weight: normal; }
    .ayuda { color: var(--tinta-suave); }
  `,
})
export class Inicio {
  private readonly auth = inject(AuthService);
  readonly usuario = this.auth.usuario;
  readonly esAdmin = this.auth.esAdmin;
}
