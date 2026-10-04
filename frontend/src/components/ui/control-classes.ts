/**
 * Unified control styles extracted from the Tradeboard / Operations terminal reference.
 *
 * Rules:
 *  - dark theme is primary (near-black #090909 background)
 *  - field/control backgrounds use 5-8% foreground opacity
 *  - raised states use 10-14% foreground opacity
 *  - borders use 4-6% foreground opacity
 *  - ring accents use subtle emerald / primary
 */

export const THUMB =
  "bg-foreground text-background font-medium shadow-sm transition-all";

export const TRACK =
  "inline-flex items-center gap-1 rounded-md bg-foreground/[0.05] p-1 text-xs text-muted-foreground border border-border/40";

export const SEGMENT_OFF =
  "rounded px-2.5 py-1 text-xs font-medium text-muted-foreground transition-colors hover:text-foreground hover:bg-foreground/[0.04]";

export const SEGMENT_ON =
  "rounded px-2.5 py-1 text-xs font-medium bg-foreground text-background shadow-sm transition-all";

export const BUTTON =
  "inline-flex items-center justify-center gap-2 rounded-md px-3 py-1.5 text-xs font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 border border-border/50 bg-foreground/[0.04] text-foreground hover:bg-foreground/[0.08] hover:border-border/80";

export const ICON_BUTTON =
  "inline-flex h-8 w-8 items-center justify-center rounded-md border border-border/50 bg-foreground/[0.03] text-muted-foreground transition-colors hover:bg-foreground/[0.08] hover:text-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring";

export const BUTTON_PRIMARY =
  "inline-flex items-center justify-center gap-2 rounded-md bg-foreground px-3.5 py-1.5 text-xs font-semibold text-background transition-opacity hover:opacity-90 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50";

export const BUTTON_DANGER =
  "inline-flex items-center justify-center gap-2 rounded-md bg-rose-500/10 border border-rose-500/30 px-3 py-1.5 text-xs font-medium text-rose-400 transition-colors hover:bg-rose-500/20";

export const FIELD =
  "h-8 rounded-md border border-border/60 bg-foreground/[0.03] px-2.5 text-xs text-foreground placeholder:text-muted-foreground/60 focus:border-border focus:bg-foreground/[0.06] focus:outline-none focus:ring-1 focus:ring-ring transition-colors";

export const SURFACE =
  "rounded-lg border border-border/50 bg-card/60 backdrop-blur-sm";

export const RAISED =
  "rounded-lg border border-border/70 bg-card shadow-sm";
