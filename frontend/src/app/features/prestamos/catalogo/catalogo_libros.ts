import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { LibroDisponible } from '../../../core/models/catalogo.models';
import { LibroCatalogoService } from '../../../core/services/libro_catalogo.service';
import { PrestamoService } from '../../../core/services/prestamo.service';
import { mensajeDeError } from '../../../core/utils/errores';

@Component({
  selector: 'app-catalogo-libros',
  imports: [ReactiveFormsModule],
  templateUrl: './catalogo_libros.html',
  styleUrl: './catalogo_libros.scss',
})
export class CatalogoLibros {
  private readonly librosApi = inject(LibroCatalogoService);
  private readonly prestamosApi = inject(PrestamoService);
  private readonly fb = inject(FormBuilder);

  readonly libros = signal<LibroDisponible[]>([]);
  readonly cargando = signal(false);
  readonly solicitandoId = signal<number | null>(null);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    duracionDias: [7, [Validators.required, Validators.min(7), Validators.max(30)]],
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.librosApi
      .listarActivos()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (libros) => this.libros.set(libros),
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  solicitar(libro: LibroDisponible): void {
    if (libro.stock <= 0 || this.solicitandoId() !== null) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.solicitandoId.set(libro.id);
    this.error.set(null);
    this.aviso.set(null);

    this.prestamosApi
      .solicitar({
        libroId: libro.id,
        duracionDias: this.form.controls.duracionDias.value,
      })
      .pipe(finalize(() => this.solicitandoId.set(null)))
      .subscribe({
        next: (prestamo) => {
          this.aviso.set(
            `Préstamo creado. Debes devolver “${prestamo.tituloLibro}” el ${prestamo.fechaVencimiento}.`,
          );
          this.cargar();
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(mensajeDeError(err));
        },
      });
  }

  duracionInvalida(): boolean {
    const control = this.form.controls.duracionDias;
    return control.invalid && control.touched;
  }
}
