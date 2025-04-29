package com.yoanesber.rest_api_nginx_backend.controller;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoanesber.rest_api_nginx_backend.dto.RequestDTO;
import com.yoanesber.rest_api_nginx_backend.dto.ResponseDTO;
import com.yoanesber.rest_api_nginx_backend.entity.CustomHttpResponse;


@RestController
@RequestMapping("/api/v1")
public class ApiController {

    @PostMapping("/post-order-payment")
    public ResponseEntity<CustomHttpResponse> postAction(@RequestBody RequestDTO requestDTO) {
        // Check if the order payment was created successfully.
        if (requestDTO == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Failed to create order payment", null));
        }

        // Return a successful response with the created order payment details.
        // The response includes the order ID, transaction ID, payment status, amount, currency, payment method, and creation time.
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CustomHttpResponse(HttpStatus.CREATED.value(),
            "Order payment created successfully", 
            new ResponseDTO(requestDTO.getOrderId(), 
                "TXN" + System.currentTimeMillis(),
                "PENDING", 
                requestDTO.getAmount(),
                requestDTO.getCurrency(),
                requestDTO.getPaymentMethod(),
                requestDTO.getCardNumber(),
                requestDTO.getCardHolderName(),
                requestDTO.getCardExpiryDate(),
                requestDTO.getCardCvv(),
                Instant.now())));
    }

    @GetMapping("/get-order-payment")
    public ResponseEntity<CustomHttpResponse> getAction() {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setOrderId("12345");
        responseDTO.setTransactionId("67890");
        responseDTO.setPaymentStatus("SUCCESS");
        responseDTO.setAmount(new BigDecimal("100.00"));
        responseDTO.setCurrency("USD");
        responseDTO.setPaymentMethod("CREDIT_CARD");
        responseDTO.setCardNumber("**** **** **** 1234");
        responseDTO.setCardHolderName("John Doe");
        responseDTO.setCardExpiryDate("12/25");
        responseDTO.setCardCvv("123");
        responseDTO.setCreatedAt(Instant.now());

        return ResponseEntity.status(HttpStatus.OK)
            .body(new CustomHttpResponse(HttpStatus.OK.value(), 
                "Order payment retrieved successfully", responseDTO));
    }

    @PutMapping("/update-order-payment/{orderId}")
    public ResponseEntity<CustomHttpResponse> putAction(@PathVariable String orderId, @RequestBody RequestDTO requestDTO) {

        // Check if the order ID is valid.
        if (orderId == null || orderId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomHttpResponse(HttpStatus.BAD_REQUEST.value(), 
                    "Invalid order ID", null));
        }
        
        // Check if the order payment was updated successfully.
        if (requestDTO == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Failed to update order payment", null));
        }

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setOrderId(orderId);
        responseDTO.setTransactionId("TXN" + System.currentTimeMillis());
        responseDTO.setPaymentStatus("SUCCESS");
        responseDTO.setAmount(requestDTO.getAmount());
        responseDTO.setCurrency(requestDTO.getCurrency());
        responseDTO.setPaymentMethod(requestDTO.getPaymentMethod());
        responseDTO.setCardNumber(requestDTO.getCardNumber());
        responseDTO.setCardHolderName(requestDTO.getCardHolderName());
        responseDTO.setCardExpiryDate(requestDTO.getCardExpiryDate());
        responseDTO.setCardCvv(requestDTO.getCardCvv());
        responseDTO.setCreatedAt(Instant.now());

        return ResponseEntity.status(HttpStatus.OK)
            .body(new CustomHttpResponse(HttpStatus.OK.value(), 
                "Order payment updated successfully", responseDTO));
    }
}
