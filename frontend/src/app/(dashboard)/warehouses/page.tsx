"use client";

import * as React from "react";
import { Plus, Warehouse, MapPin, X, AlertCircle } from "lucide-react";
import { BUTTON, BUTTON_PRIMARY, FIELD } from "@/components/ui/control-classes";
import { useWarehouses, useCreateWarehouse } from "@/hooks/useWarehouses";
import { useInventory } from "@/hooks/useInventory";
import type { Warehouse as WarehouseType } from "@/types";
import { useAuth } from "@/hooks/useAuth";

export default function WarehousesPage() {
  const [isModalOpen, setIsModalOpen] = React.useState(false);
  const [name, setName] = React.useState("");
  const [location, setLocation] = React.useState("");
  const [errorMsg, setErrorMsg] = React.useState<string | null>(null);

  const { data: whData, isLoading, isError: isWarehouseError, refetch } = useWarehouses();
  const { data: inventoryData, isLoading: isInventoryLoading, isError: isInventoryError } = useInventory({ page: 0, size: 500 });
  const createWh = useCreateWarehouse();
  const { user } = useAuth();
  const role = typeof user?.role === "string" ? user.role : user?.role.name;
  const canManageFacilities = role === "ADMIN" || role === "MANAGER";

  const warehouses: WarehouseType[] = whData?.data ?? [];

  // Compute facility metrics
  const facilityStats = React.useMemo(() => {
    const map = new Map<number, { totalQty: number; skuIds: Set<number>; capacity: number }>();
    if (inventoryData?.data) {
      for (const item of inventoryData.data) {
        const whId = item.warehouse?.id;
        if (whId) {
          const prev = map.get(whId) || { totalQty: 0, skuIds: new Set<number>(), capacity: 0 };
          if (item.product?.id) prev.skuIds.add(item.product.id);
          map.set(whId, {
            totalQty: prev.totalQty + item.quantity,
            skuIds: prev.skuIds,
            capacity: prev.capacity + (item.maxCapacity ?? 0),
          });
        }
      }
    }
    return map;
  }, [inventoryData]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !location.trim()) {
      setErrorMsg("Please provide both name and location.");
      return;
    }

    try {
      await createWh.mutateAsync({ name, location });
      setName("");
      setLocation("");
      setIsModalOpen(false);
      refetch();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Failed to register warehouse";
      setErrorMsg(msg);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/40 pb-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-foreground md:text-2xl">
            Facility Network
          </h1>
          <p className="mt-1 text-xs text-muted-foreground">
            Distribution centers, bonded depositories, and regional fulfillment hubs.
          </p>
        </div>

        {canManageFacilities && <button
          type="button"
          onClick={() => setIsModalOpen(true)}
          className={BUTTON_PRIMARY}
        >
          <Plus className="h-3.5 w-3.5" />
          <span>Add Facility</span>
        </button>}
      </div>

      {/* Facilities Grid */}
      {isLoading ? (
        <div className="rounded-lg border border-border/50 bg-card/60 px-4 py-12 text-center text-sm text-muted-foreground">Loading registered facilities…</div>
      ) : isWarehouseError ? (
        <div role="alert" className="rounded-lg border border-rose-500/30 bg-rose-500/[0.06] px-4 py-8 text-center text-sm text-rose-300">Could not load facilities from StockFlow. Check the backend connection and try again.</div>
      ) : warehouses.length === 0 ? (
        <div className="rounded-lg border border-border/50 bg-card/60 px-4 py-12 text-center"><p className="text-sm font-medium text-foreground">No facilities registered</p><p className="mt-1 text-xs text-muted-foreground">{canManageFacilities ? "Add a facility to start tracking stock by location." : "Ask an administrator or manager to add a facility."}</p></div>
      ) : (
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
        {warehouses.map((wh) => {
          const stats = facilityStats.get(wh.id) || { totalQty: 0, skuIds: new Set<number>(), capacity: 0 };
          const hasCapacity = stats.capacity > 0 && !isInventoryLoading && !isInventoryError;
          const usagePercent = hasCapacity ? Math.min(100, Math.round((stats.totalQty / stats.capacity) * 100)) : 0;

          return (
            <div
              key={wh.id}
              className="flex flex-col justify-between rounded-lg border border-border/50 bg-card/60 p-4 transition-colors hover:border-border/80"
            >
              <div>
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-md bg-foreground/[0.04] border border-border/40 text-foreground">
                      <Warehouse className="h-4 w-4" />
                    </div>
                    <div>
                      <h2 className="text-xs font-semibold text-foreground">{wh.name}</h2>
                      <div className="flex items-center gap-1 text-[11px] text-muted-foreground">
                        <MapPin className="h-3 w-3" />
                        <span>{wh.location}</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Utilization Gauge */}
                <div className="mt-4 space-y-1.5">
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-muted-foreground text-[11px]">{hasCapacity ? "Storage Allocation" : "Stock on hand"}</span>
                    {hasCapacity && <span className="font-mono font-medium text-foreground">{usagePercent}%</span>}
                  </div>
                  {hasCapacity && <div className="h-2 w-full overflow-hidden rounded-full bg-foreground/[0.05]"><div className={`h-full rounded-full transition-all ${usagePercent > 85 ? "bg-rose-500" : usagePercent > 70 ? "bg-amber-500" : "bg-foreground"}`} style={{ width: `${usagePercent}%` }} /></div>}
                  <div className="flex justify-between text-[10px] font-mono text-muted-foreground/60">
                    <span>{isInventoryLoading ? "Loading stock…" : isInventoryError ? "Inventory unavailable" : `${stats.totalQty.toLocaleString()} units`}</span>
                    <span>{isInventoryLoading || isInventoryError ? "" : hasCapacity ? `Capacity ${stats.capacity.toLocaleString()}` : "Capacity not set"}</span>
                  </div>
                </div>
              </div>

              {/* Facility Metadata Strip */}
              <div className="mt-5 grid grid-cols-1 gap-2 border-t border-border/40 pt-3 text-[11px] font-mono">
                <div>
                  <span className="text-muted-foreground/70 text-[10px]">PRODUCT SKUS</span>
                  <p className="font-semibold text-foreground">{isInventoryLoading || isInventoryError ? "—" : stats.skuIds.size}</p>
                </div>
              </div>
            </div>
          );
        })}
      </div>
      )}

      {/* Facility Registration Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
          <div className="w-full max-w-md rounded-lg border border-border/70 bg-card p-6 shadow-2xl">
            <div className="flex items-center justify-between border-b border-border/40 pb-3">
              <h2 className="text-sm font-semibold tracking-tight text-foreground">
                Add Distribution Facility
              </h2>
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
                className="rounded p-1 text-muted-foreground hover:bg-foreground/[0.05] hover:text-foreground"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <form onSubmit={handleCreate} className="mt-4 space-y-4">
              <div>
                <label className="mb-1 block text-xs font-medium text-foreground/80">
                  Facility Name
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. South Depository (DFW-3)"
                  className={`${FIELD} w-full`}
                  required
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-medium text-foreground/80">
                  Location / Physical Address
                </label>
                <input
                  type="text"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  placeholder="e.g. Dallas, TX"
                  className={`${FIELD} w-full`}
                  required
                />
              </div>

              {errorMsg && (
                <div className="flex items-center gap-2 rounded-md border border-rose-500/30 bg-rose-500/10 p-2.5 text-xs text-rose-400">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{errorMsg}</span>
                </div>
              )}

              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setIsModalOpen(false)} className={BUTTON}>
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createWh.isPending}
                  className={BUTTON_PRIMARY}
                >
                  {createWh.isPending ? "Connecting Facility..." : "Register Facility"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
