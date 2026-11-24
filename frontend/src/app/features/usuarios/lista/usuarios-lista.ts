import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, catchError, debounceTime, of, switchMap, tap } from 'rxjs';
import { Rol, Usuario } from '../../../core/models/auth.models';
import { Pagina } from '../../../core/models/pagina.models';
import { ETIQUETA_ROL, FiltroUsuarios } from '../../../core/models/usuario.models';
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
  private readonly fb = inject(FormBuilder);
  private readonly recargar$ = new Subject<void>();
  private readonly pagina = signal(0);

  readonly etiquetaRol = ETIQUETA_ROL;
  readonly resultado = signal<Pagina<Usuario> | null>(null);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(history.state?.mensaje ?? null);

  readonly form = this.fb.nonNullable.group({
    texto: [''],
    rol: this.fb.nonNullable.control<Rol | ''>(''),
    activo: this.fb.nonNullable.control<'' | 'true' | 'false'>(''),
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
