package ni.edu.uam.facturacionapp.model;

public class Categoria {
    private Integer id;
    private String nombre;
    private boolean activa;

    public Categoria() {}

    public Categoria(Integer id, String nombre, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.activa = activa;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    @Override
    public String toString() {
        return nombre; // Importante para que se muestre bien en el ComboBox de Productos
    }
}