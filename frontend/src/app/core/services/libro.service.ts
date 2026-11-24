import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Libro, LibroGuardar } from '../models/libro.models';

@Injectable({ providedIn: 'root' })
export class LibroService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/admin/libros`;

  listar(): Observable<Libro[]> {
    return this.http.get<Libro[]>(this.url);
  }

  obtener(id: number): Observable<Libro> {
    return this.http.get<Libro>(`${this.url}/${id}`);
  }

  crear(datos: LibroGuardar): Observable<Libro> {
    return this.http.post<Libro>(this.url, datos);
  }

  actualizar(id: number, datos: LibroGuardar): Observable<Libro> {
    return this.http.put<Libro>(`${this.url}/${id}`, datos);
  }

  darDeBaja(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
