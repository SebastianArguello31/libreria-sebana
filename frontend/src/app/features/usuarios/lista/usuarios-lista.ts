import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, catchError, debounceTime, finalize, of, switchMap, tap } from 'rxjs';
import { Rol, Usuario } from '../../../core/models/auth.models';
import { Pagina } from '../../../core/models/pagina.models';
import { ETIQUETA_ROL, FiltroUsuarios } from '../../../core/models/usuario.models';
import { AuthService } from '../../../core/services/auth.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { mensajeDeError } from '../../../core/utils/errores';

const TAMANIO_PAGINA = 10;

@Component({
  selector: 'app-usuarios-lista',
  imports: [ReactiveFormsModule, DatePipe, RouterLink],
  templateUrl: './usuarios-lista.html',
  styleUrl: './usuarios-lista.scss',
})
export class UsuariosLista {
  private readonly usuarios = inject(UsuarioService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly recargar$ = new Subject<void>();
  private readonly pagina = signal(0);

  readonly etiquetaRol = ETIQUETA_ROL;
  readonly idActual = computed(() => this.auth.usuario()?.id);
  readonly resultado = signal<Pagina<Usuario> | null>(null);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);

  /** Mensaje de éxito: llega del formulario (router state) o lo ponen las acciones de la tabla. */
  readonly aviso = signal<string | null>(history.state?.mensaje ?? null);

  /** Id del usuario sobre el que hay una acción en curso, para deshabilitar sus botones. */
  readonly procesando = signal<number | null>(null);

  readonly form = this.fb.nonNullable.group({
    texto: [''],
    rol: this.fb.nonNullable.control<Rol | ''>(''),
    activo: this.fb.nonNullable.control<'' | 'true' | 'false'>(''),
  });

  // Diálogo para restablecer contraseña
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogoPassword');
  readonly usuarioPassword = signal<Usuario | null>(null);
  readonly errorPassword = signal<string | null>(null);
  readonly passwordForm = this.fb.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  constructor() {
    this.recargar$
      .pipe(
        tap(() => {
          this.cargando.set(true);
          this.error.set(null);
        }),
        switchMap(() =>
          this.usuarios.listar(this.filtroActual()).pipe(
            catchError((err: HttpErrorResponse) => {
              this.error.set(mensajeDeError(err));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((res) => {
        this.cargando.set(false);
        if (res) this.resultado.set(res);
      });

    this.form.valueChanges
      .pipe(debounceTime(300), takeUntilDestroyed())
      .subscribe(() => this.irAPagina(0));

    this.recargar$.next();
  }

  irAPagina(numero: number): void {
    this.pagina.set(numero);
    this.recargar$.next();
  }

  cambiarEstado(u: Usuario): void {
    const activar = !u.activo;
    if (!activar && !confirm(`¿Desactivar a ${u.nombreCompleto}? No va a poder iniciar sesión.`)) {
      return;
    }

    this.procesando.set(u.id);
    this.aviso.set(null);
    this.error.set(null);

    this.usuarios
      .cambiarEstado(u.id, activar)
      .pipe(finalize(() => this.procesando.set(null)))
      .subscribe({
        next: () => {
          this.aviso.set(`${u.nombreCompleto} fue ${activar ? 'activado' : 'desactivado'}.`);
          this.recargar$.next();
        },
        error: (err: HttpErrorResponse) => this.error.set(mensajeDeError(err)),
      });
  }

  abrirPassword(u: Usuario): void {
    this.usuarioPassword.set(u);
    this.passwordForm.reset();
    this.errorPassword.set(null);
    this.dialogo().nativeElement.showModal();
  }

  cerrarPassword(): void {
    this.dialogo().nativeElement.close();
  }

  guardarPassword(): void {
    const u = this.usuarioPassword();
    if (!u) return;
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.procesando.set(u.id);
    this.errorPassword.set(null);

    this.usuarios
      .restablecerPassword(u.id, this.passwordForm.getRawValue().password)
      .pipe(finalize(() => this.procesando.set(null)))
      .subscribe({
        next: () => {
          this.cerrarPassword();
          this.aviso.set(`Contraseña de ${u.nombreCompleto} restablecida.`);
        },
        error: (err: HttpErrorResponse) => this.errorPassword.set(mensajeDeError(err)),
      });
  }

  passwordInvalida(): boolean {
    const control = this.passwordForm.controls.password;
    return control.invalid && control.touched;
  }

  private filtroActual(): FiltroUsuarios {
    const { texto, rol, activo } = this.form.getRawValue();
    return {
      texto,
      rol,
      activo: activo === '' ? null : activo === 'true',
      page: this.pagina(),
      size: TAMANIO_PAGINA,
      sort: 'nombreCompleto,asc',
    };
  }
}
