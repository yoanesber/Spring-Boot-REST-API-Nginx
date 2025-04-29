package com.yoanesber.rest_api_nginx_backend.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Required for Jackson deserialization when receiving JSON requests.
@AllArgsConstructor // Helps create DTO objects easily (useful when converting from entities).
public class RequestDTO {
    private String orderId; // Order identifier (linked to Orders table)
    private BigDecimal amount; // Payment amount
    private String currency; // e.g., USD, EUR
    private String paymentMethod; // e.g., CREDIT_CARD, PAYPAL, BANK_TRANSFER
    private String cardNumber; // Card number (for credit card payments)
    private String cardHolderName; // Cardholder name (for credit card payments)
    private String cardExpiryDate; // Card expiry date (for credit card payments)
    private String cardCvv; // Card CVV (for credit card payments)
}