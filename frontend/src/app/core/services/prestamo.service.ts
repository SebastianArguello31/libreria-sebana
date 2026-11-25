import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Prestamo, PrestamoCrear, PrestamoRenovar } from '../models/prestamo.models';

@Injectable({ providedIn: 'root' })
export class PrestamoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;
  private readonly url = `${this.apiUrl}/prestamos`;
  private readonly adminUrl = `${this.apiUrl}/admin/prestamos`;

  solicitar(datos: PrestamoCrear): Observable<Prestamo> {
    return this.http.post<Prestamo>(this.url, datos);
  }

  listarMisActuales(): Observable<Prestamo[]> {
    return this.http.get<Prestamo[]>(`${this.url}/mios/actuales`);
  }

  listarMiHistorial(): Observable<Prestamo[]> {
    return this.http.get<Prestamo[]>(`${this.url}/mios/historial`);
  }

  renovarMio(id: number, datos: PrestamoRenovar): Observable<Prestamo> {
    return this.http.put<Prestamo>(`${this.url}/${id}/renovar`, datos);
  }

  listarTodos(): Observable<Prestamo[]> {
    return this.http.get<Prestamo[]>(this.adminUrl);
  }

  registrarPresencial(usuarioId: number, datos: PrestamoCrear): Observable<Prestamo> {
    return this.http.post<Prestamo>(`${this.apiUrl}/admin/usuarios/${usuarioId}/prestamos`, datos);
  }

  renovarComoAdmin(id: number, datos: PrestamoRenovar): Observable<Prestamo> {
    return this.http.put<Prestamo>(`${this.adminUrl}/${id}/renovar`, datos);
  }

  devolver(id: number): Observable<Prestamo> {
    return this.http.post<Prestamo>(`${this.adminUrl}/${id}/devolver`, {});
  }

  anular(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/${id}`);
  }
}
