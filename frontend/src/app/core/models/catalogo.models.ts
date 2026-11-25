export interface LibroDisponible {
  id: number;
  titulo: string;
  autor: string;
  isbn: string | null;
  stock: number;
  activo: boolean;
}
