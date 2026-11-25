export interface Libro {
  id: number;
  titulo: string;
  autor: string;
  isbn: string | null;
  editorial: string | null;
  anioPublicacion: number | null;
  descripcion: string | null;
  stock: number;
  activo: boolean;
  creadoEn: string | null;
}

export interface LibroGuardar {
  titulo: string;
  autor: string;
  isbn: string | null;
  editorial: string | null;
  anioPublicacion: number | null;
  descripcion: string | null;
  stock: number;
} 
