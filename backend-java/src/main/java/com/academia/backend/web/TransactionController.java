package com.academia.backend.web;

import com.academia.backend.domain.TransactionStatus;
import com.academia.backend.dto.TransactionDto;
import com.academia.backend.dto.in.CreateTransactionIn;
import com.academia.backend.dto.in.UpdateTransactionIn;
import com.academia.backend.service.JwtService;
import com.academia.backend.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Gestión de transacciones y pagos")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtService jwtService;

    public TransactionController(TransactionService transactionService, JwtService jwtService) {
        this.transactionService = transactionService;
        this.jwtService = jwtService;
    }

    @PostMapping
    @Operation(summary = "Crear transacción", description = "Iniciar proceso de pago para un curso")
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody CreateTransactionIn input,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtService.extractUserIdFromHeader(authHeader);
        TransactionDto transaction = transactionService.createTransaction(input, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener transacción por ID")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable UUID id) {
        // Buscar por ID no está implementado, usar getTransactionByReference
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Obtener transacción por referencia")
    public ResponseEntity<TransactionDto> getTransactionByReference(@PathVariable String reference) {
        TransactionDto transaction = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/my-transactions")
    @Operation(summary = "Obtener mis transacciones", description = "Lista de transacciones del usuario autenticado")
    public ResponseEntity<Page<TransactionDto>> getMyTransactions(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = jwtService.extractUserIdFromHeader(authHeader);
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = transactionService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener transacciones de un usuario", description = "Solo admin")
    public ResponseEntity<Page<TransactionDto>> getUserTransactions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = transactionService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Obtener transacciones de un curso", description = "Para profesores y admin")
    public ResponseEntity<Page<TransactionDto>> getCourseTransactions(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = transactionService.getCourseTransactions(courseId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    @Operation(summary = "Obtener todas las transacciones", description = "Solo admin")
    public ResponseEntity<Page<TransactionDto>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener transacciones por estado", description = "Solo admin")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByStatus(
            @PathVariable TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = transactionService.getTransactionsByStatus(status, pageable);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar transacción", description = "Usado por webhooks de pasarelas de pago")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionIn input) {

        TransactionDto transaction = transactionService.updateTransaction(id, input);
        return ResponseEntity.ok(transaction);
    }

    // Endpoints de reportes

    @GetMapping("/reports/total-revenue")
    @Operation(summary = "Obtener ingresos totales", description = "Solo admin")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        BigDecimal revenue = transactionService.getTotalRevenue();
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/reports/course-revenue/{courseId}")
    @Operation(summary = "Obtener ingresos de un curso", description = "Para profesores y admin")
    public ResponseEntity<BigDecimal> getCourseRevenue(@PathVariable UUID courseId) {
        BigDecimal revenue = transactionService.getCourseRevenue(courseId);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/reports/revenue-between")
    @Operation(summary = "Obtener ingresos en un rango de fechas", description = "Solo admin")
    public ResponseEntity<BigDecimal> getRevenueBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        BigDecimal revenue = transactionService.getRevenueBetween(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/reports/completed-between")
    @Operation(summary = "Obtener transacciones completadas en rango de fechas", description = "Solo admin")
    public ResponseEntity<List<TransactionDto>> getCompletedTransactionsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        List<TransactionDto> transactions = transactionService.getCompletedTransactionsBetween(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
}
