import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Rol } from '../../../core/models/auth.models';
import { AuthService } from '../../../core/services/auth.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { erroresDeCampos, mensajeDeError } from '../../../core/utils/errores';

type Campo = 'email' | 'nombreCompleto' | 'rol' | 'password';

@Component({
  selector: 'app-usuario-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './usuario-form.html',
  styleUrl: './usuario-form.scss',
})
export class UsuarioForm implements OnInit {
  private readonly usuarios = inject(UsuarioService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);


  readonly id = input<string>();
  readonly esEdicion = computed(() => this.id() !== undefined);
  readonly esUnoMismo = computed(() => this.esEdicion() && Number(this.id()) === this.auth.usuario()?.id);
  readonly cargando = signal(false);
  readonly cargaFallida = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  private readonly erroresServidor = signal<Partial<Record<Campo, string>>>({});

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    nombreCompleto: ['', [Validators.required, Validators.maxLength(120)]],
    rol: this.fb.nonNullable.control<Rol>('USUARIO', Validators.required),
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  constructor() {
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.erroresServidor.set({}));
  }

  ngOnInit(): void {
    if (!this.esEdicion()) return;

    this.form.controls.password.disable();

    if (this.esUnoMismo()) {
      this.form.controls.email.disable();
      this.form.controls.rol.disable();
    }

    this.cargando.set(true);

    this.usuarios
      .obtener(Number(this.id()))
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (u) => this.form.patchValue({ email: u.email, nombreCompleto: u.nombreCompleto, rol: u.rol }),
        error: (err: HttpErrorResponse) => {
          this.cargaFallida.set(true);
          this.error.set(mensajeDeError(err));
        },
      });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set(null);

    const { email, nombreCompleto, rol, password } = this.form.getRawValue();
    const peticion = this.esEdicion()
      ? this.usuarios.actualizar(Number(this.id()), { email, nombreCompleto, rol })
      : this.usuarios.crear({ email, nombreCompleto, rol, password });

    peticion.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: () =>
        this.router.navigate(['/usuarios'], {
          state: { mensaje: this.esEdicion() ? 'Usuario actualizado.' : 'Usuario creado.' },
        }),
      error: (err: HttpErrorResponse) => {
        const campos = erroresDeCampos(err);
        this.erroresServidor.set(campos);
        if (Object.keys(campos).length === 0) {
          this.error.set(mensajeDeError(err));
        }
      },
    });
  }

  errorDe(campo: Campo): string | null {
    const control = this.form.controls[campo];
    if (control.touched && control.errors) {
      if (control.hasError('required')) return 'Este campo es obligatorio.';
      if (control.hasError('email')) return 'El email no tiene un formato válido.';
      if (control.hasError('minlength')) return `Mínimo ${control.getError('minlength').requiredLength} caracteres.`;
      if (control.hasError('maxlength')) return `Máximo ${control.getError('maxlength').requiredLength} caracteres.`;
    }
    return this.erroresServidor()[campo] ?? null;
  }
}
