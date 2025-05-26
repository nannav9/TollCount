package com.toll.toll_data.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TollSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vehicleRegNo;

    @Column(nullable = false)
    private String narration;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private int zeroDebitCount;

    @Column(nullable = false)
    private int nonZeroDebitCount;
}
