package com.toll.toll_data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.toll.toll_data.model.TransactionDetail;

public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {

    @Query("SELECT t.vehicleRegNo, COUNT(t) FROM TransactionDetail t " +
           "WHERE t.narration = :narration AND t.debit=0 AND t.valueDate  BETWEEN :start AND :end " +
           "GROUP BY t.vehicleRegNo")
    List<Object[]> countVehiclesByNarrationAndDate(String narration, LocalDateTime start, LocalDateTime end);

   
    @Query("SELECT t.narration as narration, COUNT(t) as count " +
       "FROM TransactionDetail t " +
       "WHERE t.vehicleRegNo = :vehicleRegNo AND t.debit=0"+
       "AND t.valueDate BETWEEN :startDate AND :endDate " +
       "GROUP BY t.narration")
List<Object[]> findNarrationCountsByVehicleRegNo(
    @Param("vehicleRegNo") String vehicleRegNo,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate);

@Query("SELECT DATE(t.valueDate) as date, t.narration as narration, COUNT(t) as count " +
       "FROM TransactionDetail t " +
       "WHERE t.vehicleRegNo = :vehicleRegNo AND t.debit = 0 AND t.valueDate BETWEEN :startDate AND :endDate " +
       "GROUP BY DATE(t.valueDate), t.narration " +
       "ORDER BY DATE(t.valueDate), t.narration")
List<Object[]> findNarrationCountsByVehicleAndDate(
    @Param("vehicleRegNo") String vehicleRegNo,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate);


}
