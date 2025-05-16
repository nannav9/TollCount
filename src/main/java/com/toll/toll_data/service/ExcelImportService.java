package com.toll.toll_data.service;

import com.toll.toll_data.model.TransactionDetail;
import com.toll.toll_data.repository.TransactionDetailRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelImportService {

    private final TransactionDetailRepository transactionRepository;

    public ExcelImportService(TransactionDetailRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

 public void importData(MultipartFile file) {
    try {
        List<TransactionDetail> transactions = parseExcelFile(file.getInputStream());
        int count = 0;

        for (TransactionDetail txn : transactions) {
            try {
                transactionRepository.save(txn);
                count++;
            } catch (Exception e) {
                System.err.println("Failed to insert transaction at row #" + (count + 10)); // +10 if skipping 9 rows
                System.err.println("Transaction: " + txn);
                e.printStackTrace();
            }
        }

        System.out.println("Successfully inserted: " + count + " out of " + transactions.size());
    } catch (Exception e) {
        throw new RuntimeException("Failed to store Excel data: " + e.getMessage(), e);
    }
}



    private List<TransactionDetail> parseExcelFile(InputStream is) throws Exception {
        List<TransactionDetail> transactionList = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        // Skip first 9 rows
        int skipRows = 8;
        for (int i = 0; i < skipRows && rows.hasNext(); i++) {
            rows.next();
        }

        while (rows.hasNext()) {
            Row currentRow = rows.next();
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

        workbook.close();
        return transactionList;
    }

    private String getCellValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return "";

        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            } else {
                return String.valueOf(cell.getNumericCellValue());
            }
        } else if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else {
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
            // Optionally log warning
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
    if (value == null || value.trim().isEmpty()) {
        return BigDecimal.ZERO;
    }

    try {
        value = value.toUpperCase().replace(",", "").trim(); // Remove commas and normalize

        boolean isCredit = value.endsWith("CR");
        boolean isDebit = value.endsWith("DR");

        // Remove CR/DR if present
        value = value.replace("CR", "").replace("DR", "").trim();

        BigDecimal amount = new BigDecimal(value);
        if (isDebit) {
            amount = amount.negate(); // make negative
        }

        return amount;
    } catch (NumberFormatException e) {
        return BigDecimal.ZERO;
    }
}


    private Double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0.0;
        }

        try {
            amountStr = amountStr.replace(",", "").trim(); // Remove commas
            return Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
