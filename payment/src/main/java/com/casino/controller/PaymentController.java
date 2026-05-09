package com.casino.controller;

import com.casino.dto.CreatePaymentDto;
import com.casino.dto.PaymentDto;
import com.casino.dto.PaymentProviderWebhookDto;
import com.casino.model.PaymentType;
import com.casino.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/deposits")
    public ResponseEntity<PaymentDto> createDeposit(@RequestBody CreatePaymentDto request) {
        PaymentDto payment = paymentService.createPayment(request, PaymentType.DEPOSIT);

        return ResponseEntity.ok(payment);
    }

    @PostMapping("/withdrawals")
    public ResponseEntity<PaymentDto> createWithdrawal(@RequestBody CreatePaymentDto request) {
        PaymentDto payment = paymentService.createPayment(request, PaymentType.WITHDRAWAL);

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable Long id) {
        PaymentDto payment = paymentService.getPaymentById(id);

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<PaymentDto>> getPlayerPayments(@PathVariable Long playerId) {
        List<PaymentDto> payments = paymentService.getPaymentsByPlayerId(playerId);

        return ResponseEntity.ok(payments);
    }

    @PostMapping("/provider/webhook")
    public ResponseEntity<Void> handleProviderWebhook(@RequestBody PaymentProviderWebhookDto webhookDto) {
        paymentService.handleProviderWebhook(webhookDto);

        return ResponseEntity.ok().build();
    }
}