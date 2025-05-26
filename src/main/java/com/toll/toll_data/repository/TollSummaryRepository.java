package com.toll.toll_data.repository;

import com.toll.toll_data.model.TollSummary;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TollSummaryRepository extends JpaRepository<TollSummary, Long> {
   @Query("SELECT t FROM TollSummary t WHERE t.vehicleRegNo = :vehicleRegNo AND t.narration = :narration")
   List<TollSummary> findByNarrationAndVehicleRegNo(@Param("narration") String narration,
                                                  @Param("vehicleRegNo") String vehicleRegNo);

    @Query("SELECT t FROM TollSummary t WHERE t.vehicleRegNo = :vehicleRegNo")
    List<TollSummary> findByVehicleRegNo( @Param("vehicleRegNo") String vehicleRegNo);

    @Query("SELECT t FROM TollSummary t WHERE  t.narration = :narration")
    List<TollSummary> findByNarration(@Param("narration") String narration);

    List<TollSummary> findByVehicleRegNoContainingIgnoreCaseOrNarrationContainingIgnoreCase(String vehicleRegNo, String narration);

   @Query("""
    SELECT COUNT(t) FROM TransactionDetail t
    JOIN TollSummary s ON t.narration = s.narration AND t.vehicleRegNo = s.vehicleRegNo
    WHERE t.debit = 0 
    AND t.valueDate BETWEEN :newDate AND CURRENT_TIMESTAMP
    AND s.narration = :narration
    AND s.vehicleRegNo = :vehicleRegNo
""")
int updateZeroDebitCount(
    @Param("narration") String narration,
    @Param("vehicleRegNo") String vehicleRegNo,
    @Param("newDate") LocalDateTime newDate
);

  @Query("""
    SELECT COUNT(t) FROM TransactionDetail t
    JOIN TollSummary s ON t.narration = s.narration AND t.vehicleRegNo = s.vehicleRegNo
    WHERE t.debit != 0 
    AND t.valueDate BETWEEN :newDate AND CURRENT_TIMESTAMP
    AND s.narration = :narration
    AND s.vehicleRegNo = :vehicleRegNo
""")
int updateNonZeroDebitCount(
    @Param("narration") String narration,
    @Param("vehicleRegNo") String vehicleRegNo,
    @Param("newDate") LocalDateTime newDate
);




}
