package com.toll.toll_data.controller;

import com.toll.toll_data.service.TransactionQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionQueryController {

    private final TransactionQueryService queryService;

    public TransactionQueryController(TransactionQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/vehicleCountByNarration")
    public Map<String, Long> getVehicleCountsWithDebitZero(
            @RequestParam String narration,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return queryService.getVehicleCounts(narration, startDate, endDate);
    }




    @GetMapping("/narrationCountsByVehicle")
public Map<String, Long> getNarrationCountsByVehicle(
        @RequestParam String vehicleRegNo,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

    return queryService.getNarrationCountsByVehicleRegNo(vehicleRegNo, startDate, endDate);
}

@GetMapping("/narrationCountsByVehicleAndDate")
public Map<String, Map<String, Long>> getNarrationCountsByVehicleAndDate(
        @RequestParam String vehicleRegNo,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

    return queryService.getNarrationCountsPerDateForVehicle(vehicleRegNo, startDate, endDate);
}


}
