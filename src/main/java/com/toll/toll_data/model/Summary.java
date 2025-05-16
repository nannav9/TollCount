package com.toll.toll_data.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Summary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;
    private String clientId;
    private String clientName;
    private String walletBalance;
    private String masterBalance;
    private String sdBalance;
}
