package com.earn.earnmoney.payload.request;

import lombok.Data;

@Data
public class WithdrawRequest {
    private double amount;
    private String walletNumber;
    private String paymentMethod; // "ZAIN_CASH", "MASTERCARD"
    private String cardHolderName;
}
