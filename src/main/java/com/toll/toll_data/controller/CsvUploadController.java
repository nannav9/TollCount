package com.toll.toll_data.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.toll.toll_data.service.CsvImportService;

@RestController
@RequestMapping("/api/csv")
public class CsvUploadController {

    @Autowired
    private CsvImportService csvImportService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            csvImportService.importData(file);
            return ResponseEntity.ok("CSV file imported successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to import CSV file: " + e.getMessage());
        }
    }
}
