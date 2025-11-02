package spring_boot.project_swp.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalDiscountsId implements Serializable {
  private Long rental;
  private Long discount;
}
