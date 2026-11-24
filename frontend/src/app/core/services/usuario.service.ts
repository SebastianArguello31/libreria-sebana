import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Usuario } from '../models/auth.models';
import { Pagina } from '../models/pagina.models';
import { FiltroUsuarios, UsuarioActualizar, UsuarioCrear } from '../models/usuario.models';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/usuarios`;

  listar(filtro: FiltroUsuarios): Observable<Pagina<Usuario>> {
    let params = new HttpParams().set('page', filtro.page).set('size', filtro.size);

    if (filtro.texto?.trim()) params = params.set('texto', filtro.texto.trim());
    if (filtro.rol) params = params.set('rol', filtro.rol);
    if (filtro.activo !== undefined && filtro.activo !== null) params = params.set('activo', filtro.activo);
    if (filtro.sort) params = params.set('sort', filtro.sort);

    return this.http.get<Pagina<Usuario>>(this.url, { params });
  }

  obtener(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.url}/${id}`);
  }

  crear(datos: UsuarioCrear): Observable<Usuario> {
    return this.http.post<Usuario>(this.url, datos);
  }

  actualizar(id: number, datos: UsuarioActualizar): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.url}/${id}`, datos);
  }

  cambiarEstado(id: number, activo: boolean): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.url}/${id}/estado`, { activo });
  }

  restablecerPassword(id: number, password: string): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}/password`, { password });
  }
}
