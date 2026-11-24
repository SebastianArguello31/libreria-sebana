import { Rol } from './auth.models';

export interface UsuarioCrear {
  email: string;
  nombreCompleto: string;
  password: string;
  rol: Rol;
}

export interface UsuarioActualizar {
  email: string;
  nombreCompleto: string;
  rol: Rol;
}

export interface FiltroUsuarios {
  texto?: string;
  rol?: Rol | '';
  activo?: boolean | null;
  page: number;
  size: number;
  sort?: string;
}

export interface PasswordCambiar {
  passwordActual: string;
  passwordNueva: string;
}

export const ETIQUETA_ROL: Record<Rol, string> = {
  ADMIN: 'Administrador',
  USUARIO: 'Socio lector',
};
