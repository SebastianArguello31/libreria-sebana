import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Libro } from '../../../core/models/libro.models';
import { LibroService } from '../../../core/services/libro.service';
import { mensajeDeError } from '../../../core/utils/errores';

type FiltroEstado = 'todos' | 'activos' | 'inactivos';

@Component({
  selector: 'app-libros-lista',
  imports: [RouterLink],
  templateUrl: './libro_lista.html',
  styleUrl: './libro_lista.css',
})
export class LibrosLista {
  private readonly servicio = inject(LibroService);

  readonly libros = signal<Libro[]>([]);
  readonly busqueda = signal('');
  readonly estado = signal<FiltroEstado>('todos');
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(history.state?.mensaje ?? null);
  readonly procesando = signal<number | null>(null);

  readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLocaleLowerCase();
    const estado = this.estado();

    return this.libros().filter((libro) => {
      const coincideTexto =
        !texto ||
        libro.titulo.toLocaleLowerCase().includes(texto) ||
        libro.autor.toLocaleLowerCase().includes(texto) ||
        (libro.isbn ?? '').toLocaleLowerCase().includes(texto);

      const coincideEstado =
        estado === 'todos' ||
        (estado === 'activos' && libro.activo) ||
        (estado === 'inactivos' && !libro.activo);

      return coincideTexto && coincideEstado;
    });
  });

  constructor() {
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.servicio
      .listar()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (libros) => this.libros.set(libros),
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  iniciales(titulo: string): string {
    return titulo
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((parte) => parte[0])
      .join('')
      .toUpperCase();
  }

  darDeBaja(libro: Libro): void {
    if (!libro.activo || !confirm(`¿Dar de baja "${libro.titulo}"?`)) {
      return;
    }

    this.procesando.set(libro.id);
    this.error.set(null);
    this.aviso.set(null);

    this.servicio
      .darDeBaja(libro.id)
      .pipe(finalize(() => this.procesando.set(null)))
      .subscribe({
        next: () => {
          this.aviso.set(`"${libro.titulo}" fue dado de baja.`);
          this.recargar();
        },
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }
}
