package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VehicleModels")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ModelId")
    private int modelId;
    @Column(name = "ModelName", nullable = false, unique = true, length = 100)
    private String modelName;
    @Column(name = "Brand", nullable = false, length = 100)
    private String brand;
    @Column(name = "Type", nullable = false, length = 100)
    private String type;
    @Column(name = "CapacityKWh", nullable = false, length = 100)
    private String capacityKWh;
    @Column(name = "Description", length = 500)
    private String description;


}
