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

  return (
    <div className="flex min-h-[100dvh] bg-background text-foreground antialiased">
      {/* Desktop Persistent Sidebar */}
      <AppSidebar />

      {/* Main Workspace */}
      <div className="flex flex-1 flex-col min-w-0 overflow-hidden">
        <TopBar
          searchValue={searchValue}
          onSearchChange={setSearchValue}
          selectedWarehouseId={selectedWarehouseId}
          onSelectWarehouse={setSelectedWarehouseId}
          onLogout={logout}
        />
        <main className="min-w-0 flex-1 overflow-y-auto px-4 py-5 md:px-6 lg:px-7">
          {children}
        </main>
      </div>
    </div>
  );
}
