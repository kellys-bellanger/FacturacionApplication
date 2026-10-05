package ni.edu.uam.facturacionapp.model;


import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Cargo {
    private Integer id;
    private String nombre;
    private String descripcion;
}
