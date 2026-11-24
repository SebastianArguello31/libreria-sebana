package py.una.sebana.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(length = 120)
    private String editorial;

    @Column(name = "anio_publicacion")
    private Short anioPublicacion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime creadoEn;

    protected Libro() {
    }

    public Libro(
            String titulo,
            String autor,
            String isbn,
            String editorial,
            Short anioPublicacion,
            String descripcion,
            Integer stock
    ) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.descripcion = descripcion;
        this.stock = stock;
    }

    public Integer getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getIsbn() { return isbn; }
    public String getEditorial() { return editorial; }
    public Short getAnioPublicacion() { return anioPublicacion; }
    public String getDescripcion() { return descripcion; }
    public Integer getStock() { return stock; }
    public boolean isActivo() { return activo; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAutor(String autor) { this.autor = autor; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setEditorial(String editorial) { this.editorial = editorial; }
    public void setAnioPublicacion(Short anioPublicacion) { this.anioPublicacion = anioPublicacion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setStock(Integer stock) { this.stock = stock; }
    public void setActivo(boolean activo) { this.activo = activo; }
}