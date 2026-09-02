Offline Payment Relay System

A Spring Boot application that demonstrates how payment requests can be transported across a temporary offline network and delivered to a backend once network connectivity is available.

The project combines a backend transaction processor with a software-based device network. The simulator makes it possible to observe the complete lifecycle of a transaction—from creation and forwarding to verification and settlement—without requiring physical Bluetooth devices.

Table of Contents
Overview
Problem Statement
Solution
Running the Application
Transaction Lifecycle
Architecture
Security Model
Duplicate Delivery Protection
Project Structure
API Reference
Testing
Current Limitations
Possible Extensions
Overview

Conventional payment applications generally expect the initiating device to have access to a network service at the time a transaction is created.

This project investigates a different model: a transaction can be prepared locally and carried through a network of participating devices before reaching a server.

The intermediary devices act as transport nodes rather than payment processors. They forward protected transaction data without needing access to the underlying payment information.

A simulated network is included so that this behavior can be reproduced entirely on a local machine.

Problem Statement

Intermittent connectivity creates a challenge for applications that normally depend on immediate communication with a central server.

A useful offline transaction mechanism needs to address several concerns:

How can transaction data be carried while the originating device is disconnected?
How can messages be transferred through multiple intermediary devices?
How can sensitive information remain protected during forwarding?
What happens if the same transaction is delivered more than once?
How can the backend determine whether a received request is still valid?
How can account updates remain consistent when requests arrive concurrently?

This project provides a simplified implementation of these mechanisms for experimentation and demonstration.

Solution

The application separates the transaction process into two parts.

The device layer is responsible for temporarily holding and forwarding transaction packets.

The server layer becomes responsible for validation and settlement once a packet reaches a connected gateway.

The resulting workflow is:

Transaction Creation
        │
        ▼
Protected Message
        │
        ▼
Device-to-Device Forwarding
        │
        ▼
Connected Gateway
        │
        ▼
Backend Ingestion
        │
        ▼
Validation
        │
        ▼
Settlement
        │
        ▼
Ledger Entry


A software mesh is used instead of actual Bluetooth hardware, allowing different delivery scenarios to be reproduced consistently during development.

Running the Application
Requirements
JDK 17 or later
Java available through PATH or JAVA_HOME

Check the installed Java version:

java -version

Windows
mvnw.cmd spring-boot:run

macOS / Linux
./mvnw spring-boot:run


Once Spring Boot has started, visit:

http://localhost:8080


The application dashboard provides controls for creating transactions and operating the simulated network.

Running the test suite

Windows:

mvnw.cmd test


macOS / Linux:

./mvnw test

Transaction Lifecycle
1. Transaction creation

The dashboard allows a payment request to be generated with the required transaction information.

A unique identifier and timestamp are associated with the request before it is prepared for transmission.

The transaction is then converted into a protected packet suitable for forwarding.

2. Packet distribution

The packet is introduced into the simulated device network.

During a gossip cycle, devices exchange packets with other available nodes.

A node can therefore receive a packet, retain it temporarily, and make it available for subsequent forwarding.

A hop limit prevents packets from being propagated indefinitely.

3. Gateway delivery

A device configured with network access acts as a gateway.

When it receives pending packets, it submits them to the backend ingestion endpoint:

/api/bridge/ingest


The backend becomes the authoritative component for validating and processing the transaction.

4. Backend processing

The received packet passes through several checks before account state is changed.

Incoming Packet
      │
      ▼
Message Identification
      │
      ▼
Duplicate Check
      │
      ▼
Decryption
      │
      ▼
Timestamp Validation
      │
      ▼
Settlement
      │
      ▼
Ledger Update


This separation ensures that forwarding a packet does not itself constitute a completed payment.

Architecture
┌────────────────────────────────────────────────────────────┐
│                    OFFLINE DEVICE                          │
│                                                            │
│  Payment Request                                           │
│        │                                                   │
│        ▼                                                   │
│  Message Protection                                        │
│        │                                                   │
│        ▼                                                   │
│  MeshPacket                                                │
└──────────────────────────┬─────────────────────────────────┘
                           │
                           │ Device forwarding
                           ▼
                 ┌───────────────────┐
                 │  Device Network   │
                 │                   │
                 │  Node A → Node B  │
                 │       → Node C    │
                 └─────────┬─────────┘
                           │
                           ▼
                 ┌───────────────────┐
                 │  Gateway Device   │
                 │                   │
                 │ Internet Enabled  │
                 └─────────┬─────────┘
                           │
                           │ HTTP POST
                           ▼
┌────────────────────────────────────────────────────────────┐
│                    SPRING BOOT SERVER                     │
│                                                            │
│  Bridge Ingestion                                          │
│        │                                                   │
│        ▼                                                   │
│  Idempotency Check                                         │
│        │                                                   │
│        ▼                                                   │
│  Cryptographic Verification                                │
│        │                                                   │
│        ▼                                                   │
│  Freshness Validation                                      │
│        │                                                   │
│        ▼                                                   │
│  Settlement Service                                        │
│        │                                                   │
│        ├──────────────► Account Updates                    │
│        │                                                   │
│        └──────────────► Transaction Ledger                 │
└────────────────────────────────────────────────────────────┘

Security Model

Transaction information should not become readable simply because a packet passes through an intermediary node.

The application therefore protects the transaction before it enters the simulated network.

The implementation uses hybrid cryptography:

RSA is used for protecting the symmetric encryption key.
AES-GCM is used for the transaction payload.
The authenticated encryption mechanism allows modified ciphertext to be detected.
A timestamp is used to reject transactions outside the permitted validity period.

The intermediary nodes operate on the transport packet and do not perform settlement.

This is an educational implementation and should not be treated as a production-ready payment security architecture.

Duplicate Delivery Protection

A store-and-forward network can naturally produce multiple copies of the same packet.

For example, different gateway devices may independently submit a packet that originated from the same transaction.

Processing every copy as a new payment would result in an incorrect account balance.

The backend therefore performs an idempotency check before settlement.

                 Received Packet
                       │
                       ▼
                Calculate Identity
                       │
                       ▼
                 Already Seen?
                  /          \
                Yes           No
                 │             │
                 ▼             ▼
              Ignore       Register
                              │
                              ▼
                           Process
                              │
                              ▼
                           Settle


The atomic claim operation ensures that concurrent submissions cannot all proceed as independent transactions.

Project Structure

The application follows the existing Spring Boot organization, with application code under src/main and automated tests under src/test.

src/
├── main/
│   ├── java/
│   │   └── ...
│   └── resources/
│       └── ...
└── test/
    └── java/
        └── ...


The main components cover:

REST endpoints
Transaction processing
Cryptographic operations
Mesh simulation
Gateway ingestion
Account management
Ledger persistence
Idempotency handling
API Reference
Bridge ingestion
POST /api/bridge/ingest


Accepts a transaction packet submitted by a connected gateway.

The backend validates the packet and proceeds with processing only when the request passes the required checks.

Testing

The test suite covers the main transaction-processing paths.

Examples include:

Successful transaction processing
Invalid transaction rejection
Duplicate packet handling
Concurrent packet delivery
Account balance changes
Ledger creation

The concurrency test is particularly important because multiple copies of a packet can arrive at the backend at nearly the same time.

Current Limitations

This project is intended as a proof-of-concept rather than a real payment platform.

The current implementation does not provide:

Physical Bluetooth communication
Integration with actual UPI infrastructure
Production banking settlement
Hardware-backed key storage
Production-grade device authentication
Real-world fraud prevention
Distributed production deployment
Regulatory compliance

The mesh network is intentionally simplified so that its behavior can be demonstrated locally.

Possible Extensions

The system could be expanded with:

A real Android client
Bluetooth Low Energy communication
Persistent offline queues
Device identity and authentication
Delivery acknowledgements
Improved packet expiration
Network partition testing
Retry policies
Monitoring and audit trails
Integration with a payment sandbox
Disclaimer

This repository is a technical demonstration of offline message forwarding and backend transaction processing.

It is not connected to real UPI infrastructure and should not be used for real financial transactions.