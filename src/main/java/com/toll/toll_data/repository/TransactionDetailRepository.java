package com.toll.toll_data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.toll.toll_data.model.TransactionDetail;

public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {

    @Query("SELECT t.vehicleRegNo, COUNT(t) FROM TransactionDetail t " +
           "WHERE t.narration = :narration AND t.debit = 0 AND t.valueDate BETWEEN :start AND :end " +
           "GROUP BY t.vehicleRegNo")
    List<Object[]> countVehiclesByNarrationAndDate(
        @Param("narration") String narration, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end);

    @Query("SELECT t.narration, COUNT(t) FROM TransactionDetail t " +
           "WHERE t.vehicleRegNo = :vehicleRegNo AND t.debit = 0 " +
           "AND t.valueDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.narration")
    List<Object[]> findNarrationCountsByVehicleRegNo(
        @Param("vehicleRegNo") String vehicleRegNo,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // @Query("SELECT FUNCTION('DATE', t.valueDate) as date, t.narration, COUNT(t) FROM TransactionDetail t " +
    //        "WHERE t.vehicleRegNo = :vehicleRegNo AND t.debit = 0 " +
    //        "AND t.valueDate BETWEEN :startDate AND :endDate " +
    //        "GROUP BY FUNCTION('DATE', t.valueDate), t.narration " +
    //        "ORDER BY FUNCTION('DATE', t.valueDate), t.narration")
    // List<Object[]> findNarrationCountsByVehicleAndDate(
    //     @Param("vehicleRegNo") String vehicleRegNo,
    //     @Param("startDate") LocalDateTime startDate,
    //     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT t.narration, t.vehicleRegNo FROM TransactionDetail t ")
    List<Object[]> findDistinctNarrationAndVehicle();

    // Fix: Return type should be Long instead of long for queries
    @Query("""
        SELECT COUNT(t) FROM TransactionDetail t
        JOIN TollSummary s ON t.narration = s.narration AND t.vehicleRegNo = s.vehicleRegNo
        WHERE t.debit = 0 AND t.valueDate BETWEEN s.paymentDate AND CURRENT_TIMESTAMP
        """)    
    Long countByNarrationAndVehicleRegNoAndDebitIsZero(
        @Param("narration") String narration,
        @Param("vehicleRegNo") String vehicleRegNo
    );

    @Query("""
        SELECT COUNT(t) FROM TransactionDetail t
        JOIN TollSummary s ON t.narration = s.narration AND t.vehicleRegNo = s.vehicleRegNo
        WHERE t.debit != 0 AND t.valueDate BETWEEN s.paymentDate AND CURRENT_TIMESTAMP
        """)    
    Long countByNarrationAndVehicleRegNoAndDebitNonZero(
        @Param("narration") String narration,
        @Param("vehicleRegNo") String vehicleRegNo
    );
}
