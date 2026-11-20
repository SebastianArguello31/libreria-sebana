import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Rol, Usuario } from '../models/auth.models';

interface Sesion {
  token: string;
  expiraEn: string;
  usuario: Usuario;
}

const STORAGE_KEY = 'sebana.sesion';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly url = `${environment.apiUrl}/auth`;
  private readonly sesion = signal<Sesion | null>(this.leerSesionGuardada());

  readonly usuario = computed(() => this.sesion()?.usuario ?? null);
  readonly estaAutenticado = computed(() => this.sesion() !== null);
  readonly esAdmin = computed(() => this.usuario()?.rol === 'ADMIN');

  login(credenciales: LoginRequest): Observable<Usuario> {
    return this.http.post<LoginResponse>(`${this.url}/login`, credenciales).pipe(
      tap((res) => this.guardarSesion({ token: res.token, expiraEn: res.expiraEn, usuario: res.usuario })),
      map((res) => res.usuario),
    );
  }

  logout(): void {
    sessionStorage.removeItem(STORAGE_KEY);
    this.sesion.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    const s = this.sesion();
    if (!s) return null;
    if (this.estaVencida(s)) {
      this.logout();
      return null;
    }
    return s.token;
  }

  tieneRol(...roles: Rol[]): boolean {
    const rol = this.usuario()?.rol;
    return rol !== undefined && roles.includes(rol);
  }

  private guardarSesion(sesion: Sesion): void {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(sesion));
    this.sesion.set(sesion);
  }

  private leerSesionGuardada(): Sesion | null {
    try {
      const json = sessionStorage.getItem(STORAGE_KEY);
      if (!json) return null;

      const sesion = JSON.parse(json) as Sesion;
      if (this.estaVencida(sesion)) {
        sessionStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return sesion;
    } catch {
      sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }

  private estaVencida(sesion: Sesion): boolean {
    return new Date(sesion.expiraEn).getTime() <= Date.now();
  }
}
