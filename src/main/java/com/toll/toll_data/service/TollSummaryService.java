package com.toll.toll_data.service;


import com.toll.toll_data.model.TollSummary;
import com.toll.toll_data.repository.TollSummaryRepository;
import com.toll.toll_data.repository.TransactionDetailRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class TollSummaryService {

    private final TransactionDetailRepository transactionRepo;
    private final TollSummaryRepository summaryRepo;
    
    @Autowired
    private final ExcelImportService excelImportService;

    public TollSummaryService(TransactionDetailRepository transactionRepo, TollSummaryRepository summaryRepo) {
        this.transactionRepo = transactionRepo;
        this.summaryRepo = summaryRepo;
        this.excelImportService = null;
    }

    public void generateTollSummaries() {
        excelImportService.updateTollSummary(); 
        }
    
    public List<TollSummary> findByNarrationAndVehicleRegNo(String narration,
            String vehicleRegNo) {

            List<TollSummary> results = new ArrayList<>();         
            if (vehicleRegNo != null && narration != null) {
                results = summaryRepo.findByNarrationAndVehicleRegNo(narration, vehicleRegNo);
            
            return summaryRepo.findAll().stream()
                    .filter(summary -> summary.getVehicleRegNo().equals(vehicleRegNo)
                            && summary.getNarration().equals(narration))
                    .collect(Collectors.toList());
        } else if (vehicleRegNo != null) {
            results= summaryRepo.findByVehicleRegNo(vehicleRegNo);
            return summaryRepo.findAll().stream()
                    .filter(summary -> summary.getVehicleRegNo().equals(vehicleRegNo))
                    .collect(Collectors.toList());
        } else if (narration != null) {
            results = summaryRepo.findByNarration(narration);
            return summaryRepo.findAll().stream()
                    .filter(summary -> summary.getNarration().equals(narration))
                    .collect(Collectors.toList());
        } else {
            return summaryRepo.findAll();
        }    }




    public List<TollSummary> findByVehicleRegNo(String vehicleRegNo) {
        // TODO Auto-generated method stub
        return summaryRepo.findAll().stream()
                .filter(summary -> summary.getVehicleRegNo().equals(vehicleRegNo))
                .collect(Collectors.toList());
    }




    public List<TollSummary> findByNarration(String narration) {
        // TODO Auto-generated method stub
        return summaryRepo.findAll().stream()
                .filter(summary -> summary.getNarration().equals(narration))
                .collect(Collectors.toList());
            }


    public List<TollSummary> findAll() {
        // TODO Auto-generated method stub
        return summaryRepo.findAll();}

         public Optional<TollSummary> updatePaymentDate(String narration, String vehicleRegNo, LocalDateTime newDate) {
            
            return summaryRepo.findByNarrationAndVehicleRegNo(narration, vehicleRegNo)
                .stream()
                .findFirst()
                .map(summary -> {
                    summary.setPaymentDate(newDate);
                    summary.setNarration(narration);
                    summary.setVehicleRegNo(vehicleRegNo);
                    summary.setZeroDebitCount(summaryRepo.updateZeroDebitCount(narration, vehicleRegNo,newDate));
                    summary.setNonZeroDebitCount(summaryRepo.updateNonZeroDebitCount(narration, vehicleRegNo, newDate));
                    
                    return summaryRepo.save(summary);
        });
    }

}
