System Architecture

```mermaid
flowchart TB
    subgraph SENSORS["Operating System Sensors & Inputs"]
        CS["Call Screening Service<br/>(Telephony Ingestion)"]
        PR["Phone State Receiver<br/>(Call State Lifecycle)"]
        OCR["Outgoing Call Receiver<br/>(Outbound Calls)"]
        SMS["SMS Receiver<br/>(SMS Received Broadcast, High Priority)"]
        NLS["Notification Listener Service<br/>(Notification Shade & VoIP Calls)"]
        PKG["Package Event Receiver<br/>(Application Installation Events)"]
    end

    subgraph INGESTION["Event Ingestion & Normalization Layer"]
        EN["Event Normalizer<br/>(Call Details & System Telemetry)"]
        NN["Notification Normalizer<br/>(Message & Action Parsing)"]
        IM["Interaction Hub<br/>(Unified Event Pipeline & State Coordinator)"]
    end

    subgraph IDENTITY["Identity & Contact Verification"]
        CIR["Caller Identity Resolver<br/>(Composite Verification)"]
        LCR["Local Contact Resolver<br/>(Device Address Book Query)"]
        EXT["External Directory Provider<br/>(Reputation & Category Assessment)"]
        CACHE["Identity & Reputation Cache<br/>(In-Memory Fast Path)"]
    end

    subgraph INTELLIGENCE["Contextual & Semantic Intelligence"]
        SIC["Message Intent Classifier<br/>(Pattern & Keyword Signals)"]
        URLA["URL Analyzer<br/>(Link & Domain Anomaly Detection)"]
        PNE["Phone Number Extractor<br/>(Callback Number Mismatch Detection)"]
        ACE["Attack Context Engine<br/>(Multi-Event Sliding Window Correlation)"]
        LLM["Semantic Intelligence Client<br/>(Remote Cloud LLM / Local Fallback)"]
    end

    subgraph RISK["Risk Evaluation & Fusion"]
        EFE["Evidence Fusion Engine<br/>(Multi-Signal Weighted Synthesis)"]
        RE["Risk Engine<br/>(0-100 Scoring & Severity Bands)"]
    end

    subgraph PROTECTION["Protection & Defensive Intervention"]
        PPE["Protection Policy Engine<br/>(Rules, Whitelist & Actions)"]
        SIM["Security Incident Manager<br/>(Incident Lifecycle & Recommendations)"]
        OV["Floating System Overlay<br/>(Draggable Window Overlay)"]
        EAM["Emergency Alarm System<br/>(Audible Alarm & Vibration Loop)"]
        FAS["Family Alert Service<br/>(Outbound Alert Dispatcher)"]
    end

    subgraph VCD["Biometric Voice Clone Defence"]
        MIC["Audio Capture Engine<br/>(Loudspeaker Room Audio / VoIP Sink)"]
        BUF["Audio Buffer & Window Slicer<br/>(16 kHz Mono PCM Framing)"]
        SPK["Speaker Verification Model<br/>(Neural Embedding Extraction)"]
        SPF["Anti-Spoofing Detection Model<br/>(Synthetic Artifact Detection)"]
        FUS["Score Fusion & Calibration<br/>(Per-Contact Baseline Calibration)"]
        ASR["Offline Speech Recognizer<br/>(On-Device Offline ASR)"]
    end

    subgraph PERSISTENCE["Data Storage & Cryptographic Vault"]
        DB[("Application Database<br/>(Interactions, Events, Risk, Incidents)")]
        VCD_DB[("Biometric Profile Store<br/>(Encrypted Voiceprints & Calibrations)")]
        VAULT["Hardware Keystore Vault<br/>(AES-256 GCM Keyring)"]
    end

    CS --> EN
    OCR --> EN
    NLS --> NN
    EN --> IM
    NN --> IM
    SMS --> IM
    PKG --> IM

    IM --> CIR
    CIR --> LCR
    CIR --> EXT
    EXT --> CACHE

    IM --> SIC
    IM --> URLA
    IM --> PNE
    SIC --> ACE
    URLA --> ACE
    PNE --> ACE
    IM -.-> LLM
    LLM -.-> ACE

    IM --> EFE
    CIR --> EFE
    SIC --> EFE
    URLA --> EFE
    PNE --> EFE
    ACE --> EFE
    LLM --> EFE
    EFE --> RE

    RE --> PPE
    RE --> SIM
    RE --> DB
    IM --> DB
    SIM --> DB

    PPE --> CS
    PPE --> OV
    RE --> FAS
    SMS -->|"Emergency Keyword"| EAM
    SMS -->|"Emergency Keyword"| OV

    MIC --> BUF
    BUF --> SPK
    BUF --> SPF
    SPK --> FUS```
    
    SPF --> FUS
    FUS --> OV
    BUF --> ASR
    ASR --> LLM
    FUS -.-> VCD_DB
    VAULT -.-> VCD_DB
