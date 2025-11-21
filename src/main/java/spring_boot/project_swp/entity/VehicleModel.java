package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.*;
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
  Long modelId;

  @Column(name = "ModelName", nullable = false, unique = true, length = 100)
  String modelName;

  @Column(name = "Brand", nullable = false, length = 100)
  String brand; // VinFast, Yamaha...

  @Column(name = "Type", nullable = false, length = 100)
  String type; // Electric Scooter, Bike...

  @Column(name = "CapacityKWh", nullable = false)
  int capacityKWh; // Dung lượng pin thiết kế

  @Column(name = "Description", length = 500)
  String description;
}
