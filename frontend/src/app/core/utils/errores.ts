import { HttpErrorResponse } from '@angular/common/http';
import { ErrorResponse } from '../models/auth.models';

export function mensajeDeError(err: HttpErrorResponse): string {
  if (err.status === 0) {
    return 'No se pudo conectar con el servidor. Verificá que la API esté en ejecución.';
  }
  const body = err.error as ErrorResponse | null;
  if (err.status === 400 && body?.campos) {
    return Object.values(body.campos).join('. ');
  }
  return body?.mensaje ?? 'Ocurrió un error inesperado. Intentá de nuevo.';
}

export function erroresDeCampos(err: HttpErrorResponse): Record<string, string> {
  return err.status === 400 ? ((err.error as ErrorResponse | null)?.campos ?? {}) : {};
}
