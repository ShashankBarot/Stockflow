"use client";

import * as React from "react";
import { X, AlertCircle } from "lucide-react";
import { BUTTON, BUTTON_PRIMARY, FIELD } from "@/components/ui/control-classes";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useCreateProduct } from "@/hooks/useInventory";
import { useBrands, useCategories } from "@/hooks/useMasterData";

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
  const [categoryId, setCategoryId] = React.useState("");
  const [brandId, setBrandId] = React.useState("");
  const [basePrice, setBasePrice] = React.useState("");
  const [errorMsg, setErrorMsg] = React.useState<string | null>(null);

  const createProduct = useCreateProduct();
  const categoriesQuery = useCategories();
  const brandsQuery = useBrands();
  const categories = categoriesQuery.data ?? [];
  const brands = brandsQuery.data ?? [];
  const selectedCategoryId = categoryId || (categories[0] ? String(categories[0].id) : "");
  const selectedBrandId = brandId || (brands[0] ? String(brands[0].id) : "");

  const closeForm = () => {
    setErrorMsg(null);
    onClose();
  };

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !sku.trim() || !basePrice || Number(basePrice) <= 0 || !selectedCategoryId || !selectedBrandId) {
      setErrorMsg("Enter a SKU, product name, positive unit price, category, and brand.");
      return;
    }
    if (categories.length === 0 || brands.length === 0) {
      setErrorMsg("Add at least one category and one brand before creating a product.");
      return;
    }

    try {
      await createProduct.mutateAsync({
        sku: sku.trim().toUpperCase(),
        name: name.trim(),
        description: description.trim(),
        categoryId: Number(selectedCategoryId),
        brandId: Number(selectedBrandId),
        basePrice: Number(basePrice),
      });
      onSuccess?.();
      closeForm();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Failed to register product";
      setErrorMsg(msg);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div className="max-h-[92dvh] w-full max-w-md overflow-y-auto rounded-lg border border-border/70 bg-card p-4 shadow-2xl sm:p-6">
        <div className="flex items-center justify-between border-b border-border/40 pb-3">
          <h2 className="text-sm font-semibold tracking-tight text-foreground">
            Register New Inventory Asset
          </h2>
          <button
            type="button"
            onClick={closeForm}
            aria-label="Close product form"
            className="rounded p-2 text-muted-foreground hover:bg-foreground/[0.05] hover:text-foreground"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <p className="mt-3 text-xs leading-relaxed text-muted-foreground">
          This adds the product to Inventory Assets and Overview. Add its on-hand quantity afterward with Adjust stock.
        </p>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="mb-1 block text-xs font-medium text-foreground/80">
              SKU Identifier
            </label>
            <input
              type="text"
              aria-label="SKU identifier"
              value={sku}
              onChange={(e) => setSku(e.target.value)}
              placeholder="e.g. SKU-1042"
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
              aria-label="Product title"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Ultrasonic Flow Meter 24V"
              className={`${FIELD} w-full`}
              required
            />
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <label htmlFor="product-category" className="mb-1 block text-xs font-medium text-foreground/80">
                Category
              </label>
              <Select value={selectedCategoryId} onValueChange={setCategoryId} disabled={categoriesQuery.isLoading || categories.length === 0}>
                <SelectTrigger id="product-category" className={`${FIELD} w-full`} aria-label="Category">
                  <SelectValue placeholder={categoriesQuery.isLoading ? "Loading categories…" : "Select a category"} />
                </SelectTrigger>
                <SelectContent className="border-white/10 bg-[#101214] text-[#e8eaed]">
                  {categories.map((category) => (
                    <SelectItem key={category.id} value={String(category.id)} className="focus:bg-white/[0.08] focus:text-white">
                      {category.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label htmlFor="product-brand" className="mb-1 block text-xs font-medium text-foreground/80">
                Brand
              </label>
              <Select value={selectedBrandId} onValueChange={setBrandId} disabled={brandsQuery.isLoading || brands.length === 0}>
                <SelectTrigger id="product-brand" className={`${FIELD} w-full`} aria-label="Brand">
                  <SelectValue placeholder={brandsQuery.isLoading ? "Loading brands…" : "Select a brand"} />
                </SelectTrigger>
                <SelectContent className="border-white/10 bg-[#101214] text-[#e8eaed]">
                  {brands.map((brand) => (
                    <SelectItem key={brand.id} value={String(brand.id)} className="focus:bg-white/[0.08] focus:text-white">
                      {brand.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="sm:col-span-2">
              <label htmlFor="product-price" className="mb-1 block text-xs font-medium text-foreground/80">
                Unit Base Price ($)
              </label>
              <input
                id="product-price"
                type="number"
                step="0.01"
                min="0.01"
                value={basePrice}
                onChange={(e) => setBasePrice(e.target.value)}
                placeholder="Enter actual unit price"
                className={`${FIELD} w-full font-mono`}
                required
              />
            </div>
          </div>

          {!categoriesQuery.isLoading && !brandsQuery.isLoading && (categories.length === 0 || brands.length === 0) && (
            <p className="rounded-md border border-amber-500/20 bg-amber-500/[0.06] p-2.5 text-xs leading-relaxed text-amber-200/90">
              Add at least one category and one brand before registering products. You can create them through the Categories and Brands API endpoints.
            </p>
          )}

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

          {(errorMsg || categoriesQuery.isError || brandsQuery.isError) && (
            <div className="flex items-center gap-2 rounded-md border border-rose-500/30 bg-rose-500/10 p-2.5 text-xs text-rose-400">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{errorMsg ?? "Couldn't load categories or brands. Refresh and try again."}</span>
            </div>
          )}

          <div className="flex flex-col-reverse justify-end gap-2 pt-2 sm:flex-row">
            <button type="button" onClick={closeForm} className={BUTTON}>
              Cancel
            </button>
            <button
              type="submit"
              disabled={createProduct.isPending || categories.length === 0 || brands.length === 0}
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
