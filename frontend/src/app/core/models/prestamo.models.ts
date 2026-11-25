export type EstadoPrestamo = 'PRESTADO' | 'DEVUELTO' | 'ANULADO';

export interface Prestamo {
  id: number;
  usuarioId: number;
  nombreUsuario: string;
  emailUsuario: string;
  libroId: number;
  tituloLibro: string;
  isbnLibro: string | null;
  duracionDias: number;
  fechaPrestamo: string;
  fechaVencimiento: string;
  fechaDevolucion: string | null;
  estado: EstadoPrestamo;
  creadoEn: string;
}

export interface PrestamoCrear {
  libroId: number;
  duracionDias: number;
}

export interface PrestamoRenovar {
  duracionTotalDias: number;
}
