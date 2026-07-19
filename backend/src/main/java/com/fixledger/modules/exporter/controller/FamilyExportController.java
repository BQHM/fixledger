package com.fixledger.modules.exporter.controller;

import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.exporter.service.CsvExportFile;
import com.fixledger.modules.exporter.service.FamilyExportService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/families/{familyId}/exports")
public class FamilyExportController {

  private static final MediaType CSV_MEDIA_TYPE =
      new MediaType("text", "csv", StandardCharsets.UTF_8);

  private final FamilyExportService familyExportService;

  public FamilyExportController(FamilyExportService familyExportService) {
    this.familyExportService = familyExportService;
  }

  @GetMapping("/devices.csv")
  public ResponseEntity<byte[]> exportDevices(@PathVariable Long familyId) {
    CsvExportFile file = familyExportService.exportDevices(
        CurrentUserContext.getUserId(),
        familyId
    );
    return csvResponse(file);
  }

  @GetMapping("/maintenance-costs.csv")
  public ResponseEntity<byte[]> exportMaintenanceCosts(
      @PathVariable Long familyId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate endDate
  ) {
    CsvExportFile file = familyExportService.exportMaintenanceCosts(
        CurrentUserContext.getUserId(),
        familyId,
        startDate,
        endDate
    );
    return csvResponse(file);
  }

  private ResponseEntity<byte[]> csvResponse(CsvExportFile file) {
    return ResponseEntity.ok()
        .contentType(CSV_MEDIA_TYPE)
        .contentLength(file.content().length)
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(file.filename()))
        .body(file.content());
  }

  private String contentDisposition(String filename) {
    return ContentDisposition.attachment()
        .filename(filename, StandardCharsets.UTF_8)
        .build()
        .toString();
  }
}
