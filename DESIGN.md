---
name: Cofrete
description: Mobile-first freight finance tool for Brazilian truck drivers.
colors:
  accent: "#0F766E"
  accent-border: "#A8CEC4"
  accent-soft: "#DDEEE9"
  accent-text: "#F7FBF8"
  surface: "#FAF9F4"
  panel: "#F2F0E8"
  field: "#FFFCF2"
  border: "#D9DED6"
  text: "#18231F"
  muted: "#5E6A64"
  success-bg: "#E3F1DF"
  success-text: "#245B34"
  danger-bg: "#F8E7DF"
  danger-text: "#8A2D14"
  info-bg: "#E6E7F4"
  info-text: "#343473"
typography:
  display:
    fontFamily: "system"
    fontSize: "26px"
    fontWeight: 800
    lineHeight: 1.15
    letterSpacing: "0"
  headline:
    fontFamily: "system"
    fontSize: "24px"
    fontWeight: 800
    lineHeight: 1.25
    letterSpacing: "0"
  title:
    fontFamily: "system"
    fontSize: "15px"
    fontWeight: 800
    lineHeight: 1.2
    letterSpacing: "0"
  body:
    fontFamily: "system"
    fontSize: "15px"
    fontWeight: 400
    lineHeight: 1.4
    letterSpacing: "0"
  label:
    fontFamily: "system"
    fontSize: "12px"
    fontWeight: 800
    lineHeight: 1.33
    letterSpacing: "0"
  mono:
    fontFamily: "monospace"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: 1.58
    letterSpacing: "0"
rounded:
  xs: "5px"
  sm: "8px"
  pill: "999px"
spacing:
  xxs: "3px"
  xs: "4px"
  sm: "6px"
  md: "8px"
  lg: "12px"
  xl: "14px"
  xxl: "16px"
  page: "18px"
  bottom: "24px"
components:
  button-primary:
    backgroundColor: "{colors.accent}"
    textColor: "{colors.accent-text}"
    rounded: "{rounded.sm}"
    padding: "0 16px"
    height: "50px"
  button-primary-pressed:
    backgroundColor: "{colors.accent}"
    textColor: "{colors.accent-text}"
    rounded: "{rounded.sm}"
    padding: "0 16px"
    height: "50px"
  input-default:
    backgroundColor: "{colors.field}"
    textColor: "{colors.text}"
    rounded: "{rounded.sm}"
    padding: "10px 12px"
    height: "46px"
  card-default:
    backgroundColor: "{colors.field}"
    textColor: "{colors.text}"
    rounded: "{rounded.sm}"
    padding: "14px"
  panel-default:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    rounded: "{rounded.sm}"
    padding: "16px"
  chip-info:
    backgroundColor: "{colors.info-bg}"
    textColor: "{colors.info-text}"
    rounded: "{rounded.pill}"
    padding: "6px 12px"
  chip-success:
    backgroundColor: "{colors.success-bg}"
    textColor: "{colors.success-text}"
    rounded: "{rounded.pill}"
    padding: "7px 12px"
---

# Design System: Cofrete

## 1. Overview

**Creative North Star: "Cab Ledger"**

Cofrete should feel like a working financial notebook built for a truck cab: practical, conservative, readable under pressure, and clearly separated into business decisions. The interface is product-first, not expressive for its own sake. It uses familiar forms, bordered panels, compact metrics, and direct Portuguese-Brazil copy so the driver can decide whether to accept, reject, or renegotiate a freight.

The system rejects consumer-finance gloss, official-government styling, gamified earnings celebration, generic SaaS dashboard polish, and any wording that implies guaranteed income or professional legal, tax, accounting, insurance, or official guidance. Visual energy comes from money separation and decision clarity, not decoration.

**Key Characteristics:**
- Restrained warm surfaces with one teal operational accent.
- Flat, bordered panels that keep cost, reserve, profit, and safe withdrawal categories separate.
- Dense but readable mobile forms, with 46px minimum inputs and 50px primary actions.
- Conservative status language that pairs color with explicit text.

## 2. Colors

The palette is warm, low-glare, and utilitarian, with teal reserved for primary action, current selection, and key decision emphasis.

### Primary
- **Operational Teal** (#0F766E): Primary buttons, active tab state, small action markers, and the border on the active profitability result panel. Use sparingly so it keeps decision weight.
- **Teal Border** (#A8CEC4): Quiet border for teal-tinted hero and orientation panels.
- **Washed Teal** (#DDEEE9): Hero and orientation panels where the current workflow needs gentle emphasis without looking promotional.
- **Action Text Cream** (#F7FBF8): Text on teal actions.

### Neutral
- **Cab Paper Surface** (#FAF9F4): App background and most full-width product surfaces.
- **Ledger Panel** (#F2F0E8): API and secondary reference panels.
- **Input Paper** (#FFFCF2): Inputs, metric cards, and compact status rows.
- **Dust Line** (#D9DED6): Standard one-pixel borders and dividers.
- **Ink Green-Black** (#18231F): Primary text.
- **Roadside Muted** (#5E6A64): Secondary text, labels, helper notes, inactive navigation.

### Semantic
- **Reserve Success Wash** (#E3F1DF): Positive confirmation and recommendation backgrounds.
- **Reserve Success Text** (#245B34): Positive confirmation and recommendation text.
- **Risk Wash** (#F8E7DF): Validation and error backgrounds.
- **Risk Text** (#8A2D14): Validation and error text.
- **MVP Info Wash** (#E6E7F4): Low-priority status chips.
- **MVP Info Text** (#343473): Low-priority status chip text.

### Named Rules

**The Teal Earns Its Place Rule.** Operational Teal is for actions, active navigation, and decision emphasis only. Do not use it as broad decoration.

**The Warm Neutral Rule.** Do not introduce pure white or pure black for product surfaces. Surfaces should stay slightly warm and low-glare.

## 3. Typography

**Display Font:** system font with platform fallback.
**Body Font:** system font with platform fallback.
**Label/Mono Font:** system label text plus monospace for API endpoints.

**Character:** Native, compact, and work-focused. Typography should feel like an operating tool for repeated decisions, not a marketing product or financial lifestyle app.

### Hierarchy
- **Display** (800, 26px, 1.15): Brand wordmark in the app header only.
- **Headline** (800, 24px, 30px line-height): Screen titles and major workflow headings.
- **Title** (800, 15px, tight line-height): Section titles, result panel headings, and compact panel labels.
- **Body** (400, 15px, 21px line-height): Summary copy, field text, and driver-facing explanations. Keep prose short and scannable.
- **Small Body** (400-700, 12-13px, 16-19px line-height): Caveats, tones, helper notes, API paths, and metadata.
- **Label** (700-900, 11-12px, uppercase where useful): Field labels, metric labels, tabs, chips, and recommendations.

### Named Rules

**The No Drama Type Rule.** Do not use display fonts, fluid heading scales, negative letter spacing, or oversized hero type inside app workflows.

## 4. Elevation

Cofrete is flat by default. Depth is conveyed through tonal layering, one-pixel borders, spacing, and state opacity rather than shadows. This keeps financial categories auditable and avoids a glossy fintech feel.

### Named Rules

**The Border Ledger Rule.** Use full one-pixel borders around panels, cards, inputs, and status blocks. Do not use colored side stripes or decorative shadow lifts.

**The Flat At Rest Rule.** Avoid shadows for normal content. If future overlays are introduced, use elevation only to clarify modality or focus.

## 5. Components

### Buttons

- **Shape:** Rounded rectangle with 8px radius.
- **Primary:** Operational Teal background, Action Text Cream text, 50px minimum height, 16px horizontal padding, bold 15px label.
- **Pressed / Disabled:** Use opacity changes only. Pressed state uses 0.88 opacity; disabled/loading uses 0.72 opacity plus inline activity indicator.
- **Secondary / Ghost:** Not yet formalized. Future variants should keep the same radius, height, and label weight.

### Chips

- **Style:** Pill radius at 999px, compact horizontal padding, bold 12px text.
- **Info:** MVP/status chip uses MVP Info Wash and MVP Info Text.
- **Success:** Recommendation chip uses Reserve Success Wash and Reserve Success Text.
- **State:** Never rely on color alone. Chip text must state the status or recommendation.

### Cards / Containers

- **Corner Style:** 8px radius for panels, cards, inputs, and grouped sections.
- **Background:** Cab Paper Surface for main sections, Input Paper for metric cards and compact rows, Ledger Panel for secondary reference surfaces.
- **Shadow Strategy:** No shadows at rest. Use borders and tonal changes.
- **Border:** One-pixel Dust Line for normal panels; Operational Teal border only for the active profitability result.
- **Internal Padding:** 14px for metric cards, 16px for sections and panels, 18px for hero panels and page gutters.

### Inputs / Fields

- **Style:** Input Paper background, Dust Line border, 8px radius, 46px minimum height, 12px horizontal and 10px vertical padding.
- **Labels:** Uppercase 12px bold muted labels above fields.
- **Focus:** Keep the same structure. Future focus treatment should use a border color shift plus accessible platform focus behavior, not glow or animation.
- **Error / Disabled:** Errors use Risk Wash, Risk Text, full border, and plain-language copy.

### Navigation

- **Style:** Bottom tabs with 8px rounded active items, 52px minimum item height, compact 11px bold labels, and muted inactive text.
- **Active State:** Operational Teal background with Action Text Cream text.
- **Mobile Treatment:** Keep labels short and stable. Do not let tab labels wrap or resize the bar.

### Metrics

- **Style:** Two-column wrapping grid on available width, 48% flex basis, 104px minimum height, Input Paper background, Dust Line border, 14px padding.
- **Content:** Uppercase label, strong money/value line, muted explanatory tone.
- **Finance Rule:** Gross freight, pass-through cash flow, direct cost, reserves, expected profit, and safe personal withdrawal must remain separate metrics.

## 6. Do's and Don'ts

### Do:

- **Do** use Operational Teal (#0F766E) for primary actions, active navigation, and current decision emphasis only.
- **Do** keep all financial categories visibly separate: gross freight, pass-through cash flow, direct costs, reserves, profit, and safe personal withdrawal.
- **Do** use warm neutral surfaces (#FAF9F4, #FFFCF2, #F2F0E8) instead of pure white product canvases.
- **Do** pair every risk, recommendation, and caveat color with explicit Portuguese-Brazil text.
- **Do** keep touch targets stable: 46px minimum inputs, 50px primary buttons, 52px tab items.
- **Do** keep copy direct, conservative, and easy to scan in a truck cab or loading yard.

### Don't:

- **Don't** use consumer-finance gloss, official-government styling, gamified earnings celebration, or generic SaaS dashboard polish.
- **Don't** imply guaranteed income, legal compliance, tax advice, accounting advice, insurance advice, or official record updates.
- **Don't** count toll reimbursement or Vale-Pedagio as profit or safe personal withdrawal.
- **Don't** use pure #000 or #fff as product surfaces or primary text.
- **Don't** add colored side-stripe borders, gradient text, glassmorphism, decorative shadows, or hero-metric templates.
- **Don't** invent unusual form controls or navigation patterns for flavor. Familiar product controls are the standard.
