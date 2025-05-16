package com.toll.toll_data.service;

import com.toll.toll_data.repository.TransactionDetailRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class TransactionQueryService {

    private final TransactionDetailRepository transactionRepo;

    public TransactionQueryService(TransactionDetailRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    // ✅ Debit = 0 specific method
  

    // 🔁 Reuse for general narration filtering
    public Map<String, Long> getVehicleCounts(String narration, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = transactionRepo.countVehiclesByNarrationAndDate(narration, startDate, endDate);
        Map<String, Long> vehicleCountMap = new HashMap<>();
        for (Object[] row : results) {
            String vehicleRegNo = (String) row[0];
            Long count = (Long) row[1];
            vehicleCountMap.put(vehicleRegNo, count);
        }
        return vehicleCountMap;
    }

  

public Map<String, Long> getNarrationCountsByVehicleRegNo(String vehicleRegNo, LocalDateTime startDate, LocalDateTime endDate) {
    List<Object[]> results = transactionRepo.findNarrationCountsByVehicleRegNo(vehicleRegNo, startDate, endDate);

    Map<String, Long> narrationCountMap = new HashMap<>();
    for (Object[] row : results) {
        String narration = (String) row[0];
        Long count = (Long) row[1];
        narrationCountMap.put(narration, count);
    }
    return narrationCountMap;
}

public Map<String, Map<String, Long>> getNarrationCountsPerDateForVehicle(String vehicleRegNo, LocalDateTime startDate, LocalDateTime endDate) {
    List<Object[]> results = transactionRepo.findNarrationCountsByVehicleAndDate(vehicleRegNo, startDate, endDate);

    Map<String, Map<String, Long>> dateWiseNarrationCount = new HashMap<>();

    for (Object[] row : results) {
        // Convert SQL Date to String (or LocalDate if needed)
        String date = row[0].toString();  // or: LocalDate date = ((Date) row[0]).toLocalDate();
        String narration = (String) row[1];
        Long count = (Long) row[2];

        // Initialize map for each date
        dateWiseNarrationCount
            .computeIfAbsent(date, k -> new HashMap<>())
            .put(narration, count);
    }

    return dateWiseNarrationCount;
}


}
