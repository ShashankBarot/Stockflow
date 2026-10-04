"use client";

import * as React from "react";
import { AppSidebar } from "./sidebar";
import { TopBar } from "./top-bar";

interface AppShellProps {
  children: React.ReactNode;
}

export function AppShell({ children }: AppShellProps) {
  const [searchValue, setSearchValue] = React.useState("");
  const [selectedWarehouseId, setSelectedWarehouseId] = React.useState<string | undefined>();

  return (
    <div className="flex min-h-screen bg-background text-foreground antialiased selection:bg-foreground selection:text-background">
      {/* Desktop Persistent Sidebar */}
      <AppSidebar />

      {/* Main Workspace */}
      <div className="flex flex-1 flex-col min-w-0 overflow-hidden">
        <TopBar
          searchValue={searchValue}
          onSearchChange={setSearchValue}
          selectedWarehouseId={selectedWarehouseId}
          onSelectWarehouse={setSelectedWarehouseId}
        />
        <main className="flex-1 overflow-y-auto px-4 py-6 md:px-8">
          {children}
        </main>
      </div>
    </div>
  );
}
