"use client";

import * as React from "react";
import { ProtectedRoute } from "@/components/auth/protected-route";
import { Moon, Sun, Shield, Sliders, Database, Save, Check } from "lucide-react";
import { BUTTON, BUTTON_PRIMARY, FIELD } from "@/components/ui/control-classes";
import { SegmentedControl } from "@/components/ui/segmented-control";

export default function SettingsPage() {
  const [theme, setTheme] = React.useState<"dark" | "light">("dark");
  const [currency, setCurrency] = React.useState("USD");
  const [defaultThreshold, setDefaultThreshold] = React.useState(10);
  const [refreshInterval, setRefreshInterval] = React.useState("30");
  const [saved, setSaved] = React.useState(false);

  React.useEffect(() => {
    const isDark = document.documentElement.classList.contains("dark");
    setTheme(isDark ? "dark" : "light");
  }, []);

  const handleThemeChange = (next: "dark" | "light") => {
    setTheme(next);
    if (next === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <ProtectedRoute roles={["ADMIN"]}>
    <div className="space-y-6 max-w-3xl">
      <div className="border-b border-border/40 pb-4">
        <h1 className="text-xl font-bold tracking-tight text-foreground md:text-2xl">
          System & Workspace Settings
        </h1>
        <p className="mt-1 text-xs text-muted-foreground">
          Configure terminal interface parameters, default thresholds, and operational telemetry.
        </p>
      </div>

      <form onSubmit={handleSave} className="space-y-6">
        {/* Appearance & Theme */}
        <div className="rounded-lg border border-border/50 bg-card/60 p-4">
          <h2 className="text-sm font-semibold text-foreground">Interface Appearance</h2>
          <p className="text-xs text-muted-foreground mt-0.5">
            Switch between dark operations terminal mode and high-contrast light mode.
          </p>

          <div className="mt-4 flex items-center gap-3">
            <button
              type="button"
              onClick={() => handleThemeChange("dark")}
              className={`flex items-center gap-2 rounded-md px-3 py-2 text-xs font-medium border transition-all ${
                theme === "dark"
                  ? "bg-foreground text-background border-foreground shadow-sm"
                  : "bg-foreground/[0.03] text-foreground border-border/50 hover:bg-foreground/[0.08]"
              }`}
            >
              <Moon className="h-4 w-4" />
              <span>Near-Black Dark (Recommended)</span>
            </button>

            <button
              type="button"
              onClick={() => handleThemeChange("light")}
              className={`flex items-center gap-2 rounded-md px-3 py-2 text-xs font-medium border transition-all ${
                theme === "light"
                  ? "bg-foreground text-background border-foreground shadow-sm"
                  : "bg-foreground/[0.03] text-foreground border-border/50 hover:bg-foreground/[0.08]"
              }`}
            >
              <Sun className="h-4 w-4" />
              <span>Quiet Neutral Light</span>
            </button>
          </div>
        </div>

        {/* Inventory Defaults */}
        <div className="rounded-lg border border-border/50 bg-card/60 p-4 space-y-4">
          <h2 className="text-sm font-semibold text-foreground">Inventory Threshold Defaults</h2>
          <p className="text-xs text-muted-foreground">
            Standard parameters applied to newly registered items and warehouse allocations.
          </p>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-xs font-medium text-foreground/80">
                Default Safety Stock Buffer (Units)
              </label>
              <input
                type="number"
                value={defaultThreshold}
                onChange={(e) => setDefaultThreshold(parseInt(e.target.value) || 0)}
                className={`${FIELD} w-full font-mono`}
              />
              <span className="text-[10px] text-muted-foreground">
                Flags item as Low Stock when on-hand falls at or below this value.
              </span>
            </div>

            <div>
              <label className="mb-1 block text-xs font-medium text-foreground/80">
                Ledger Polling Interval (Seconds)
              </label>
              <select
                value={refreshInterval}
                onChange={(e) => setRefreshInterval(e.target.value)}
                className={`${FIELD} w-full`}
              >
                <option value="15">15 Seconds (Realtime)</option>
                <option value="30">30 Seconds (Default)</option>
                <option value="60">60 Seconds (Conserved)</option>
              </select>
              <span className="text-[10px] text-muted-foreground">
                Frequency of background delta polling for inventory tables.
              </span>
            </div>
          </div>
        </div>

        {/* Server & Cluster Node Status */}
        <div className="rounded-lg border border-border/50 bg-card/60 p-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-sm font-semibold text-foreground">Backend API Node</h2>
              <p className="text-xs text-muted-foreground">Connected to local Spring Boot service</p>
            </div>
            <span className="rounded bg-emerald-500/10 px-2 py-0.5 font-mono text-[10px] text-emerald-400 border border-emerald-500/20">
              HEALTHY : PORT 8080
            </span>
          </div>

          <div className="mt-3 rounded border border-border/40 bg-foreground/[0.02] p-2.5 font-mono text-[11px] text-muted-foreground flex justify-between">
            <span>Base URL: http://localhost:8080/api</span>
            <span>Auth Token: Bearer Active</span>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button type="submit" className={BUTTON_PRIMARY}>
            {saved ? (
              <>
                <Check className="h-3.5 w-3.5 text-emerald-400" />
                <span>Settings Saved</span>
              </>
            ) : (
              <>
                <Save className="h-3.5 w-3.5" />
                <span>Save Changes</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
    </ProtectedRoute>
  );
}
