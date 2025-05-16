package com.toll.toll_data.model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetail {
   
   
    

    private String barCode;
    private String tagId;
    private String vehicleRegNo;
    
    @Id
    private String txnId;
    private String laneId;
@Column(name = "date")
    private LocalDateTime valueDate;

    private String narration;

    private Double credit;
    private Double debit;
    private BigDecimal walletBalance;
    private BigDecimal masterBalance;
    private BigDecimal sdBalance;
}
