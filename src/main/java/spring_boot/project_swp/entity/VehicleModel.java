package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "VehicleModels")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ModelId")
  private Long modelId;

  @Column(name = "ModelName", nullable = false, unique = true, length = 100)
  private String modelName;

  @Column(name = "Brand", nullable = false, length = 100)
  private String brand;

  @Column(name = "Type", nullable = false, length = 100)
  private String type;

  @Column(name = "CapacityKWh", nullable = false)
  private int capacityKWh;

  @Column(name = "Description", length = 500)
  private String description;
}
