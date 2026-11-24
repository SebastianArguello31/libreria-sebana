export type Rol = 'ADMIN' | 'USUARIO';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface Usuario {
  id: number;
  email: string;
  nombreCompleto: string;
  rol: Rol;
  activo: boolean;
  creadoEn: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEn: string;
  usuario: Usuario;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  mensaje: string;
  path: string;
  campos?: Record<string, string>;
}
