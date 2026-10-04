# Design System: Stock Management System

> **Design Read:** B2B SaaS dashboard for warehouse operators and inventory managers, with a
> Linear-style minimalist + soft-structuralist language, leaning toward Tailwind v4 utilities +
> shadcn/ui (already installed) + Geist Sans + Geist Mono.
>
> **Dials:** `DESIGN_VARIANCE: 5` · `MOTION_INTENSITY: 3` · `VISUAL_DENSITY: 6`
>
> Rationale: This is a data-dense operational tool — not a marketing site. Variance is moderate
> (enough asymmetry to avoid template look), motion is restrained (people are working, not
> browsing), and density is high (tables, metrics, and forms are the product).

---

## 1. Visual Theme & Atmosphere

A restrained, data-confident interface. The atmosphere is clinical but not cold — like a well-lit
operations control room. Heavy use of structure and dividers over decorative cards. Typography
carries the hierarchy; colour is used sparingly to signal meaning. Every pixel earns its place.

- **Density:** Daily App Balanced → Cockpit Dense (6/10)
- **Variance:** Predictable Symmetric → Offset Asymmetric (5/10)
- **Motion:** Static Restrained (3/10) — subtle, purposeful, never decorative

---

## 2. Color Palette & Roles

All greys are cool-tinted (Zinc family). One accent. No warm/cool mixing.

| Name | Hex | Role |
|---|---|---|
| **Canvas** | `#FAFAFA` (Zinc-50) | Primary page background |
| **Surface** | `#FFFFFF` | Card and panel fill |
| **Recessed** | `#F4F4F5` (Zinc-100) | Sidebar background, input backgrounds, table row alt |
| **Border** | `rgba(228, 228, 231, 0.8)` (Zinc-200) | All 1px structural lines, dividers |
| **Border Strong** | `#D4D4D8` (Zinc-300) | Focused inputs, active panel outlines |
| **Ink** | `#18181B` (Zinc-950) | Primary text, headings — never pure `#000000` |
| **Muted** | `#71717A` (Zinc-500) | Secondary text, labels, placeholders, timestamps |
| **Subtle** | `#A1A1AA` (Zinc-400) | Disabled text, helper text |
| **Accent — Electric Indigo** | `#4F46E5` (Indigo-600) | CTAs, active states, focus rings, links |
| **Accent Muted** | `#EEF2FF` (Indigo-50) | Accent surface (selected rows, active nav item bg) |
| **Semantic — Success** | `#16A34A` (Green-600) | In-stock status, positive delta |
| **Semantic — Warning** | `#D97706` (Amber-600) | Low-stock warnings |
| **Semantic — Danger** | `#DC2626` (Red-600) | Out-of-stock, error states, destructive actions |
| **Semantic — Info** | `#0284C7` (Sky-600) | Informational toasts, neutral movement |

**Rules:**
- Saturation cap: 80%. The accent (`#4F46E5`) is the only saturated colour on the page.
- One accent. No secondary accent. Semantic colours are _not_ accent — they convey state only.
- No purple/neon gradients. No AI-purple glow. Button hover is a darkened shade, not a glow.
- All greys must come from the Zinc family exclusively. Do not mix Slate, Stone, or Gray.
- Dark sections within a light-mode page are banned. The app is light-mode-first throughout.

---

## 3. Typography Rules

### Font Stack

| Role | Font | Fallback |
|---|---|---|
| **Display / Headings** | `Geist Sans` (already loaded via `next/font`) | `system-ui, sans-serif` |
| **Body / UI** | `Geist Sans` | `system-ui, sans-serif` |
| **Mono / Numbers / Code** | `Geist Mono` (already loaded via `next/font`) | `ui-monospace, monospace` |

- **Banned fonts:** Inter, Roboto, Arial, Open Sans, Helvetica as the primary UI font.
- `Geist` is already wired in `layout.tsx` via `next/font/google` — do not add duplicate `<link>` tags.
- All numeric data values (quantities, prices, dates, percentages) in tables and metric cards **must use `font-mono` (Geist Mono) with `tabular-nums`**. This ensures columns align.

### Scale & Hierarchy

```
Page Title:        text-2xl font-semibold tracking-tight   (24px, -0.02em)
Section Heading:   text-lg font-medium                     (18px)
Card Title:        text-sm font-semibold                   (14px)
Body / Table:      text-sm font-normal leading-relaxed     (14px, 1.6 line-height)
Label / Caption:   text-xs font-medium tracking-wide       (12px, +0.02em)
Mono Data:         text-sm font-mono tabular-nums          (14px)
```

- Body text colour: `Ink` (`#18181B`) — never pure black.
- Secondary labels: `Muted` (`#71717A`).
- Max line width for any paragraph: `max-w-[65ch]`.
- Use `text-wrap: balance` on headings to prevent orphaned single words.

---

## 4. Component Stylings

### Buttons

```
Primary:      bg-indigo-600  text-white  hover:bg-indigo-700  active:scale-[0.98]  active:-translate-y-px
              rounded-md  px-4 py-2  text-sm font-medium  transition-all duration-150

Secondary:    border border-zinc-300  bg-white  text-zinc-700  hover:bg-zinc-50  active:scale-[0.98]
              rounded-md  px-4 py-2  text-sm font-medium

Destructive:  bg-red-600  text-white  hover:bg-red-700  active:scale-[0.98]
              rounded-md  px-4 py-2  text-sm font-medium

Ghost:        text-zinc-600  hover:bg-zinc-100  hover:text-zinc-900  active:scale-[0.98]
              rounded-md  px-3 py-1.5  text-sm

Icon Button:  w-8 h-8  rounded-md  border border-zinc-200  bg-white  hover:bg-zinc-50  flex items-center justify-center
```

- **No outer glows.** No `box-shadow` with colour on hover. No neon ring on focus — use `ring-2 ring-indigo-500 ring-offset-2` for focus only (keyboard nav).
- All buttons use `transition-all duration-150 ease-out` — never instant state changes.
- Active state: `-translate-y-[1px] scale-[0.98]` to simulate physical press.
- Button text must fit on **one line** at all viewport sizes. 3 words max for primary CTAs.

### Cards & Panels

Cards exist **only** when elevation communicates hierarchy. Otherwise, group content with `border-t divide-y` or negative space alone.

```
Card:         bg-white  border border-zinc-200  rounded-lg  p-5  shadow-sm
              shadow: 0 1px 3px rgba(0,0,0,0.06)  (tinted cool, never black)

Data Card:    bg-white  border border-zinc-200  rounded-lg  p-4
              (used for KPI metric tiles — number + label + delta)

Panel:        bg-white  border-r border-zinc-200  (sidebar)
              bg-zinc-50  (recessed area like filter sidebar or detail pane)
```

- Shadow tint: `rgba(71, 71, 122, 0.06)` — a faint indigo-zinc tint, not pure black.
- Corner radius is uniform across the app: `rounded-lg` (8px) for all interactive containers; `rounded-md` (6px) for inputs and buttons. Do not mix pill (`rounded-full`) containers with square cards.
- No double-card nesting (card inside card) for basic data — use dividers instead.

### Data Tables

Tables are the core product surface. They must be readable at high density.

```
Table header:    bg-zinc-50  border-b border-zinc-200  text-xs font-medium text-zinc-500 uppercase tracking-wide
Table row:       border-b border-zinc-100  text-sm text-zinc-900  hover:bg-zinc-50/60
Table row alt:   bg-zinc-50/30  (optional zebra)
Numeric cells:   font-mono tabular-nums text-right
Status badge:    inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium
```

Status badge colours:
- In Stock: `bg-green-50 text-green-700`
- Low Stock: `bg-amber-50 text-amber-700`
- Out of Stock: `bg-red-50 text-red-700`
- Pending: `bg-zinc-100 text-zinc-600`

### Forms & Inputs

```
Input:        border border-zinc-300  rounded-md  px-3 py-2  text-sm  bg-white
              focus:outline-none  focus:ring-2  focus:ring-indigo-500  focus:border-indigo-500
              placeholder:text-zinc-400

Label:        text-sm font-medium text-zinc-700  mb-1  (always ABOVE the input)
Helper text:  text-xs text-zinc-500  mt-1
Error text:   text-xs text-red-600  mt-1  (BELOW the input, inline — never `window.alert()`)
```

- Label is always **above** the input. Never floating labels. Never placeholder-as-label.
- Standard gap between label/input/helper: `gap-1.5` in a flex-col stack.
- Error inputs: `border-red-500 focus:ring-red-500`.

### Navigation (Sidebar)

```
Sidebar:         w-64  bg-white  border-r border-zinc-200  h-full  flex flex-col
Nav item:        flex items-center gap-2.5  px-3 py-2  rounded-md  text-sm  text-zinc-600
                 hover:bg-zinc-100  hover:text-zinc-900  transition-colors duration-100
Active nav item: bg-indigo-50  text-indigo-700  font-medium
Nav icon:        w-4 h-4  (use @phosphor-icons/react Light or Regular weight, stroke 1.5)
Nav section label: text-xs font-semibold text-zinc-400 uppercase tracking-widest  px-3 mb-1
```

- The active page **must** have a clearly visible active state (accent background + accent text colour).
- Nav icons: use `@phosphor-icons/react` — NOT Lucide (it's already in `package.json` but is discouraged per design skill rules; use Phosphor when building new nav components for visual differentiation).

### Loading States

- **No circular spinners** for skeleton views. Use a shimmer skeleton that matches the exact layout shape.
- Skeleton: `bg-zinc-200 rounded animate-pulse` matching the width/height of the content it replaces.
- For table rows loading: skeleton rows at the same height as real rows.
- For action buttons: disable + show a tiny spinner icon inside the button, maintaining button dimensions.

### Empty States

Every data view needs a composed empty state — not just "No data found".

```
Container:    flex flex-col items-center justify-center  py-16  text-center
Icon:         w-10 h-10  text-zinc-300  mb-4  (Phosphor icon related to the entity)
Heading:      text-base font-medium text-zinc-600
Body:         text-sm text-zinc-400  max-w-[32ch]  mb-4
CTA:          Primary button (e.g., "Add first product")
```

### Error States

- Form errors: inline, below the relevant input field. Red text (`text-red-600`).
- Page-level errors: a composed error card with icon + message + retry action.
- Toasts: for transient success/failure after actions. Not for form validation.
- Copy style: direct active voice. "Could not save product." — not "Oops! Something went wrong!"

---

## 5. Layout Principles

### Page Structure

```
Root:           flex h-screen overflow-hidden
Sidebar:        w-64 flex-shrink-0 border-r (fixed or sticky)
Main:           flex-1 overflow-y-auto
Page header:    sticky top-0 z-10 bg-white/95 backdrop-blur-sm border-b border-zinc-200 px-6 py-4
Page content:   px-6 py-6 max-w-[1400px] mx-auto
```

- **Max width:** `max-w-[1400px]` centred — content never stretches edge-to-edge on wide monitors.
- **Viewport height:** Use `h-[100dvh]` for the root shell, never `h-screen` (iOS Safari jump bug).
- **Page header is sticky** so the breadcrumb + page title + primary action CTA are always visible.

### Grid System

- Use CSS Grid, not flexbox percentage math.
- Dashboard metric row: `grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4`
- Detail/form layout: `grid grid-cols-1 lg:grid-cols-3 gap-6` (2/3 main + 1/3 sidebar)
- Table always full-width within its container.

### Spacing Philosophy

- Section internal padding: `p-5` or `p-6` for cards; `px-6 py-4` for page header.
- Between sections: `space-y-6` or `gap-6`.
- Between form fields: `space-y-4`.
- Compact row density in tables: `py-3` per row.

### Sidebar Behaviour

- Fixed width `w-64` on desktop.
- On mobile (`< 768px`): the sidebar is a slide-over drawer, not collapsed inline.
- No sidebar on auth pages (login/register).

### Responsive Rules

- All multi-column layouts collapse to single column below `768px`.
- No horizontal overflow on any viewport — this is a hard failure.
- Table on mobile: horizontal scroll within a `overflow-x-auto` wrapper, never reflowed.
- Touch targets: minimum `44px` tap area for all interactive elements.
- Typography scaling: `clamp()` for any responsive display size.

---

## 6. Motion & Interaction

Motion is **purposeful and restrained** in a dashboard context. Every animation must answer: "what does this communicate?"

### Principles

- **Spring physics:** `stiffness: 120, damping: 20` for any interactive spring. No `linear` or `ease-in-out`.
- **Transition default:** `transition-all duration-150 ease-out` for hover/active states.
- **Page transitions:** Simple `opacity` fade-in on route change, 200ms. No slide-in or bounce.
- **Scroll entry:** Gentle `opacity: 0 → 1` + `translateY(8px → 0)` over `400ms` for dashboard cards on initial load. Not on every scroll — only on first mount.
- **No perpetual decorative animations** (floating, pulsing blob backgrounds, infinite carousels). This is a work tool.

### Allowed Motion

| Trigger | Animation |
|---|---|
| Button hover | Background colour shift, 150ms |
| Button press | `scale(0.98) translateY(-1px)`, 100ms |
| Nav item hover | Background fade, 100ms |
| Modal open | Scale from `0.95` + fade-in, 200ms |
| Toast appear | SlideIn from bottom-right, 250ms spring |
| Skeleton → content | Fade-in, 300ms |
| Table row hover | Background tint shift, 100ms |

### Forbidden Patterns

- `window.addEventListener('scroll', ...)` — use IntersectionObserver or Motion's `useScroll`.
- Animating `top`, `left`, `width`, `height` — use `transform` and `opacity` only.
- `useState` for continuous pointer/scroll values — use `useMotionValue`.
- `backdrop-blur` on scrolling containers — only on fixed/sticky elements (page header).
- Decorative infinite loops in data panels (e.g., pulsing chart lines).

---

## 7. Iconography

- **Primary library:** `@phosphor-icons/react` — Regular or Light weight, consistent `size={16}` (`w-4 h-4`).
- **Not** Lucide as the primary icon system (it's an existing dep but do not expand its usage for new UI work).
- **One stroke weight across the app.** If Regular weight is chosen, every icon is Regular.
- Navigation icons: `size={18}` at `w-4.5 h-4.5` or `size={16}`.
- Table action icons: `size={14}`, `w-3.5 h-3.5`.
- Empty state icons: `size={40}`, `w-10 h-10`, `text-zinc-300`.
- Never hand-roll SVG icon paths.

---

## 8. Code Quality Standards

- **Semantic HTML:** Use `<nav>`, `<main>`, `<aside>`, `<section>`, `<header>`, `<article>`. No `<div>` soup.
- **Tabular numbers everywhere in tables:** `font-variant-numeric: tabular-nums` / Tailwind's `tabular-nums` class.
- **Focus rings:** Every interactive element must have a visible focus ring (`focus-visible:ring-2 focus-visible:ring-indigo-500`). Never `outline: none` without a replacement.
- **Alt text:** Meaningful alt text on every `<img>`. Never `alt=""` on meaningful images.
- **z-index discipline:** Systemic layers only — sidebar overlay: 40, sticky header: 30, modal: 50, toast: 60. No arbitrary `z-[9999]`.
- **No dead links.** Every `href="#"` button either links to a real route or is a `<button>` with a handler.
- **Active nav state.** Every navigation item reflects the current route via an active style.
- **No `console.log` in shipped code.**

---

## 9. Anti-Patterns (Banned — AI Tells)

The following patterns are explicitly banned. Any future component that introduces these is a design failure:

- **Fonts:** Inter, Roboto, Arial, Open Sans as the primary UI font.
- **Icons:** Expanding Lucide usage for new components. Never hand-roll SVG paths.
- **Colours:** Pure black (`#000000`) anywhere. Purple/neon gradients. Mixed warm+cool greys. More than one accent.
- **Layout:** Three equal-width cards as a feature row. Centred hero sections (not applicable to this app, but banned anyway). `h-screen` for full-height containers. Flexbox `calc()` percentage hacks.
- **Animation:** `linear` or `ease-in-out` easing on interactive elements. `window.scroll` listeners. Animating layout properties. Decorative infinite loops in operational UI.
- **Copy:** "Elevate", "Seamless", "Unleash", "Next-Gen", "Game-changer". Exclamation marks in success messages. "Oops!" in error messages. Passive voice in error/system messages. "Lorem ipsum."
- **UI patterns:** `window.alert()` for errors. Circular spinners for skeleton states. Floating/overlapping labels on inputs. Cards without elevated purpose (use dividers instead). Duplicate CTA intent on one screen.
- **Accessibility:** Missing focus rings. Missing alt text. `<div>` or `<span>` used as a button without `role="button"` and keyboard support.
- **Emojis:** Anywhere in the application UI.
- **Fake round numbers** in mock data: never `50%`, `100 units`, `$100.00`. Use `47`, `\$99.00`, `83.4%`.

---

## 10. Existing Stack Summary (Do Not Change)

| Concern | Solution |
|---|---|
| Framework | Next.js 16 (App Router, RSC) |
| Styling | Tailwind v4 (`@tailwindcss/postcss`) |
| Components | shadcn/ui (`base-nova` style, Radix primitives) |
| Fonts | Geist Sans + Geist Mono via `next/font/google` |
| Icons (existing) | `lucide-react` — do not expand, migrate new work to Phosphor |
| State | Zustand (`src/store/`) |
| Data fetching | TanStack Query v5 + Axios |
| HTTP | `src/lib/axios.ts` base instance |

> [!IMPORTANT]
> **Do not replace shadcn/ui or Tailwind.** This design system works _with_ the existing stack.
> `globals.css` currently has minimal token definitions — future tokens should be added there as
> CSS custom properties under `@theme inline` following the Tailwind v4 convention.

> [!NOTE]
> The `components.json` uses `style: "base-nova"` and `baseColor: "neutral"`. When adding new
> shadcn components via `npx shadcn@latest add`, they will follow this style automatically.
> Customise generated components to align with the colour palette above (swap `neutral` tones
> for the Zinc family as documented here).
