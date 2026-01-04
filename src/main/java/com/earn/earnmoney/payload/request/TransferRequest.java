package com.earn.earnmoney.payload.request;

import lombok.Data;

@Data
public class TransferRequest {
    private String identifier; // Email OR Referral Code
    private double amount;
}
