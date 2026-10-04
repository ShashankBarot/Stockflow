"use client";

import * as React from "react";
import { X, ArrowRight, CheckCircle2, AlertCircle } from "lucide-react";
import { SegmentedControl, SegmentOption } from "@/components/ui/segmented-control";
import { BUTTON, BUTTON_PRIMARY, FIELD } from "@/components/ui/control-classes";
import { useWarehouses } from "@/hooks/useWarehouses";
import { useCreateMovement } from "@/hooks/useMovements";
import type { Product, MovementType } from "@/types";

interface AdjustStockDrawerProps {
  product: Product | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

const TYPE_OPTIONS: SegmentOption<MovementType>[] = [
  { value: "INBOUND", label: "Inbound (+)" },
  { value: "OUTBOUND", label: "Outbound (-)" },
  { value: "TRANSFER", label: "Transfer (⇄)" },
  { value: "ADJUSTMENT", label: "Audit / Adjust" },
];

export function AdjustStockDrawer({
  product,
  isOpen,
  onClose,
  onSuccess,
}: AdjustStockDrawerProps) {
  const [movementType, setMovementType] = React.useState<MovementType>("INBOUND");
  const [quantity, setQuantity] = React.useState<number>(10);
  const [warehouseId, setWarehouseId] = React.useState<number | "">("");
  const [destWarehouseId, setDestWarehouseId] = React.useState<number | "">("");
  const [reference, setReference] = React.useState("");
  const [notes, setNotes] = React.useState("");
  const [errorMsg, setErrorMsg] = React.useState<string | null>(null);

  const { data: warehousesData } = useWarehouses();
  const warehouses = warehousesData?.data ?? [];

  const createMovement = useCreateMovement();

  React.useEffect(() => {
    if (warehouses.length > 0 && !warehouseId) {
      setWarehouseId(warehouses[0].id);
    }
  }, [warehouses, warehouseId]);

  React.useEffect(() => {
    if (isOpen) {
      setReference(`MOV-${Date.now().toString().slice(-6)}`);
      setErrorMsg(null);
    }
  }, [isOpen]);

  if (!isOpen || !product) return null;

  const currentStock = (product as unknown as { currentStock?: number; quantity?: number }).currentStock ?? (product as unknown as { quantity?: number }).quantity ?? 45;
  const qtyNum = Number(quantity) || 0;

  let projectedStock = currentStock;
  if (movementType === "INBOUND") projectedStock += qtyNum;
  else if (movementType === "OUTBOUND") projectedStock -= qtyNum;
  else if (movementType === "ADJUSTMENT") projectedStock = qtyNum;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!warehouseId) {
      setErrorMsg("Please select an origin warehouse.");
      return;
    }
    if (movementType === "TRANSFER" && (!destWarehouseId || destWarehouseId === warehouseId)) {
      setErrorMsg("Please select a different destination facility.");
      return;
    }
    if (qtyNum <= 0 && movementType !== "ADJUSTMENT") {
      setErrorMsg("Quantity must be greater than zero.");
      return;
    }

    try {
      await createMovement.mutateAsync({
        productId: product.id,
        warehouseId: Number(warehouseId),
        type: movementType,
        quantity: qtyNum,
        referenceNumber: reference,
        destinationWarehouseId: destWarehouseId ? Number(destWarehouseId) : undefined,
        notes: notes || `Stock operational update via terminal (${movementType})`,
      });

      onSuccess?.();
      onClose();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Failed to record movement";
      setErrorMsg(msg);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-xs">
      <div className="h-full w-full max-w-md border-l border-border/60 bg-card p-6 shadow-2xl overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-border/40 pb-4">
          <div>
            <h2 className="text-sm font-semibold tracking-tight text-foreground">
              Stock Adjustment Terminal
            </h2>
            <p className="mt-0.5 text-xs text-muted-foreground font-mono">
              SKU: {product.sku}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded p-1 text-muted-foreground hover:bg-foreground/[0.05] hover:text-foreground"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Item Summary Bar */}
        <div className="my-4 rounded-md border border-border/40 bg-foreground/[0.02] p-3 text-xs">
          <div className="font-medium text-foreground">{product.name}</div>
          <div className="mt-2 flex items-center justify-between text-[11px] font-mono text-muted-foreground">
            <span>Current Stock: <strong className="text-foreground">{currentStock}</strong></span>
            <span>Category: {product.category || "General"}</span>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Movement Type */}
          <div>
            <label className="mb-1.5 block text-xs font-medium text-foreground/80">
              Movement Direction
            </label>
            <SegmentedControl
              options={TYPE_OPTIONS}
              value={movementType}
              onChange={setMovementType}
              className="w-full justify-between"
              size="sm"
            />
          </div>

          {/* Warehouse */}
          <div>
            <label className="mb-1.5 block text-xs font-medium text-foreground/80">
              Origin Facility
            </label>
            <select
              value={warehouseId}
              onChange={(e) => setWarehouseId(Number(e.target.value))}
              className={`${FIELD} w-full`}
            >
              {warehouses.map((wh) => (
                <option key={wh.id} value={wh.id}>
                  {wh.name} ({wh.location})
                </option>
              ))}
            </select>
          </div>

          {/* Destination for Transfer */}
          {movementType === "TRANSFER" && (
            <div>
              <label className="mb-1.5 block text-xs font-medium text-foreground/80">
                Destination Facility
              </label>
              <select
                value={destWarehouseId}
                onChange={(e) => setDestWarehouseId(Number(e.target.value))}
                className={`${FIELD} w-full`}
              >
                <option value="">Select target warehouse...</option>
                {warehouses
                  .filter((w) => w.id !== warehouseId)
                  .map((wh) => (
                    <option key={wh.id} value={wh.id}>
                      {wh.name} ({wh.location})
                    </option>
                  ))}
              </select>
            </div>
          )}

          {/* Quantity */}
          <div>
            <label className="mb-1.5 block text-xs font-medium text-foreground/80">
              {movementType === "ADJUSTMENT" ? "Audited Real Count" : "Units to Move"}
            </label>
            <input
              type="number"
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(Math.max(0, parseInt(e.target.value) || 0))}
              className={`${FIELD} w-full font-mono text-sm`}
            />
          </div>

          {/* Reference # */}
          <div>
            <label className="mb-1.5 block text-xs font-medium text-foreground/80">
              Reference Identifier
            </label>
            <input
              type="text"
              value={reference}
              onChange={(e) => setReference(e.target.value)}
              className={`${FIELD} w-full font-mono`}
            />
          </div>

          {/* Notes */}
          <div>
            <label className="mb-1.5 block text-xs font-medium text-foreground/80">
              Audit Justification / Notes
            </label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Received shipment PO-4091 or shelf audit recount"
              className="w-full rounded-md border border-border/60 bg-foreground/[0.03] p-2 text-xs text-foreground placeholder:text-muted-foreground/60 focus:border-border focus:outline-none focus:ring-1 focus:ring-ring"
            />
          </div>

          {/* Projection Calculation Box */}
          <div className="rounded-md border border-border/50 bg-foreground/[0.03] p-3 text-xs">
            <div className="flex items-center justify-between text-muted-foreground">
              <span>Projected Level</span>
              <span className="font-mono text-[10px]">REALTIME LEDGER ESTIMATE</span>
            </div>
            <div className="mt-2 flex items-center justify-between">
              <span className="font-mono text-muted-foreground">{currentStock} units</span>
              <ArrowRight className="h-3.5 w-3.5 text-muted-foreground/60" />
              <span
                className={`font-mono font-bold text-sm ${
                  projectedStock < 0
                    ? "text-rose-400"
                    : projectedStock < 10
                    ? "text-amber-400"
                    : "text-emerald-400"
                }`}
              >
                {projectedStock} units
              </span>
            </div>
          </div>

          {errorMsg && (
            <div className="flex items-center gap-2 rounded-md border border-rose-500/30 bg-rose-500/10 p-2.5 text-xs text-rose-400">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          {/* Submit Action */}
          <div className="pt-2">
            <button
              type="submit"
              disabled={createMovement.isPending}
              className={`${BUTTON_PRIMARY} w-full py-2.5 text-xs`}
            >
              {createMovement.isPending ? "Executing Ledger Commit..." : "Commit Stock Adjustment"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
