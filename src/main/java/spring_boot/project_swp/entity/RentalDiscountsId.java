package spring_boot.project_swp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalDiscountsId implements Serializable {
    private Integer rental;
    private Integer discount;
}