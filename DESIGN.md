# 🎨 Vult — Design System & UI/UX Specification

This document defines the visual design language, design tokens, component architecture, layout principles, and UX motion patterns of **Vult**. It serves as a unified reference for product designers, frontend engineers, and contributors maintaining Material Design 3 consistency across the application.

---

## 📑 Table of Contents

- [1. Design Philosophy & Visual Identity](#1-design-philosophy--visual-identity)
- [2. Color System & Design Tokens](#2-color-system--design-tokens)
  - [2.1 Color Roles & Palette](#21-color-roles--palette)
  - [2.2 Dark Mode Token Matrix](#22-dark-mode-token-matrix)
  - [2.3 Light Mode Token Matrix](#23-light-mode-token-matrix)
  - [2.4 Dynamic Color (Material You)](#24-dynamic-color-material-you)
- [3. Typography & Text Hierarchy](#3-typography--text-hierarchy)
- [4. Component Design Specifications](#4-component-design-specifications)
  - [4.1 Security Lock Screen Overlay](#41-security-lock-screen-overlay)
  - [4.2 Alphanumeric Security Keypad](#42-alphanumeric-security-keypad)
  - [4.3 Vault Dashboard Screen](#43-vault-dashboard-screen)
  - [4.4 Permissions & Diagnostics Cards](#44-permissions--diagnostics-cards)
- [5. Iconography & Asset Guidelines](#5-iconography--asset-guidelines)
- [6. Motion & Micro-Interactions](#6-motion--micro-interactions)
- [7. Spacing, Elevation & Layout Grid](#7-spacing-elevation--layout-grid)
- [8. Accessibility & Human Interface Guidelines](#8-accessibility--human-interface-guidelines)

---

## 1. Design Philosophy & Visual Identity

Vult's visual identity balances two distinct mental states:

1. **The Vault (Console & Settings):** Calm, analytical, and frictionless. Designed with generous white-space, soft surface tinting, and clear system diagnostics to make configuring app restrictions intuitive and non-threatening.
2. **The Interception (Security Overlay):** Uncompromising, focused, and stark. When an unauthorized app is opened, distracting UI elements disappear. The interface focuses solely on the lock icon, the target app title, the PIN challenge dots, and the ergonomic numeric keypad.

```
       CONCISE                     IMMERSIVE                    TACTILE
 ┌─────────────────┐          ┌─────────────────┐         ┌─────────────────┐
 │ Zero clutter,   │          │ Deep Obsidian   │         │ 64dp circular   │
 │ direct feedback │   ───>   │ background with │  ───>   │ touch targets   │
 │ on permissions  │          │ Electric Violet │         │ with responsive │
 │ & restrictions. │          │ accents.        │         │ visual feedback.│
 └─────────────────┘          └─────────────────┘         └─────────────────┘
```

---

## 2. Color System & Design Tokens

Vult uses a **Vibrant Energetic Palette** built on an anchor pairing of **Electric Violet** (`#8B00FF`) and **Neon Teal** (`#00E5FF`), balanced against a deep obsidian background (`#0F141A`) for OLED battery conservation and high-contrast night readability.

### 2.1 Color Roles & Palette

```
  Primary                Secondary              Tertiary               Error
  Electric Violet        Neon Teal              Vivid Rose             Carmine Alert
 ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 │ #8B00FF (Light) │    │ #00E5FF (Light) │    │ #FF007F (Light) │    │ #BA1A1A (Light) │
 │ #D7BAFF (Dark)  │    │ #00E5FF (Dark)  │    │ #FFB1C8 (Dark)  │    │ #FFB4AB (Dark)  │
 └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

### 2.2 Dark Mode Token Matrix

The default theme is tailored for high-contrast security overlays and low eye-strain:

| Token Name | Hex Value | Compose Color Token | Role & Usage Description |
| :--- | :--- | :--- | :--- |
| `primary` | `#D7BAFF` | `PrimaryDark` | Primary lock glyphs, action buttons, active indicator dots |
| `onPrimary` | `#490080` | `OnPrimaryDark` | Content rendered directly on top of primary elements |
| `primaryContainer` | `#6900B5` | `PrimaryContainerDark` | Elevated card surfaces and highlighted states |
| `onPrimaryContainer` | `#F0E1FF` | `OnPrimaryContainerDark` | Text labels inside active permission cards |
| `secondary` | `#00E5FF` | `SecondaryDark` | Secondary metadata (app package labels, duration chips) |
| `onSecondary` | `#00373E` | `OnSecondaryDark` | Text rendered on secondary surfaces |
| `tertiary` | `#FFB1C8` | `TertiaryDark` | Accent highlights and decorative security accents |
| `background` | `#0F141A` | `BackgroundDark` | Full-screen background for overlay and scaffold root |
| `surface` | `#0F141A` | `SurfaceDark` | Baseline app list container surfaces |
| `onSurface` | `#E1E2E9` | `OnSurfaceDark` | Primary body and headline text typography |
| `error` | `#FFB4AB` | `ErrorDark` | Error prompts (e.g. *"Incorrect Code"*, inactive permissions) |
| `surfaceVariant` | `#232A34` | `SurfaceVariantDark` | Inactive keypad dots and subtle divider lines |

---

### 2.3 Light Mode Token Matrix

| Token Name | Hex Value | Compose Color Token | Role & Usage Description |
| :--- | :--- | :--- | :--- |
| `primary` | `#8B00FF` | `PrimaryLight` | High-contrast electric violet for headers and primary icons |
| `primaryContainer` | `#F0E1FF` | `PrimaryContainerLight` | Soft lavender tint for active toggle containers |
| `secondary` | `#00E5FF` | `SecondaryLight` | Cyan accent accents |
| `background` | `#F8F9FF` | `BackgroundLight` | Crisp, modern off-white background canvas |
| `surface` | `#F8F9FF` | `SurfaceLight` | Card and list container surfaces |
| `onBackground` | `#191C20` | `OnBackgroundLight` | Dark slate text for optimal reading contrast |
| `error` | `#BA1A1A` | `ErrorLight` | Critical warning alerts and ungranted permission banners |

---

### 2.4 Dynamic Color (Material You)

On devices running Android 12+ (API 31+), Vult supports **Material You Dynamic Theming** through `dynamicDarkColorScheme()` and `dynamicLightColorScheme()`. When enabled, the interface gracefully inherits the user's wallpaper-derived color scheme while falling back to the curated **Electric Violet** scheme on older Android versions or when dynamic theming is unavailable.

---

## 3. Typography & Text Hierarchy

Vult adheres to Material Design 3 type scales using system fonts (`Roboto` / `Google Sans`), ensuring maximum readability across device display scalings and font size preferences:

```
  Headline Medium (28sp, Bold)
  "Vult Locked"

  Title Medium (16sp, Medium)
  "YouTube" (Protected App Name)

  Body Large (16sp, Regular, 24sp Line Height)
  "Crucial: Detects when blocked apps are opened."

  Label Small (11sp, Medium, Error Alert)
  "Incorrect Code"
```

| Text Style | Size | Line Height | Weight | Tracking | Primary Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `headlineMedium` | `28.sp` | `36.sp` | **Bold (700)** | `0.sp` | Main lock screen title, Dashboard header |
| `headlineSmall` | `24.sp` | `32.sp` | **SemiBold (600)** | `0.sp` | Section titles in Settings dialogs |
| `titleLarge` | `22.sp` | `28.sp` | **Medium (500)** | `0.sp` | Keypad numeric text, App names in list |
| `titleMedium` | `16.sp` | `24.sp` | **Medium (500)** | `0.15.sp` | Subheaders, target application name in overlay |
| `bodyLarge` | `16.sp` | `24.sp` | **Regular (400)** | `0.5.sp` | Card descriptions, permission explanations |
| `labelLarge` | `14.sp` | `20.sp` | **Bold (700)** | `0.1.sp` | Section headers (`"Permissions"`, `"App Behavior"`) |
| `labelSmall` | `11.sp` | `16.sp` | **Medium (500)** | `0.5.sp` | Error badges, PIN status labels |

---

## 4. Component Design Specifications

### 4.1 Security Lock Screen Overlay

The security overlay is rendered via `SecurityOverlayService` using a full-screen translucent window (`TYPE_APPLICATION_OVERLAY`).

```
 ┌──────────────────────────────────────────────┐
 │                                              │
 │                   [ 🔒 ]                     │  <-- 64dp Lock Icon (Primary Tint)
 │                                              │
 │                Vult Locked                   │  <-- HeadlineMedium, Bold
 │                  YouTube                     │  <-- TitleMedium, Secondary Tint
 │                                              │
 │              ○ ○ ○ ○ ○ ○ ○ ○ ○               │  <-- 9x 12dp Dot Indicators
 │               Incorrect Code                 │  <-- LabelSmall, Error Tint (Conditional)
 │                                              │
 │                [1]  [2]  [3]                 │
 │                [4]  [5]  [6]                 │  <-- 64dp Circular Keypad
 │                [7]  [8]  [9]                 │
 │                     [0]  [⌫]                 │
 │                                              │
 └──────────────────────────────────────────────┘
```

#### Anatomical Specs:
- **Lock Icon:** `64.dp` size, centered, tinted with `MaterialTheme.colorScheme.primary`.
- **Spacing:** `16.dp` between icon and title, `32.dp` between subtitle and PIN indicators.
- **PIN Dot Row:**
  - Contains 9 circular indicators corresponding to code length.
  - Dot diameter: `12.dp`.
  - Dot spacing: `8.dp` horizontal gap.
  - Active dot color: `MaterialTheme.colorScheme.primary`.
  - Inactive dot color: `MaterialTheme.colorScheme.surfaceVariant`.

---

### 4.2 Alphanumeric Security Keypad

The keypad layout uses an ergonomic 3x4 grid optimized for single-handed thumb reach:

- **Key Geometry:** Circular (`CircleShape`), diameter: `64.dp`.
- **Row Spacing:** `16.dp` vertical padding between rows.
- **Column Spacing:** `24.dp` horizontal padding between columns.
- **Delete Key:** Standard `IconButton` housing an `Icons.AutoMirrored.Filled.Backspace` glyph.
- **Zero Placement:** Centered on the 4th row, flanked by an empty spacer on the left and delete key on the right.

---

### 4.3 Vault Dashboard Screen

The primary application console lists all installed user applications:

- **Top Bar:** Material 3 `LargeTopAppBar` with dynamic scroll collapse (`exitUntilCollapsedScrollBehavior`).
- **Search Bar:** Real-time search query input styled with `surfaceVariant` pill background.
- **List Items:**
  - Height: Minimum `72.dp` touch row.
  - Leading Icon: 48dp rounded app icon loaded asynchronously via Coil.
  - Headline: App name in `titleMedium`.
  - Supporting: Package name in `bodySmall` with subtle opacity.
  - Trailing: Material 3 `Switch` toggle with custom thumb tint.

---

### 4.4 Permissions & Diagnostics Cards

The settings screen displays real-time health checks of OS services:

```
 ┌────────────────────────────────────────────────────────┐
 │ [ ♿ ]  Accessibility Service              [ ACTIVE ✅ ] │
 │        Crucial: Detects when blocked apps are opened.  │
 └────────────────────────────────────────────────────────┘
```

- **Active Container Color:** `MaterialTheme.colorScheme.primaryContainer` with 30% alpha.
- **Inactive Container Color:** `MaterialTheme.colorScheme.errorContainer` with 20% alpha.
- **Interactive State:** Cards respond to tap by launching direct intents to the corresponding system settings screen.

---

## 5. Iconography & Asset Guidelines

- **Icon Set:** Android Material Icons Rounded & AutoMirrored (providing soft, modern corner radiuses).
- **Launcher Icon:**
  - Vector adaptive icon with layer separation (`ic_launcher_background.xml` and `ic_launcher_foreground.xml`).
  - Supports dynamic circular, squircle, and teardrop masks across diverse OEM launchers.
  - Density buckets supplied: `hdpi`, `mdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`.

---

## 6. Motion & Micro-Interactions

| Interaction | Target | Animation Spec | User Intent |
| :--- | :--- | :--- | :--- |
| **Digit Press** | Keypad Button | `ripple()` with alpha pulse (100ms) | Confirms touch registration |
| **Dot Fill** | PIN Indicator | Alpha + color transition (150ms ease-out) | Instant visual verification of code entry |
| **Error Feedback** | Error Text & Dots | Instant error state toggle with red tint | Clear signaling of invalid authentication |
| **Unlock Dismiss** | Overlay Window | Immediate window detachment via `WindowManager.removeView()` | Friction-free transition to unlocked app |
| **Scroll Collapse** | Top App Bar | M3 `nestedScroll` spring physics | Maximize vertical reading viewport |

---

## 7. Spacing, Elevation & Layout Grid

Vult strictly adheres to an **8dp Base Grid**:

```
 4dp    8dp    12dp    16dp    24dp    32dp    48dp    64dp
  │      │       │       │       │       │       │       │
  ▼      ▼       ▼       ▼       ▼       ▼       ▼       ▼
Micro  Inner   Dot    Standard  Screen  Section Keypad  Large
Gap    Pads    Sizes   Margin   Padding Spacing Spacing Target
```

- **Screen Padding:** Standard horizontal gutters are `24.dp` for immersive full-screen breathing room.
- **Card Margins:** List cards use `16.dp` horizontal and `8.dp` vertical padding.
- **Elevation:** Flat design hierarchy with chromatic elevation (relying on color value distinctions between surface and surfaceVariant rather than heavy drop shadows).

---

## 8. Accessibility & Human Interface Guidelines

1. **Target Sizing (WCAG 2.5.5):** All interactive buttons and touch targets meet or exceed the recommended **48dp × 48dp** minimum (keypad buttons are generously sized at **64dp × 64dp**).
2. **Contrast Standards (WCAG 2.1 AA):** All text elements maintain at least a **4.5:1** contrast ratio against their respective background surfaces.
3. **Screen Reader Readiness:** Keypad buttons and state indicators provide descriptive `contentDescription` properties for Android TalkBack.
4. **Haptic & Visual Redundancy:** Input feedback does not rely on color alone; text prompts (*"Incorrect Code"*) accompany visual state changes.

---

<div align="center">
<sub>Vult Design Specification • Built with Jetpack Compose & Material 3 • MIT Licensed</sub>
</div>
