# VOCIS — Voice & Communication Intelligence System

VOCIS is an advanced, real-time cyber defense platform for Android designed to detect, screen, and counteract social engineering attacks, digital arrest fraud, OTP theft, and deepfake voice cloning.

## Role A: Platform Foundation, Data & Sensors

This repository contains the platform foundation and core architecture implemented for **Role A**:

- **Build System**: Gradle 8.7+, Kotlin 2.0, KSP 2.0, version catalogs (`gradle/libs.versions.toml`), ABI packaging (`arm64-v8a`, `armeabi-v7a`, `x86_64`).
- **Core Domain & Math**: `RiskLevel`, `EventType`, `IncidentType`, `IncidentStatus`, `ProtectionAction`, 16 kHz audio constants, and vector operations (`VectorMath`: L2 norm, dot product, cosine similarity).
- **Dual Room Database**:
  - `app.db` (9 entities): `interactions`, `security_events`, `security_incidents`, `caller_identities`, `protection_policies`, `attack_contexts`, `audit_logs`, `family_contacts`, `threat_rules`.
  - `vcd.db` (2 entities): `contact_voiceprints`, `vcd_call_history`.
- **Hardware Keystore Cryptographic Vault**: `BiometricCryptoVault` using AndroidKeyStore AES-256 GCM (`vcd_voiceprint_master_key`) with 12-byte prepended IV for 256-dim float embeddings.
- **Pure-Function Normalizers**: `EventNormalizer` and `NotificationNormalizer` with detection for financial packages and remote desktop tools (AnyDesk, TeamViewer, RustDesk).
- **Platform Sensor Layer**: `CallScreeningSensor` (<=1800ms fail-open budget), `TelephonyStateMonitor`, `OutgoingCallMonitor`, `SmsSensor` (priority 999 with multipart PDU assembly), `NotificationSensor`, and `PackageEventMonitor`.
- **System Permissions**: Full Android Manifest declarations for all 18 required telecom, SMS, audio, overlay, and foreground service permissions.

## Documentation
Complete architectural specifications, data flows, and rebuild plans are located in the `docs/` directory.
