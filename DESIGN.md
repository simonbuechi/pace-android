# Pace Amigo — Design System & UI Specification 🎨

This document outlines the design philosophy, visual identity, soft tactile neumorphic system, typography scaling formulas, and responsive behavior for **Pace Amigo**.

---

## 1. Design Philosophy

Pace Amigo merges **Material 3 Expressive** with **Soft Tactile Neumorphism**:
- **Tangible & Physical**: Digital surfaces behave with gentle physical depth—raised cards, sunken wells, and glowing interactive pills.
- **Extreme Visibility ("Across the Room" Rule)**: A timer app must be readable from 10 to 20 meters away in noisy, bright, or fast-paced environments (gyms, boxing rings, kitchens, desk stands).
- **Expressive Motion**: Fluid color transitions reflect workout intensity changes without jarring context switches.
- **Adaptive Contrast**: Tactile elements automatically evaluate background luminance to preserve readability in both bright sunlight and dark workout studios.

---

## 2. Brand Identity & Color System

### 2.1 The Signature Pace Gradient
The app's visual anchor is the continuous gradient drawn from the Pace logo:
- **Pace Magenta**: `#9123A6` (`Color(0xFF9123A6)`)
- **Pace Raspberry**: `#D7195F` (`Color(0xFFD7195F)`)
- **Brand Gradient**: `Brush.linearGradient(listOf(Color(0xFF9123A6), Color(0xFFD7195F)))`

```
┌────────────────────────────────────────────────────────┐
│  #9123A6 (Magenta Purple) ────► #D7195F (Raspberry Pink)│
└────────────────────────────────────────────────────────┘
```

### 2.2 Phase-Specific Color Semantics
During timer execution, ambient gradients and indicators dynamically tint to convey phase urgency:

| Phase | Color Hex | Color Name | Intended Psychological Feel |
| :--- | :--- | :--- | :--- |
| **Warm-Up / Prep** | `#FFA000` | Amber Pulse | Preparation, anticipation, getting ready |
| **Focus / Work** | `#D7195F` | Pace Raspberry | High energy, drive, exertion |
| **Break / Rest** | `#00BFA5` | Teal Mint | Cooling down, breathing, recovery |
| **Completed** | `#9123A6` | Pace Magenta | Celebration, achievement, closure |

### 2.3 Soft Tactile Theme Palettes

#### Dark Mode Palette
- **Background**: `#16141B` (Deep obsidian with subtle violet undertone)
- **Surface**: `#201C27` (Base tactile surface)
- **Surface Raised**: `#282331` (Elevated cards and pill buttons)
- **Surface Sunken**: `#131118` (Recessed wells and inner tracks)
- **Shadow Dark**: `#0C0A0F` (Ambient deep shadow)
- **Shadow Light / Highlight**: `Color.White.copy(alpha = 0.08f)` (Bevel light source)
- **On Background**: `#F3EEF8`
- **On Surface Variant**: `#B0A7BA`

#### Light Mode Palette
- **Background**: `#F4F1F7` (Soft warm lavender-tinted white)
- **Surface**: `#F9F7FB` (Base clean surface)
- **Surface Raised**: `#FFFFFF` (Crisp raised card surface)
- **Surface Sunken**: `#E9E5EE` (Subtle recessed well)
- **Shadow Dark**: `#D4CEDC` (Soft ambient shadow)
- **Shadow Light / Highlight**: `Color.White.copy(alpha = 0.95f)` (Crisp top rim highlight)
- **On Background**: `#1F1A25`
- **On Surface Variant**: `#5D5466`

---

## 3. The Soft Tactile Component System

Located in [`TactileSurface.kt`](file:///d:/dev/antigravity/pace-android/app/src/main/java/ch/simibu/pace/ui/components/TactileSurface.kt).

### 3.1 `TactileCard` (Raised Surface)
Used for duration pickers, round steppers, routine cards, and settings blocks:
- **Elevation**: `4.dp` to `6.dp` shadow with dual ambient/spot diffusion.
- **Bevel Border**: Vertical gradient border with 1.dp stroke:
  - Top edge: High-alpha white highlight representing overhead light.
  - Bottom edge: Low-alpha shadow border anchoring the elevation.
- **Corner Radius**: `22.dp` to `24.dp` continuous curvature.

### 3.2 `TactileSunkenWell` (Recessed Surface)
Used for wheel picker selection frames, round count badges, phase indicator tags, and progress tracks:
- **Depth Effect**: Inverted vertical gradient border:
  - Top edge: Dark shadow border casting downward.
  - Bottom edge: Light highlight rim catching ambient light from below.
- **Corner Radius**: `14.dp` to `18.dp`.

### 3.3 `TactilePillButton` (Interactive Hero Action)
Used for "Training starten", play/pause toggles, and floating action buttons:
- **Fill**: `PaceBrandGradient` (`#9123A6` to `#D7195F`).
- **Glow Shadow**: Colored ambient shadow tinted with `PaceRaspberry.copy(alpha = 0.35f)` and spot shadow tinted with `PaceMagenta.copy(alpha = 0.55f)`.
- **Elevation**: `8.dp` to `10.dp`.

---

## 4. Typography & Scaling Formulas

### 4.1 Tabular Figures Requirement
All countdown digits require monospace tabular figures to eliminate jitter and horizontal wobble during high-speed ticks:
```kotlin
style = MaterialTheme.typography.displayLarge.copy(
    fontFamily = FontFamily.Default,
    fontFeatureSettings = "tnum"
)
```

### 4.2 Portrait Layout Scaling
In portrait mode, the circular timer ring scales adaptively with device width and height:
$$\text{ringDiameter} = \min(\text{maxWidth} - 32\,\text{dp},\; \text{maxHeight} \times 0.44)\quad [\text{clamped to } 350\,\text{dp}]$$
$$\text{digitFontSize} = (\text{ringDiameter.value} \times 0.27)\,\text{sp}\quad [\approx 88\,\text{sp} - 96\,\text{sp}]$$

### 4.3 Widescreen & Landscape Panoramic Layout
In landscape mode (or widescreen foldables/tablets), circular rings restrict vertical height. Therefore, the UI transforms into a panoramic dashboard:
- **Timer Digits**: Rendered unconstrained at maximal size:
  $$\text{landFontSize} = (\text{maxHeight.value} \times 0.48)\,\text{sp}\quad [\text{up to } 140\,\text{sp} - 160\,\text{sp}]$$
- **Progress Track**: A sleek, full-width horizontal tactile bar (`14.dp` height) filled with `PaceBrandGradient`.
- **Controls Column**: Clustered on the right edge for thumb reachability on mobile and desk-docked accessibility.

```
┌───────────────────────────────────────────────────────────────────────────┬────────┐
│  [ FOKUS ]  Runde 1 von 5   Quick Session                                  │  ( X ) │
│                                                                           │        │
│    04:32                                                                  │  ( ↺ ) │
│                                                                           │  ( ▶ ) │
│  [████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░]   │  ( ⏭ ) │
└───────────────────────────────────────────────────────────────────────────┴────────┘
```

---

## 5. Motion & Micro-Interactions

### 5.1 Warning Pulse Transition
During the last 3 seconds of any phase ($t \in [1, 3]$):
- `rememberInfiniteTransition` pulses the ring scale between $1.0\times$ and $1.05\times$ via `FastOutSlowInEasing`.
- Acoustic warning tick (`Beep` or `DigitalPulse`) fires on each second tick.

### 5.2 Phase Color Morphing
When transitioning from **Focus** $\to$ **Break** $\to$ **Focus**:
- Background ambient radial/vertical gradient smoothly interpolates over `600ms`.
- Ring progress resets with `tween(350, FastOutSlowInEasing)`.

### 5.3 Digits Crossfade
- Numerical changes animate using `fadeIn(tween(100))` combined with `fadeOut(tween(100))` to produce a silky, instant digital clock feel.

---

## 6. Accessibility & Ergonomics

1. **Color Contrast**: All primary text maintains a minimum contrast ratio of `4.5:1` in both Light (`#1F1A25` on `#F4F1F7`) and Dark (`#F3EEF8` on `#16141B`) modes.
2. **Touch Targets**: All interactive tactile buttons have a minimum touch target diameter of `48.dp` (main Play/Pause button: `86.dp`).
3. **Multi-Sensory Feedback**: Users are never reliant on visual countdowns alone; acoustic sound pool cues and vibrator patterns accompany every phase transition.
