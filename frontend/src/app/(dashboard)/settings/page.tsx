"use client";

import * as React from "react";
import { ProtectedRoute } from "@/components/auth/protected-route";
import { Moon, Sun, Shield, Save, Check, UserPlus, KeyRound } from "lucide-react";
import { BUTTON_PRIMARY, FIELD } from "@/components/ui/control-classes";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useAuth } from "@/hooks/useAuth";
import axiosInstance from "@/lib/axios";
import { authService } from "@/services/auth.service";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { masterDataService } from "@/services/master-data.service";

export default function SettingsPage() {
  const [theme, setTheme] = React.useState<"dark" | "light">("dark");
  const [defaultThreshold, setDefaultThreshold] = React.useState(10);
  const [refreshInterval, setRefreshInterval] = React.useState("30");
  const [saved, setSaved] = React.useState(false);
  const { register, user, logout } = useAuth();
  const queryClient = useQueryClient();
  const [account, setAccount] = React.useState({ username: "", email: "", password: "", role: "STAFF" as "MANAGER" | "STAFF" });
  const [accountMessage, setAccountMessage] = React.useState("");
  const [accountError, setAccountError] = React.useState("");
  const [creatingAccount, setCreatingAccount] = React.useState(false);
  const [roleMismatchUsername, setRoleMismatchUsername] = React.useState<string | null>(null);
  const [apiStatus, setApiStatus] = React.useState<"checking" | "healthy" | "unavailable">("checking");
  const [passwords, setPasswords] = React.useState({ current: "", next: "", confirm: "" });
  const [passwordMessage, setPasswordMessage] = React.useState("");
  const [passwordError, setPasswordError] = React.useState("");
  const [changingPassword, setChangingPassword] = React.useState(false);
  const [categoryName, setCategoryName] = React.useState("");
  const [brandName, setBrandName] = React.useState("");
  const [catalogError, setCatalogError] = React.useState("");
  const [catalogMessage, setCatalogMessage] = React.useState("");
  const [savingCatalog, setSavingCatalog] = React.useState(false);
  const isAdmin = (typeof user?.role === "string" ? user.role : user?.role.name) === "ADMIN";
  const role = typeof user?.role === "string" ? user.role : user?.role.name;
  const canManageCatalog = role === "ADMIN" || role === "MANAGER";
  const categoriesQuery = useQuery({ queryKey: ["categories"], queryFn: masterDataService.getCategories, enabled: canManageCatalog });
  const brandsQuery = useQuery({ queryKey: ["brands"], queryFn: masterDataService.getBrands, enabled: canManageCatalog });

  React.useEffect(() => {
    let active = true;
    const checkApi = async () => {
      try {
        await axiosInstance.get("/health");
        if (active) setApiStatus("healthy");
      } catch {
        if (active) setApiStatus("unavailable");
      }
    };
    void checkApi();
    const interval = window.setInterval(checkApi, 30_000);
    return () => { active = false; window.clearInterval(interval); };
  }, []);

  const handleThemeChange = (next: "dark" | "light") => {
    setTheme(next);
    if (next === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  };

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  const createAccount = async () => {
    setCreatingAccount(true); setAccountError(""); setAccountMessage("");
    const requestedRole = account.role;
    try {
      const created = await register({ ...account, role: requestedRole });
      const savedRole = typeof created.role === "string" ? created.role : created.role?.name ?? "unknown role";
      if (savedRole.toUpperCase() !== requestedRole) {
        setRoleMismatchUsername(created.username);
        setAccountError(`${created.username} was saved as ${savedRole}, not ${requestedRole}. The running backend may be outdated. This account already exists as ${savedRole}; restart Spring Boot with the current backend code. This form creates accounts only, so changing this existing account's role requires the admin role-management workflow, which is not available yet.`);
        return;
      }
      setAccountMessage(`${created.username} was created as ${savedRole}. Share the username and temporary password securely.`);
      setAccount({ username: "", email: "", password: "", role: "STAFF" });
    } catch (error) {
      setAccountError((error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? "Could not create the account. Check the details and try again.");
    } finally { setCreatingAccount(false); }
  };

  const changePassword = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setPasswordError("");
    setPasswordMessage("");
    if (passwords.next !== passwords.confirm) {
      setPasswordError("The new passwords do not match.");
      return;
    }
    setChangingPassword(true);
    try {
      await authService.changePassword(passwords.current, passwords.next);
      setPasswordMessage("Password changed. Signing you out so you can sign in with the new password…");
      setPasswords({ current: "", next: "", confirm: "" });
      void logout();
      window.location.replace("/login?passwordChanged=1");
    } catch (error) {
      setPasswordError((error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? "Could not change the password. Check your current password and try again.");
    } finally {
      setChangingPassword(false);
    }
  };

  const addCatalogItem = async (kind: "category" | "brand", event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const name = (kind === "category" ? categoryName : brandName).trim();
    if (!name) return;
    setCatalogError("");
    setCatalogMessage("");
    setSavingCatalog(true);
    try {
      if (kind === "category") {
        await masterDataService.createCategory({ name, description: "" });
        setCategoryName("");
        await queryClient.invalidateQueries({ queryKey: ["categories"] });
      } else {
        await masterDataService.createBrand({ name, description: "" });
        setBrandName("");
        await queryClient.invalidateQueries({ queryKey: ["brands"] });
      }
      setCatalogMessage(`${kind === "category" ? "Category" : "Brand"} added and available for products.`);
    } catch (error) {
      setCatalogError((error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? `Could not add this ${kind}. It may already exist.`);
    } finally {
      setSavingCatalog(false);
    }
  };

  return (
    <ProtectedRoute>
    <div className="space-y-6 max-w-3xl">
      <div className="border-b border-border/40 pb-4">
        <h1 className="text-xl font-bold tracking-tight text-foreground md:text-2xl">
          System & Account Settings
        </h1>
        <p className="mt-1 text-xs text-muted-foreground">
          Configure terminal interface parameters, default thresholds, and operational telemetry.
        </p>
      </div>

      <div className="space-y-6">
        {isAdmin && <section className="rounded-lg border border-border/50 bg-card/60 p-4">
          <div className="flex items-center gap-2"><Shield className="h-4 w-4 text-muted-foreground" /><div><h2 className="text-sm font-semibold text-foreground">Team Access</h2><p className="text-xs text-muted-foreground">Create new MANAGER and STAFF accounts. Existing accounts cannot be re-assigned here; all users sign in at the same StockFlow login page.</p></div></div>
          <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label className="text-xs text-muted-foreground">Username<input required minLength={3} maxLength={50} autoComplete="off" value={account.username} onChange={(e) => setAccount({ ...account, username: e.target.value })} className={`${FIELD} mt-1 w-full`} /></label>
            <label className="text-xs text-muted-foreground">Email<input required type="email" autoComplete="off" value={account.email} onChange={(e) => setAccount({ ...account, email: e.target.value })} className={`${FIELD} mt-1 w-full`} /></label>
            <label className="text-xs text-muted-foreground">Temporary password<input required minLength={6} type="password" autoComplete="new-password" value={account.password} onChange={(e) => setAccount({ ...account, password: e.target.value })} className={`${FIELD} mt-1 w-full`} /></label>
            <div className="text-xs text-muted-foreground"><span className="mb-1 block">Role</span><Select value={account.role} onValueChange={(role) => setAccount((current) => ({ ...current, role: role as "MANAGER" | "STAFF" }))}><SelectTrigger className="mt-1 w-full"><SelectValue /></SelectTrigger><SelectContent><SelectItem value="STAFF">Staff</SelectItem><SelectItem value="MANAGER">Manager</SelectItem></SelectContent></Select></div>
          </div>
          {accountMessage && <p role="status" className="mt-3 text-xs text-emerald-400">{accountMessage}</p>}
          {accountError && <p role="alert" className="mt-3 text-xs text-rose-400">{accountError}</p>}
          <button type="button" disabled={creatingAccount || !account.username || !account.email || !account.password || roleMismatchUsername === account.username} onClick={() => void createAccount()} className={`${BUTTON_PRIMARY} mt-4 disabled:cursor-not-allowed disabled:opacity-50`}><UserPlus className="h-3.5 w-3.5" />{creatingAccount ? "Creating…" : roleMismatchUsername === account.username ? "Account already created" : "Create account"}</button>
        </section>}
        {canManageCatalog && <section className="rounded-lg border border-border/50 bg-card/60 p-4">
          <div className="mb-4"><h2 className="text-sm font-semibold text-foreground">Product Categories & Brands</h2><p className="text-xs text-muted-foreground">Add the categories and brands used by your inventory. These options appear in the product form.</p></div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <form onSubmit={(event) => void addCatalogItem("category", event)} className="space-y-2">
              <label className="block text-xs text-muted-foreground">New category<input required maxLength={100} value={categoryName} onChange={(event) => setCategoryName(event.target.value)} placeholder="e.g. Bakery & Prepared Foods" className={`${FIELD} mt-1 w-full`} /></label>
              <button type="submit" disabled={savingCatalog || !categoryName.trim()} className={`${BUTTON_PRIMARY} disabled:opacity-50`}>{savingCatalog ? "Adding…" : "Add category"}</button>
            </form>
            <form onSubmit={(event) => void addCatalogItem("brand", event)} className="space-y-2">
              <label className="block text-xs text-muted-foreground">New brand<input required maxLength={100} value={brandName} onChange={(event) => setBrandName(event.target.value)} placeholder="e.g. Acme Foods" className={`${FIELD} mt-1 w-full`} /></label>
              <button type="submit" disabled={savingCatalog || !brandName.trim()} className={`${BUTTON_PRIMARY} disabled:opacity-50`}>{savingCatalog ? "Adding…" : "Add brand"}</button>
            </form>
          </div>
          <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 text-xs">
            <div><p className="mb-1 font-medium text-foreground">Categories ({categoriesQuery.data?.length ?? 0})</p><p className="text-muted-foreground">{categoriesQuery.data?.map((item) => item.name).join(" · ") || "Loading categories…"}</p></div>
            <div><p className="mb-1 font-medium text-foreground">Brands ({brandsQuery.data?.length ?? 0})</p><p className="text-muted-foreground">{brandsQuery.data?.map((item) => item.name).join(" · ") || "Loading brands…"}</p></div>
          </div>
          {catalogMessage && <p role="status" className="mt-3 text-xs text-emerald-400">{catalogMessage}</p>}
          {catalogError && <p role="alert" className="mt-3 text-xs text-rose-400">{catalogError}</p>}
        </section>}
        <section className="rounded-lg border border-border/50 bg-card/60 p-4">
          <div className="flex items-center gap-2"><KeyRound className="h-4 w-4 text-muted-foreground" /><div><h2 className="text-sm font-semibold text-foreground">Change password</h2><p className="text-xs text-muted-foreground">Use your current password to set a new one. You’ll need to sign in again afterward.</p></div></div>
          <form onSubmit={(event) => void changePassword(event)} className="mt-4 space-y-3">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <label className="text-xs text-muted-foreground sm:col-span-2">Current password<input required type="password" autoComplete="current-password" value={passwords.current} onChange={(event) => setPasswords({ ...passwords, current: event.target.value })} className={`${FIELD} mt-1 w-full`} /></label>
              <label className="text-xs text-muted-foreground">New password<input required minLength={12} maxLength={72} type="password" autoComplete="new-password" value={passwords.next} onChange={(event) => setPasswords({ ...passwords, next: event.target.value })} className={`${FIELD} mt-1 w-full`} /><span className="mt-1 block text-[10px]">Use 12 to 72 characters.</span></label>
              <label className="text-xs text-muted-foreground">Confirm new password<input required minLength={12} maxLength={72} type="password" autoComplete="new-password" value={passwords.confirm} onChange={(event) => setPasswords({ ...passwords, confirm: event.target.value })} className={`${FIELD} mt-1 w-full`} /></label>
            </div>
            {passwordMessage && <p role="status" className="text-xs text-emerald-400">{passwordMessage}</p>}
            {passwordError && <p role="alert" className="text-xs text-rose-400">{passwordError}</p>}
            <button type="submit" disabled={changingPassword || !passwords.current || !passwords.next || !passwords.confirm} className={`${BUTTON_PRIMARY} disabled:cursor-not-allowed disabled:opacity-50`}><KeyRound className="h-3.5 w-3.5" />{changingPassword ? "Changing…" : "Change password"}</button>
          </form>
        </section>
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
              <Select value={refreshInterval} onValueChange={setRefreshInterval}>
                <SelectTrigger className="mt-1 w-full"><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="15">15 Seconds (Realtime)</SelectItem>
                  <SelectItem value="30">30 Seconds (Default)</SelectItem>
                  <SelectItem value="60">60 Seconds (Conserved)</SelectItem>
                </SelectContent>
              </Select>
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
              <p className="text-xs text-muted-foreground">Live API and database connectivity check</p>
            </div>
            <span className={`rounded px-2 py-0.5 font-mono text-[10px] border ${apiStatus === "healthy" ? "bg-emerald-500/10 text-emerald-400 border-emerald-500/20" : apiStatus === "unavailable" ? "bg-rose-500/10 text-rose-400 border-rose-500/20" : "bg-foreground/5 text-muted-foreground border-border/50"}`}>
              {apiStatus === "healthy" ? "API + DATABASE HEALTHY" : apiStatus === "unavailable" ? "API UNAVAILABLE" : "CHECKING API…"}
            </span>
          </div>

          <div className="mt-3 rounded border border-border/40 bg-foreground/[0.02] p-2.5 font-mono text-[11px] text-muted-foreground flex justify-between">
            <span>API: same-origin secure proxy</span>
            <span>Signed in: {user?.username ?? "Unknown"}</span>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button type="button" onClick={handleSave} className={BUTTON_PRIMARY}>
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
      </div>
    </div>
    </ProtectedRoute>
  );
}
