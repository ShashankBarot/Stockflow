"use client";

import * as React from "react";
import { AppSidebar } from "./sidebar";
import { TopBar } from "./top-bar";
import { useAuth } from "@/hooks/useAuth";

interface AppShellProps {
  children: React.ReactNode;
}

export function AppShell({ children }: AppShellProps) {
  const { logout } = useAuth();
  const [searchValue, setSearchValue] = React.useState("");
  const [selectedWarehouseId, setSelectedWarehouseId] = React.useState<string | undefined>();
  const [navigationOpen, setNavigationOpen] = React.useState(false);

  React.useEffect(() => {
    if (!navigationOpen) return;

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setNavigationOpen(false);
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [navigationOpen]);

  return (
    <div className="flex min-h-[100dvh] bg-background text-foreground antialiased">
      {/* Desktop Persistent Sidebar */}
      <AppSidebar className="sticky top-0 hidden w-[196px] md:flex" />

      {navigationOpen && (
        <>
          <button
            type="button"
            aria-label="Close navigation"
            className="fixed inset-0 z-40 bg-black/55 backdrop-blur-[2px] md:hidden"
            onClick={() => setNavigationOpen(false)}
          />
          <AppSidebar
            className="fixed inset-y-0 left-0 z-50 w-[min(84vw,280px)] shadow-2xl md:hidden"
            onClose={() => setNavigationOpen(false)}
          />
        </>
      )}

      {/* Main Workspace */}
      <div className="flex flex-1 flex-col min-w-0 overflow-hidden">
        <TopBar
          searchValue={searchValue}
          onSearchChange={setSearchValue}
          selectedWarehouseId={selectedWarehouseId}
          onSelectWarehouse={setSelectedWarehouseId}
          onLogout={logout}
          onOpenNavigation={() => setNavigationOpen(true)}
        />
        <main className="min-w-0 flex-1 overflow-y-auto px-3 py-4 sm:px-4 sm:py-5 md:px-6 lg:px-7">
          {children}
        </main>
      </div>
    </div>
  );
}
