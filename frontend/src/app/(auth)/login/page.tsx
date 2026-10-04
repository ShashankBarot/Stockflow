"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { ArrowRight, Boxes, LockKeyhole } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { FormField } from "@/components/ui/form-field";

export default function LoginPage() {
  const { login, isLoading, error, isAuthenticated } = useAuth();
  const router = useRouter();
  const [username, setUsername] = React.useState("");
  const [password, setPassword] = React.useState("");

  React.useEffect(() => { if (isAuthenticated) router.replace("/"); }, [isAuthenticated, router]);

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      await login({ username: username.trim(), password });
      const next = new URLSearchParams(window.location.search).get("next");
      router.replace(next?.startsWith("/") ? next : "/");
    } catch { /* The shared auth state displays the service error. */ }
  }

  return (
    <main className="mx-auto w-full max-w-[400px]">
      <div className="mb-7 flex items-center gap-3">
        <span className="grid h-9 w-9 place-items-center rounded-md bg-foreground text-background"><Boxes className="h-5 w-5" /></span>
        <div><p className="text-sm font-semibold tracking-tight">Stockflow</p><p className="text-[11px] text-muted-foreground">Inventory operations</p></div>
      </div>
      <section className="border-y border-white/[0.08] py-6">
        <div className="mb-6">
          <p className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">Workspace access</p>
          <h1 className="mt-2 text-[25px] font-medium tracking-tight">Sign in to Stockflow</h1>
          <p className="mt-1.5 text-sm text-muted-foreground">Continue to your inventory and warehouse operations.</p>
        </div>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField label="Username" htmlFor="username" required>
            <Input id="username" name="username" autoComplete="username" required minLength={3} maxLength={50} value={username} onChange={(event) => setUsername(event.target.value)} placeholder="Your username" />
          </FormField>
          <FormField label="Password" htmlFor="password" required>
            <Input id="password" name="password" type="password" autoComplete="current-password" required minLength={6} maxLength={100} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Your password" />
          </FormField>
          {error && <p role="alert" className="rounded-md border border-[var(--negative)]/20 bg-[var(--negative)]/[0.06] px-3 py-2 text-xs text-[var(--negative)]">{error}</p>}
          <Button type="submit" className="w-full" disabled={isLoading}><LockKeyhole className="h-4 w-4" />{isLoading ? "Signing in…" : "Sign in"}<ArrowRight className="ml-auto h-4 w-4" /></Button>
        </form>
      </section>
      <p className="mt-4 text-xs text-muted-foreground">Access is provisioned by your Stockflow administrator.</p>
    </main>
  );
}
