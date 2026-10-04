import { type ReactNode } from "react";

export default function DashboardLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen">
      {/* Sidebar placeholder */}
      <aside className="w-64 border-r bg-muted/40">
        <nav className="p-4">
          <h2 className="text-lg font-semibold">Stock Management</h2>
          <ul className="mt-4 space-y-2 text-sm">
            <li><a href="/dashboard/inventory">Inventory</a></li>
            <li><a href="/dashboard/movements">Movements</a></li>
            <li><a href="/dashboard/warehouses">Warehouses</a></li>
            <li><a href="/dashboard/reports">Reports</a></li>
            <li><a href="/dashboard/settings">Settings</a></li>
          </ul>
        </nav>
      </aside>
      {/* Main content */}
      <main className="flex-1 p-8">{children}</main>
    </div>
  );
}
