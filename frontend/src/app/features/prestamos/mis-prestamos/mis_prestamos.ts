import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { Prestamo } from '../../../core/models/prestamo.models';
import { PrestamoService } from '../../../core/services/prestamo.service';
import { mensajeDeError } from '../../../core/utils/errores';

type VistaPrestamos = 'actuales' | 'historial';

@Component({
  selector: 'app-mis-prestamos',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './mis_prestamos.html',
  styleUrl: './mis_prestamos.scss',
})
export class MisPrestamos {
  private readonly prestamosApi = inject(PrestamoService);
  private readonly fb = inject(FormBuilder);

  readonly vista = signal<VistaPrestamos>('actuales');
  readonly actuales = signal<Prestamo[]>([]);
  readonly historial = signal<Prestamo[]>([]);
  readonly prestamosVisibles = computed(() =>
    this.vista() === 'actuales' ? this.actuales() : this.historial(),
  );

  readonly cargando = signal(false);
  readonly renovandoId = signal<number | null>(null);
  readonly prestamoSeleccionado = signal<Prestamo | null>(null);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    duracionTotalDias: [7, [Validators.required, Validators.min(7), Validators.max(30)]],
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    forkJoin({
      actuales: this.prestamosApi.listarMisActuales(),
      historial: this.prestamosApi.listarMiHistorial(),
    })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (resultado) => {
          this.actuales.set(resultado.actuales);
          this.historial.set(resultado.historial);
        },
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  cambiarVista(vista: VistaPrestamos): void {
    this.vista.set(vista);
    this.error.set(null);
    this.aviso.set(null);
    this.prestamoSeleccionado.set(null);
  }

  iniciarRenovacion(prestamo: Prestamo): void {
    if (prestamo.duracionDias >= 30) return;

    this.error.set(null);
    this.aviso.set(null);
    this.prestamoSeleccionado.set(prestamo);
    this.form.reset({ duracionTotalDias: prestamo.duracionDias });
  }

  cancelarRenovacion(): void {
    this.prestamoSeleccionado.set(null);
    this.error.set(null);
  }

  duracionNoAumenta(prestamo: Prestamo): boolean {
    const control = this.form.controls.duracionTotalDias;
    return control.touched && control.value <= prestamo.duracionDias;
  }

  renovar(): void {
    const prestamo = this.prestamoSeleccionado();
    if (!prestamo) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const duracionTotalDias = this.form.controls.duracionTotalDias.value;

    if (duracionTotalDias <= prestamo.duracionDias) {
      this.form.controls.duracionTotalDias.markAsTouched();
      return;
    }

    this.renovandoId.set(prestamo.id);
    this.error.set(null);

    this.prestamosApi
      .renovarMio(prestamo.id, { duracionTotalDias })
      .pipe(finalize(() => this.renovandoId.set(null)))
      .subscribe({
        next: () => {
          this.aviso.set('Préstamo renovado.');
          this.prestamoSeleccionado.set(null);
          this.cargar();
        },
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  etiquetaEstado(estado: Prestamo['estado']): string {
    return estado === 'DEVUELTO' ? 'Devuelto' : 'Anulado';
  }
}
