import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Libro, LibroGuardar } from '../../../core/models/libro.models';
import { LibroService } from '../../../core/services/libro.service';
import { erroresDeCampos, mensajeDeError } from '../../../core/utils/errores';

type Campo =
  'titulo' | 'autor' | 'isbn' | 'editorial' | 'anioPublicacion' | 'descripcion' | 'stock';

@Component({
  selector: 'app-libro-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './libro_form.html',
  styleUrl: './libro_form.css',
})
export class LibroForm implements OnInit {
  private readonly libros = inject(LibroService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly id = input<string>();
  readonly esEdicion = computed(() => this.id() !== undefined);

  readonly libro = signal<Libro | null>(null);
  readonly cargando = signal(false);
  readonly cargaFallida = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);

  private readonly erroresServidor = signal<Partial<Record<Campo, string>>>({});

  readonly form = this.fb.group({
    titulo: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200)]),
    autor: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(150)]),
    isbn: this.fb.nonNullable.control('', [Validators.maxLength(20)]),
    editorial: this.fb.nonNullable.control('', [Validators.maxLength(120)]),
    anioPublicacion: this.fb.control<number | null>(null, [
      Validators.min(1),
      Validators.max(32767),
    ]),
    descripcion: this.fb.nonNullable.control('', [Validators.maxLength(5000)]),
    stock: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0)]),
  });

  ngOnInit(): void {
    if (!this.esEdicion()) return;

    this.cargando.set(true);

    this.libros
      .obtener(Number(this.id()))
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (libro) => {
          this.libro.set(libro);
          this.form.patchValue({
            titulo: libro.titulo,
            autor: libro.autor,
            isbn: libro.isbn ?? '',
            editorial: libro.editorial ?? '',
            anioPublicacion: libro.anioPublicacion,
            descripcion: libro.descripcion ?? '',
            stock: libro.stock,
          });
        },
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
    this.erroresServidor.set({});

    const valores = this.form.getRawValue();

    const datos: LibroGuardar = {
      titulo: valores.titulo.trim(),
      autor: valores.autor.trim(),
      isbn: this.opcional(valores.isbn),
      editorial: this.opcional(valores.editorial),
      anioPublicacion: valores.anioPublicacion,
      descripcion: this.opcional(valores.descripcion),
      stock: valores.stock,
    };

    const peticion = this.esEdicion()
      ? this.libros.actualizar(Number(this.id()), datos)
      : this.libros.crear(datos);

    peticion.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: () => {
        const mensaje = this.esEdicion() ? 'Libro actualizado.' : 'Libro creado.';

        this.router.navigate(['/admin/libros'], { state: { mensaje } });
      },
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
      if (control.hasError('required')) {
        return 'Este campo es obligatorio.';
      }

      if (control.hasError('maxlength')) {
        return `Máximo ${control.getError('maxlength').requiredLength} caracteres.`;
      }

      if (control.hasError('min')) {
        return 'El valor debe ser mayor o igual al mínimo permitido.';
      }

      if (control.hasError('max')) {
        return 'El año ingresado no es válido.';
      }
    }

    return this.erroresServidor()[campo] ?? null;
  }

  private opcional(valor: string): string | null {
    const limpio = valor.trim();
    return limpio.length > 0 ? limpio : null;
  }
}
