package com.toll.toll_data.controller;


import com.toll.toll_data.model.TollSummary;
import com.toll.toll_data.service.TollSummaryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/toll-summary")
public class TollSummaryController {

    private final TollSummaryService tollSummaryService;

    public TollSummaryController(TollSummaryService tollSummaryService) {
        this.tollSummaryService = tollSummaryService;
    }

    @PostMapping("/generate")
    public String generateSummary() {
        tollSummaryService.generateTollSummaries();
        return "Toll summary generated successfully!";
    }
    @GetMapping("/all")
    public List<TollSummary> getAll() {
        return tollSummaryService.findAll();
    }

    @GetMapping("/search")
    public List<TollSummary> getByVehicleOrNarration(
            @RequestParam(required = false) String vehicleRegNo,
            @RequestParam(required = false) String narration) {
        if (vehicleRegNo != null && narration != null) {
            return tollSummaryService.findByNarrationAndVehicleRegNo(narration, vehicleRegNo);
        } else if (vehicleRegNo != null) {
            return tollSummaryService.findByVehicleRegNo(vehicleRegNo);
        } else if (narration != null) {
            return tollSummaryService.findByNarration(narration);
        } else {
            return tollSummaryService.findAll();
        }
    }
@PutMapping("/update-payment-date")
public ResponseEntity<?> updatePaymentDate(@RequestBody Map<String, String> requestBody) {
    try {
        String narration = requestBody.get("narration");
        String vehicleRegNo = requestBody.get("vehicleRegNo");
        String dateStr = requestBody.get("paymentDate");

        if (narration == null || vehicleRegNo == null || dateStr == null) {
            return ResponseEntity.badRequest().body("Missing required fields: narration, vehicleRegNo, or paymentDate");
        }

        LocalDateTime newDate = LocalDateTime.parse(dateStr); // e.g. "2025-05-25T15:30:00"

        return tollSummaryService.updatePaymentDate(narration, vehicleRegNo, newDate)
                .map(updated -> ResponseEntity.ok(updated))
                .orElse(ResponseEntity.notFound().build());

    } catch (DateTimeParseException e) {
        return ResponseEntity.badRequest().body("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error updating payment date");
    }
}


}
