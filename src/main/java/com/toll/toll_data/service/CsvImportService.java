package com.toll.toll_data.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class CsvImportService {

    public void importData(MultipartFile file) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Assuming CSV columns separated by commas
                String[] fields = line.split(",");

                // Example: Print fields (You can replace this with saving to DB)
                System.out.println("CSV Fields: ");
                for (String field : fields) {
                    System.out.print(field + " | ");
                }
                System.out.println();

                // TODO: map fields to your model/entity and save to DB here
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import CSV file: " + e.getMessage());
        }
    }
}
