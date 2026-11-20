package py.una.sebana.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {
    public static final String ADMIN = "ADMIN";
    public static final String USUARIO = "USUARIO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(length = 120)
    private String descripcion;

    protected Rol() {
        // JPA exige un constructor sin argumentos para instanciar la entidad al leerla de la BD
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}