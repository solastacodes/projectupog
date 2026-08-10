package com.demo.upimesh.service;

import javax.crypto.Cipher;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.demo.upimesh.crypto.ServerKeyHolder;
import com.demo.upimesh.model.Account;
import com.demo.upimesh.model.AccountRepository;
import com.demo.upimesh.model.MeshPacket;
import com.demo.upimesh.model.PaymentInstruction;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.UUID;


@Service
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    @Autowired private AccountRepository accounts;
    @Autowired private ServerKeyHolder serverKey;

    @PostConstruct
    public void seedAccounts() {
        if (accounts.count() == 0) {
            accounts.save(new Account("alice@demo", "Alice",   new BigDecimal("5000.00")));
            accounts.save(new Account("bob@demo",   "Bob",     new BigDecimal("1000.00")));
            accounts.save(new Account("carol@demo", "Carol",   new BigDecimal("2500.00")));
            accounts.save(new Account("dave@demo",  "Dave",    new BigDecimal("500.00")));
            log.info("Seeded 4 demo accounts");
        }
    }

    public MeshPacket createPacket(String senderVpa, String receiverVpa,
                                   BigDecimal amount, String pin) throws Exception {
        PaymentInstruction instruction = new PaymentInstruction(
                senderVpa,
                receiverVpa,
                amount,
                sha256Hex(pin)
        );

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, serverKey.getPublicKey());
        ObjectMapper mapper = new ObjectMapper();
        byte[] json = mapper.writeValueAsBytes(instruction);
        String ciphertext = Base64.getEncoder().encodeToString(cipher.doFinal(json));

        MeshPacket packet = new MeshPacket();
        packet.setPacketId(UUID.randomUUID().toString());
        packet.setCiphertext(ciphertext);
        return packet;
    }

    private String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}