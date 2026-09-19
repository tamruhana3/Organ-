# NetForge

> **Private routing, plainly done.**
> *Crafted by Axiom Collective — Version 1.0*

[![Platform](https://img.shields.io/badge/Platform-Android_7.0+_(API_24+)-7C5CFF.svg)](https://android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean--Room_VpnService-34D399.svg)](#architecture)
[![UI](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-FF6B4A.svg)](#design-system)
[![Security](https://img.shields.io/badge/Crypto-Argon2id_+_AES--256--GCM-7C5CFF.svg)](#vault-format-nfg)

---

## Overview

**NetForge** is a high-performance, clean-room Android VPN tunnel client built entirely from scratch with Kotlin and Jetpack Compose. Unlike legacy injector tools, NetForge features an original, zero-tracking routing core designed for absolute reliability, security, and censorship resistance.

### Key Capabilities
- **Real Native VpnService Engine:** Direct TUN file descriptor packet processing loop bridging IP frames with authenticated SSH-2.0 tunnels.
- **5 Encapsulation Transports:**
  - **Direct:** Raw TCP socket to remote SSH gateway.
  - **Wrapped:** TLS-wrapped stream (stunnel4) with custom SNI masking.
  - **WrappedPlus:** TLS wrap + HTTP CONNECT proxy + customizable payload injector.
  - **Slow:** Covert DNS tunneling across recursive nameservers.
  - **Live:** High-throughput WebSocket stream with bidirectional frame pumping.
- **Sealed `.nfg` Vault Format:** Cryptographically hardened configuration export/import powered by **Argon2id** (RFC 9106) key derivation and **AES-256-GCM** authenticated encryption with hardware device binding and tamper verification.
- **Diagnostics Bench:** Integrated network telemetry suite including Egress IP detection, RTT sparkline pingline, session stability index, TLS certificate inspection, and interface route tracing.
- **Dusk Design System:** OLED-optimized dark theme with dynamic status rings, fluid haptics, and Material 3 components.

---

## Design System

NetForge defaults to the **"Dusk"** palette:
- **Accent:** `#7C5CFF` (Violet)
- **Ember:** `#FF6B4A` (Active live glow)
- **Ink:** `#0A0A0E` (Deep background canvas)
- **Paper:** `#16161D` (Card surface)
- **Paper2:** `#1E1E28` (Elevated elements)
- **Moss:** `#34D399` (Success indicator)
- **Rust:** `#F87171` (Halt / error)
- **Amber:** `#FBBF24` (Opening state)

---

## Architecture

```
[Android OS Applications]
          │ (IPv4 Packets)
          ▼
   [TUN Interface] (NetForgeVpnService)
          │
          ▼
   [TunForwarder] (ByteBuffer IO Loop)
          │
          ▼
   [Local SOCKS5 Proxy] (127.0.0.1:10808)
          │
          ▼
   [TunnelEngine]
   ├── DirectTunnel (SSH TCP)
   ├── WrappedTunnel (TLS Socket + SSH)
   ├── WrappedPlusTunnel (TLS + HTTP Injector + SSH)
   ├── SlowTunnel (DNS Covert Channel)
   └── LiveTunnel (WebSocket Stream)
          │
          ▼
[NetForge Gateway Server (Ubuntu 22.04)]
```

---

## Vault Format (`.nfg`)

Profiles are exported and shared as binary `.nfg` vaults:
1. **Magic Header:** `NFOR` (4 bytes ASCII)
2. **Version:** `0x01` (1 byte)
3. **Flags:** Reserved (1 byte)
4. **Argon2id Salt:** 16 cryptographically random bytes
5. **AES-GCM IV:** 12 random nonce bytes
6. **Encrypted Payload:** Gzip-compressed INI configuration string
7. **Authentication Tag:** 16-byte GCM tag

Passphrases are evaluated in real-time with an interactive complexity meter (`Weak`, `Fair`, `Strong`, `Fortified`). Import policies support hardware device locking, root checks, emulator prevention, and import limits.

---

## Server Deployment

To configure a compatible server backend on Ubuntu 22.04 LTS, consult the comprehensive guide:

📖 **[SERVER_GUIDE.md](SERVER_GUIDE.md)**

---

## Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK with Platform 34 and Build Tools 34.0.0
- Gradle 8.x

### Build Debug APK
```bash
gradle assembleDebug
```
The output APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

### Run Local Unit Tests
```bash
gradle :app:testDebugUnitTest
```

---

## License

Crafted by **Axiom Collective**. Released for sovereign and private network routing.
