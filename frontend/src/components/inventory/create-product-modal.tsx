"use client";

import * as React from "react";
import { X, AlertCircle } from "lucide-react";
import { BUTTON, BUTTON_PRIMARY, FIELD } from "@/components/ui/control-classes";
import { useCreateProduct } from "@/hooks/useInventory";

interface CreateProductModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

export function CreateProductModal({
  isOpen,
  onClose,
  onSuccess,
}: CreateProductModalProps) {
  const [sku, setSku] = React.useState("");
  const [name, setName] = React.useState("");
  const [description, setDescription] = React.useState("");
  const [category, setCategory] = React.useState("Sensors");
  const [basePrice, setBasePrice] = React.useState<number>(95);
  const [errorMsg, setErrorMsg] = React.useState<string | null>(null);

  const createProduct = useCreateProduct();

  React.useEffect(() => {
    if (isOpen) {
      setSku(`SKU-${Math.floor(1000 + Math.random() * 9000)}`);
      setErrorMsg(null);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !sku.trim()) {
      setErrorMsg("Please provide both product title and SKU.");
      return;
    }

    try {
      await createProduct.mutateAsync({
        sku,
        name,
        description,
        category,
        basePrice: Number(basePrice) || 0,
      });
      onSuccess?.();
      onClose();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Failed to register product";
      setErrorMsg(msg);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div className="w-full max-w-md rounded-lg border border-border/70 bg-card p-6 shadow-2xl">
        <div className="flex items-center justify-between border-b border-border/40 pb-3">
          <h2 className="text-sm font-semibold tracking-tight text-foreground">
            Register New Inventory Asset
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded p-1 text-muted-foreground hover:bg-foreground/[0.05] hover:text-foreground"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="mb-1 block text-xs font-medium text-foreground/80">
              SKU Identifier
            </label>
            <input
              type="text"
              value={sku}
              onChange={(e) => setSku(e.target.value)}
              className={`${FIELD} w-full font-mono uppercase`}
              required
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-medium text-foreground/80">
              Product Title
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Ultrasonic Flow Meter 24V"
              className={`${FIELD} w-full`}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-1 block text-xs font-medium text-foreground/80">
                Category
              </label>
              <input
                type="text"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                placeholder="Sensors, Valves..."
                className={`${FIELD} w-full`}
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-foreground/80">
                Unit Base Price ($)
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={basePrice}
                onChange={(e) => setBasePrice(parseFloat(e.target.value) || 0)}
                className={`${FIELD} w-full font-mono`}
              />
            </div>
          </div>

          <div>
            <label className="mb-1 block text-xs font-medium text-foreground/80">
              Specifications / Description
            </label>
            <textarea
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Operational specs, supplier part number..."
              className="w-full rounded-md border border-border/60 bg-foreground/[0.03] p-2 text-xs text-foreground placeholder:text-muted-foreground/60 focus:border-border focus:outline-none focus:ring-1 focus:ring-ring"
            />
          </div>

          {errorMsg && (
            <div className="flex items-center gap-2 rounded-md border border-rose-500/30 bg-rose-500/10 p-2.5 text-xs text-rose-400">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={onClose} className={BUTTON}>
              Cancel
            </button>
            <button
              type="submit"
              disabled={createProduct.isPending}
              className={BUTTON_PRIMARY}
            >
              {createProduct.isPending ? "Registering Asset..." : "Register Product"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
