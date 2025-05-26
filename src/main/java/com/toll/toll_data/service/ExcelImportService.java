package com.toll.toll_data.service;

import com.toll.toll_data.model.TransactionDetail;
import com.toll.toll_data.model.TollSummary;
import com.toll.toll_data.repository.TransactionDetailRepository;
import com.toll.toll_data.repository.TollSummaryRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);

    private final TransactionDetailRepository transactionRepository;
    private final TollSummaryRepository tollSummaryRepository;

    public ExcelImportService(TransactionDetailRepository transactionRepository, TollSummaryRepository tollSummaryRepository) {
        this.transactionRepository = transactionRepository;
        this.tollSummaryRepository = tollSummaryRepository;
    }

    public void importData(MultipartFile file) {
        try {
            List<TransactionDetail> transactions = parseExcelFile(file.getInputStream());
            int successCount = 0;

            for (TransactionDetail txn : transactions) {
                try {
                    transactionRepository.save(txn);
                    successCount++;
                } catch (Exception e) {
                    logger.error("Failed to insert transaction: {}", txn, e);
                }
            }

            logger.info("Inserted {} out of {} transactions successfully", successCount, transactions.size());

            // Update toll summary after transactions inserted
            updateTollSummary();

        } catch (Exception e) {
            throw new RuntimeException("Failed to store Excel data: " + e.getMessage(), e);
        }
    }

    /**
     * Update TollSummary based on distinct narration and vehicleRegNo where debit = 0.
     * This method is transactional to keep data consistent.
     */
    @Transactional
    public void updateTollSummary() {
        // Fetch distinct narration and vehicleRegNo where debit = 0
        List<Object[]> distinctEntries = transactionRepository.findDistinctNarrationAndVehicle();

        for (Object[] entry : distinctEntries) {
            // Expecting [narration, vehicleRegNo] per query definition
            String narration = (String) entry[0];
            String vehicleRegNo = (String) entry[1];

            List<TollSummary> optionalSummary = tollSummaryRepository.findByNarrationAndVehicleRegNo(narration, vehicleRegNo);

            TollSummary summary = optionalSummary.stream().findFirst()
            .orElseGet(() -> {
            TollSummary newSummary = new TollSummary();
            newSummary.setNarration(narration);
            newSummary.setVehicleRegNo(vehicleRegNo);
            newSummary.setZeroDebitCount(0);
            newSummary.setNonZeroDebitCount(0);
        // paymentDate left null for now
        return newSummary;
            });

            // Set paymentDate to Jan 1, 2025 if null
            if (summary.getPaymentDate() == null) {
                summary.setPaymentDate(LocalDateTime.of(2025, 1, 1, 0, 0));
            }

            long zeroDebitCount = transactionRepository.countByNarrationAndVehicleRegNoAndDebitIsZero(narration, vehicleRegNo);
            long nonZeroDebitCount = transactionRepository.countByNarrationAndVehicleRegNoAndDebitNonZero(narration, vehicleRegNo);

            summary.setZeroDebitCount((int) zeroDebitCount);
            summary.setNonZeroDebitCount((int) nonZeroDebitCount);

            tollSummaryRepository.save(summary);
        }
    }

    private List<TransactionDetail> parseExcelFile(InputStream is) throws Exception {
        List<TransactionDetail> transactionList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip header rows (first 8)
            int skipRows = 8;
            for (int i = 0; i < skipRows && rows.hasNext(); i++) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (isRowEmpty(currentRow)) {
                    continue; // skip empty rows
                }

                TransactionDetail transaction = new TransactionDetail();

                transaction.setBarCode(getCellValue(currentRow, 0));
                transaction.setTagId(getCellValue(currentRow, 1));
                transaction.setVehicleRegNo(getCellValue(currentRow, 2));
                transaction.setTxnId(getCellValue(currentRow, 3));
                transaction.setLaneId(getCellValue(currentRow, 4));
                transaction.setValueDate(parseDate(getCellValue(currentRow, 5)));
                transaction.setNarration(getCellValue(currentRow, 6));
                transaction.setCredit(parseAmount(getCellValue(currentRow, 7)));
                transaction.setDebit(parseAmount(getCellValue(currentRow, 8)));
                transaction.setWalletBalance(parseBigDecimal(getCellValue(currentRow, 9)));
                transaction.setMasterBalance(parseBigDecimal(getCellValue(currentRow, 10)));
                transaction.setSdBalance(parseBigDecimal(getCellValue(currentRow, 11)));

                transactionList.add(transaction);
            }
        }

        return transactionList;
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c <= 11; c++) { // check 12 columns
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(row, c).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                } else {
                    double d = cell.getNumericCellValue();
                    if (d == (long) d) {
                        return String.valueOf((long) d);
                    }
                    return String.valueOf(d);
                }
            case STRING:
                return cell.getStringCellValue().trim();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                    } else {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return cell.toString().trim();
        }
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return LocalDateTime.parse(dateStr.trim(), formatter);
        } catch (Exception e) {
            logger.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            value = value.toUpperCase().replace(",", "").trim();
            boolean isCredit = value.endsWith("CR");
            boolean isDebit = value.endsWith("DR");

            value = value.replace("CR", "").replace("DR", "").trim();

            BigDecimal amount = new BigDecimal(value);
            if (isDebit) {
                amount = amount.negate();
            }

            return amount;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse BigDecimal: {}", value);
            return BigDecimal.ZERO;
        }
    }

    private Double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0.0;
        }

        try {
            amountStr = amountStr.replace(",", "").trim();
            return Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse amount: {}", amountStr);
            return 0.0;
        }
    }
}
