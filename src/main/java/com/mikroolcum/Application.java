package com.mikroolcum;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RestController
@CrossOrigin
public class Application {
  public static void main(String[] args) { SpringApplication.run(Application.class, args); }

  public record Measurement(String code, String value) {}
  public record ReportRequest(String serialNumber, String date, List<Measurement> measurements) {}

  private static final Map<String,Integer> ROWS = Map.ofEntries(
    Map.entry("K31",13), Map.entry("K34",14), Map.entry("K41",15),
    Map.entry("K43",16), Map.entry("B",17), Map.entry("K32",18),
    Map.entry("K33",19), Map.entry("K36",20), Map.entry("A",21),
    Map.entry("C",22), Map.entry("D",23), Map.entry("H",24),
    Map.entry("K35",25), Map.entry("K42",26), Map.entry("K44",27)
  );

  @GetMapping("/api/health")
  public Map<String,String> health() {
    return Map.of("status","ok","template","sablon.xls");
  }

  @PostMapping(value="/api/report", consumes=MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<byte[]> createReport(@RequestBody ReportRequest request) throws Exception {
    String serial = request.serialNumber() == null ? "" : request.serialNumber().trim();
    if (serial.isBlank()) return ResponseEntity.badRequest().body("Seri numarası zorunludur.".getBytes("UTF-8"));

    byte[] bytes;
    try (InputStream in = new ClassPathResource("sablon.xls").getInputStream();
         HSSFWorkbook book = new HSSFWorkbook(in);
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Sheet sheet = book.getSheet("KGY ÖLÇÜM RAPORU");
      if (sheet == null) throw new IllegalStateException("Şablonda KGY ÖLÇÜM RAPORU sayfası bulunamadı.");

      // F1: tarih. Hücrenin mevcut biçimini koru.
      if (request.date() != null && !request.date().isBlank()) {
        LocalDate date = LocalDate.parse(request.date());
        Cell dateCell = getOrCreate(sheet, 0, 5);
        dateCell.setCellValue(java.sql.Date.valueOf(date));
        CellStyle style = book.createCellStyle();
        style.cloneStyleFrom(dateCell.getCellStyle());
        style.setDataFormat(book.createDataFormat().getFormat("dd.mm.yyyy"));
        dateCell.setCellStyle(style);
      }

      // F13:F27: ölçüm değerleri. H/K35/K44 gibi mastar sonuçları OK/RET olabilir.
      if (request.measurements() != null) {
        for (Measurement m : request.measurements()) {
          if (m == null || m.code() == null || m.value() == null) continue;
          Integer rowNum = ROWS.get(m.code().trim().toUpperCase(Locale.ROOT));
          if (rowNum == null) continue;
          String value = m.value().trim();
          if (value.isEmpty()) continue;
          Cell cell = getOrCreate(sheet, rowNum - 1, 5);
          if (value.equalsIgnoreCase("OK") || value.equalsIgnoreCase("RET")) {
            cell.setCellValue(value.toUpperCase(Locale.ROOT));
          } else {
            String normalized = value.replace(',', '.');
            cell.setCellValue(Double.parseDouble(normalized));
          }
        }
      }

      // F11 is intentionally untouched: the original formula derives the serial from the filename.
      book.setForceFormulaRecalculation(true);
      book.write(out);
      bytes = out.toByteArray();
    }

    String safe = serial.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_");
    String filename = safe + ".xls";
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
        java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8).replace("+","%20"))
      .body(bytes);
  }

  private static Cell getOrCreate(Sheet sheet, int rowIndex, int colIndex) {
    Row row = sheet.getRow(rowIndex);
    if (row == null) row = sheet.createRow(rowIndex);
    Cell cell = row.getCell(colIndex);
    if (cell == null) cell = row.createCell(colIndex);
    return cell;
  }
}
