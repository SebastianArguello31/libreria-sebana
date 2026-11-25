import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { LibroDisponible } from '../../../core/models/catalogo.models';
import { Prestamo } from '../../../core/models/prestamo.models';
import { Pagina } from '../../../core/models/pagina.models';
import { Usuario } from '../../../core/models/auth.models';
import { LibroCatalogoService } from '../../../core/services/libro_catalogo.service';
import { PrestamoService } from '../../../core/services/prestamo.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { mensajeDeError } from '../../../core/utils/errores';

type FiltroPrestamos = 'activos' | 'historial' | 'todos';

@Component({
  selector: 'app-admin-prestamos',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './admin_prestamos.html',
  styleUrl: './admin_prestamos.scss',
})
export class AdminPrestamos {
  private readonly prestamosApi = inject(PrestamoService);
  private readonly usuariosApi = inject(UsuarioService);
  private readonly librosApi = inject(LibroCatalogoService);
  private readonly fb = inject(FormBuilder);

  readonly prestamos = signal<Prestamo[]>([]);
  readonly usuarios = signal<Usuario[]>([]);
  readonly libros = signal<LibroDisponible[]>([]);
  readonly librosDisponibles = computed(() => this.libros().filter((libro) => libro.stock > 0));

  readonly filtro = signal<FiltroPrestamos>('activos');
  readonly prestamosVisibles = computed(() => {
    if (this.filtro() === 'activos') {
      return this.prestamos().filter((prestamo) => prestamo.estado === 'PRESTADO');
    }
    if (this.filtro() === 'historial') {
      return this.prestamos().filter((prestamo) => prestamo.estado !== 'PRESTADO');
    }
    return this.prestamos();
  });

  readonly cargando = signal(false);
  readonly mostrandoRegistro = signal(false);
  readonly procesandoRegistro = signal(false);
  readonly procesandoId = signal<number | null>(null);
  readonly prestamoRenovacion = signal<Prestamo | null>(null);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);

  readonly formRegistro = this.fb.group({
    usuarioId: ['', Validators.required],
    libroId: ['', Validators.required],
    duracionDias: this.fb.nonNullable.control(7, [
      Validators.required,
      Validators.min(7),
      Validators.max(30),
    ]),
  });

  readonly formRenovacion = this.fb.nonNullable.group({
    duracionTotalDias: [7, [Validators.required, Validators.min(7), Validators.max(30)]],
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    forkJoin({
      prestamos: this.prestamosApi.listarTodos(),
      usuarios: this.usuariosApi.listar({
        rol: 'USUARIO',
        activo: true,
        page: 0,
        size: 100,
        sort: 'nombreCompleto,asc',
      }),
      libros: this.librosApi.listarActivos(),
    })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (resultado: {
          prestamos: Prestamo[];
          usuarios: Pagina<Usuario>;
          libros: LibroDisponible[];
        }) => {
          this.prestamos.set(resultado.prestamos);
          this.usuarios.set(resultado.usuarios.content);
          this.libros.set(resultado.libros);
        },
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  cambiarFiltro(filtro: FiltroPrestamos): void {
    this.filtro.set(filtro);
    this.prestamoRenovacion.set(null);
  }

  registrarPresencial(): void {
    if (this.formRegistro.invalid) {
      this.formRegistro.markAllAsTouched();
      return;
    }

    const datos = this.formRegistro.getRawValue();
    const usuarioId = Number(datos.usuarioId);
    const libroId = Number(datos.libroId);

    this.procesandoRegistro.set(true);
    this.error.set(null);
    this.aviso.set(null);

    this.prestamosApi
      .registrarPresencial(usuarioId, {
        libroId,
        duracionDias: datos.duracionDias,
      })
      .pipe(finalize(() => this.procesandoRegistro.set(false)))
      .subscribe({
        next: () => {
          this.aviso.set('Préstamo presencial registrado.');
          this.formRegistro.reset({
            usuarioId: '',
            libroId: '',
            duracionDias: 7,
          });
          this.mostrandoRegistro.set(false);
          this.cargar();
        },
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  iniciarRenovacion(prestamo: Prestamo): void {
    if (prestamo.duracionDias >= 30) return;

    this.error.set(null);
    this.prestamoRenovacion.set(prestamo);
    this.formRenovacion.reset({
      duracionTotalDias: prestamo.duracionDias,
    });
  }

  duracionRenovacionInvalida(prestamo: Prestamo): boolean {
    const control = this.formRenovacion.controls.duracionTotalDias;
    return control.touched && control.value <= prestamo.duracionDias;
  }

  renovar(): void {
    const prestamo = this.prestamoRenovacion();
    if (!prestamo) return;

    if (this.formRenovacion.invalid) {
      this.formRenovacion.markAllAsTouched();
      return;
    }

    const duracionTotalDias = this.formRenovacion.controls.duracionTotalDias.value;

    if (duracionTotalDias <= prestamo.duracionDias) {
      this.formRenovacion.controls.duracionTotalDias.markAsTouched();
      return;
    }

    this.procesandoId.set(prestamo.id);
    this.error.set(null);

    this.prestamosApi
      .renovarComoAdmin(prestamo.id, { duracionTotalDias })
      .pipe(finalize(() => this.procesandoId.set(null)))
      .subscribe({
        next: () => {
          this.aviso.set('Préstamo renovado.');
          this.prestamoRenovacion.set(null);
          this.cargar();
        },
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  devolver(prestamo: Prestamo): void {
    if (!confirm(`¿Registrar la devolución de “${prestamo.tituloLibro}”?`)) {
      return;
    }

    this.procesandoId.set(prestamo.id);
    this.error.set(null);

    this.prestamosApi
      .devolver(prestamo.id)
      .pipe(finalize(() => this.procesandoId.set(null)))
      .subscribe({
        next: () => {
          this.aviso.set('Devolución registrada.');
          this.cargar();
        },
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  anular(prestamo: Prestamo): void {
    if (!confirm(`¿Anular el préstamo de “${prestamo.tituloLibro}”?`)) {
      return;
    }

    this.procesandoId.set(prestamo.id);
    this.error.set(null);

    this.prestamosApi
      .anular(prestamo.id)
      .pipe(finalize(() => this.procesandoId.set(null)))
      .subscribe({
        next: () => {
          this.aviso.set('Préstamo anulado.');
          this.cargar();
        },
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  etiquetaEstado(estado: Prestamo['estado']): string {
    if (estado === 'PRESTADO') return 'En préstamo';
    if (estado === 'DEVUELTO') return 'Devuelto';
    return 'Anulado';
  }
}
