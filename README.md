# Salim - Native Android Notes Application

**Salim** (`com.salim.notes`) is an offline-first, native Android notes application built with Kotlin and Jetpack Compose. It features a glassmorphic aesthetic matching the three-screen reference design:
1. **Your Notes**: Staggered 2-column grid displaying mint checklists, frosted text notes, lavender voice notes with audio waveforms, photo collections with peek cards, and smart attachment cards.
2. **Thought of the Day (Collection Detail)**: 3D perspective stacked cards with spring gesture navigation, category filter chips, and bottom "+ Create new" pill.
3. **Good Morning**: Dynamic time-of-day greeting, tilted featured note card, overlapping highlights photo card, and real Daily Overview statistics.

---

## Architecture & Features

- **100% Native Kotlin & Jetpack Compose**: Pure Compose UI using custom glass surfaces (`RenderEffect` blur on API 31+ with translucent fallbacks), spring animations, and edge-to-edge window insets.
- **Offline-First Room Persistence**: Local SQLite database with FTS4 search, cascade foreign keys, and reactive `Flow` streams.
- **Privacy & Security**:
  - Zero `INTERNET` permission — no tracking, no external server calls.
  - Hardware-backed AES-256-GCM encryption with Android KeyStore for locked notes and biometric protection.
- **Voice Notes**: In-place recording with AAC MPEG-4 encoding, real 64-bar amplitude sampling, and speed-controlled playback.
- **Smart Attachments**: Photo picker (zero storage permissions), camera capture via `FileProvider`, and `PdfRenderer` for multi-page viewing and thumbnails.
- **Exact Reminders**: Integrated with `AlarmManager` exact alarms, notification channels, and boot rescheduling receivers.
- **Backup & Restore**: Full database and attachment packaging with optional PBKDF2-HMAC-SHA256 + AES-GCM encryption, as well as Markdown ZIP export.

---

## Build & Release Instructions

### Debug Build
```bash
gradle assembleDebug
```

### Release Build
```bash
gradle assembleRelease
gradle bundleRelease
```

### Running Unit Tests
```bash
gradle :app:testDebugUnitTest
```
