import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LibroDisponible } from '../models/catalogo.models';

@Injectable({ providedIn: 'root' })
export class LibroCatalogoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/libros`;

  listarActivos(): Observable<LibroDisponible[]> {
    return this.http.get<LibroDisponible[]>(this.url);
  }
}
