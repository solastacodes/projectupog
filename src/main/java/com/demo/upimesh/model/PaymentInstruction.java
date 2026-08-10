package com.demo.upimesh.model;

import java.math.BigDecimal;

public class PaymentInstruction {

    private String senderVpa;
    private String receiverVpa;
    private BigDecimal amount;
    private String pinHash;

    public PaymentInstruction() {}

    public PaymentInstruction(String senderVpa, String receiverVpa, BigDecimal amount, String pinHash) {
        this.senderVpa = senderVpa;
        this.receiverVpa = receiverVpa;
        this.amount = amount;
        this.pinHash = pinHash;
    }

    public String getSenderVpa() { return senderVpa; }
    public void setSenderVpa(String senderVpa) { this.senderVpa = senderVpa; }

    public String getReceiverVpa() { return receiverVpa; }
    public void setReceiverVpa(String receiverVpa) { this.receiverVpa = receiverVpa; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
}