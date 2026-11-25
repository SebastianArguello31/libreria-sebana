package py.una.sebana.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "prestamos")
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(name = "duracion_dias", nullable = false)
    private Short duracionDias;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPrestamo estado = EstadoPrestamo.PRESTADO;

    @org.hibernate.annotations.Generated
    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime creadoEn;

    protected Prestamo() {
        // Constructor requerido por JPA
    }

    public Prestamo(Usuario usuario, Libro libro, int duracionDias) {
        this.usuario = usuario;
        this.libro = libro;
        this.fechaPrestamo = LocalDate.now();
        this.duracionDias = (short) duracionDias;
        this.fechaVencimiento = this.fechaPrestamo.plusDays(duracionDias);
        this.estado = EstadoPrestamo.PRESTADO;
    }

    public Integer getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Libro getLibro() { return libro; }
    public Short getDuracionDias() { return duracionDias; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public EstadoPrestamo getEstado() { return estado; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }

    public void renovar(int nuevaDuracionTotalDias) {
        this.duracionDias = (short) nuevaDuracionTotalDias;
        this.fechaVencimiento = this.fechaPrestamo.plusDays(nuevaDuracionTotalDias);
    }

    public void marcarDevuelto(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
        this.estado = EstadoPrestamo.DEVUELTO;
    }

    public void anular() {
        this.estado = EstadoPrestamo.ANULADO;
    }
}