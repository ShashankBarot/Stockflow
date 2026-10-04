"use client";

import * as React from "react";
import { MoreHorizontal } from "lucide-react";
import { ICON_BUTTON } from "./control-classes";

export interface RowActionItem {
  label: string;
  onClick: () => void;
  icon?: React.ReactNode;
  danger?: boolean;
}

interface RowActionProps {
  items: RowActionItem[];
}

export function RowAction({ items }: RowActionProps) {
  const [open, setOpen] = React.useState(false);
  const menuRef = React.useRef<HTMLDivElement | null>(null);

  React.useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    if (open) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  return (
    <div className="relative inline-block text-left" ref={menuRef}>
      <button
        type="button"
        onClick={(e) => {
          e.stopPropagation();
          setOpen(!open);
        }}
        className={ICON_BUTTON}
        aria-label="Row actions"
      >
        <MoreHorizontal className="h-3.5 w-3.5" />
      </button>

      {open && (
        <div className="absolute right-0 z-50 mt-1 w-36 origin-top-right rounded-md border border-border/70 bg-popover p-1 shadow-md focus:outline-none">
          {items.map((item, idx) => (
            <button
              key={idx}
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                setOpen(false);
                item.onClick();
              }}
              className={`flex w-full items-center gap-2 rounded px-2.5 py-1.5 text-xs text-left transition-colors ${
                item.danger
                  ? "text-rose-400 hover:bg-rose-500/10"
                  : "text-foreground hover:bg-foreground/[0.08]"
              }`}
            >
              {item.icon}
              {item.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

export function useRowExit() {
  const [exitingIds, setExitingIds] = React.useState<Set<string | number>>(new Set());

  const triggerExit = React.useCallback((id: string | number, onComplete?: () => void) => {
    setExitingIds((prev) => new Set(prev).add(id));
    setTimeout(() => {
      onComplete?.();
      setExitingIds((prev) => {
        const next = new Set(prev);
        next.delete(id);
        return next;
      });
    }, 250);
  }, []);

  return { exitingIds, triggerExit };
}
