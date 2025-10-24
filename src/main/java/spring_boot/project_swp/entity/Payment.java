package spring_boot.project_swp.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.naming.Name;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId")
    int paymentId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rentalId", referencedColumnName = "rentalId")
    @JsonBackReference
    Rental rental;

    @Column(name = "amount", nullable = false)
    double amount;

    @Column(name = "paymentMethod", nullable = false, length = 50)
    String paymentMethod;

    @Column(name = "paymentType", nullable = false)
    String paymentType;

    @Column(name = "transactionTime", length = 100)
    LocalDateTime transactionTime;

    @Column(name = "transactionCode", nullable = false, length = 150)
    String transactionCode;

    @Column(name = "status", nullable = false, length = 50)
    String status;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "UserId", referencedColumnName = "UserId")
    @JsonBackReference
    User staffId;
}
