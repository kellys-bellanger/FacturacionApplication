package ni.edu.uam.facturacionapp.model;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Producto {
    private Integer id;
    private String codigo;
    private String nombre;
    private Categoria categoria;
    private BigDecimal precioVenta;
    private int existencia;
    private String rutaImagen;
    private boolean activo;

    // Constructor sin rutaImagen (para compatibilidad previa)
    public Producto(Integer id, String codigo, String nombre, Categoria categoria, BigDecimal precioVenta, int existencia, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precioVenta = precioVenta;
        this.existencia = existencia;
        this.rutaImagen = null;
        this.activo = activo;
    }
}