package com.demo.upimesh.model;

import jakarta.validation.constraints.NotBlank;

public class MeshPacket {

    @NotBlank
    private String packetId;

    @NotBlank
    private String ciphertext;

    public MeshPacket() {}

    public String getPacketId() { return packetId; }
    public void setPacketId(String packetId) { this.packetId = packetId; }

    public String getCiphertext() { return ciphertext; }
    public void setCiphertext(String ciphertext) { this.ciphertext = ciphertext; }
}