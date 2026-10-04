"use client";

import * as React from "react";
import {
  ToastProvider,
  ToastViewport,
  Toast,
  ToastTitle,
  ToastDescription,
  ToastClose,
  ToastAction,
  type ToastProps,
  type ToastActionElement,
} from "@/components/ui/toast";

/**
 * useToast — imperative toast API.
 *
 * Usage (in any client component):
 *   const { toast } = useToast();
 *   toast({ title: "Product saved.", variant: "success" });
 *   toast({ title: "Could not delete product.", variant: "destructive" });
 *
 * Rules:
 * - Titles use active voice, no exclamation marks.
 * - Use "destructive" for errors, not generic "default" with red text.
 * - Max 1 toast visible at a time per category.
 */

// ── Types ──────────────────────────────────────────────────────────────────

type ToastPayload = Omit<ToastProps, "id"> & {
  title: string;
  description?: string;
  action?: ToastActionElement;
};

type ToastItem = ToastPayload & {
  id: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
};

// ── State ──────────────────────────────────────────────────────────────────

const TOAST_LIMIT = 3;
const TOAST_REMOVE_DELAY = 200; // ms — matches animate-out duration

type State = { toasts: ToastItem[] };
type Action =
  | { type: "ADD_TOAST"; payload: ToastItem }
  | { type: "DISMISS_TOAST"; id: string }
  | { type: "REMOVE_TOAST"; id: string };

let count = 0;
function genId() {
  count = (count + 1) % Number.MAX_SAFE_INTEGER;
  return String(count);
}

const listeners: Array<(state: State) => void> = [];
let memoryState: State = { toasts: [] };

function dispatch(action: Action) {
  memoryState = reducer(memoryState, action);
  listeners.forEach((l) => l(memoryState));
}

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case "ADD_TOAST":
      return {
        toasts: [action.payload, ...state.toasts].slice(0, TOAST_LIMIT),
      };
    case "DISMISS_TOAST": {
      const { id } = action;
      setTimeout(() => dispatch({ type: "REMOVE_TOAST", id }), TOAST_REMOVE_DELAY);
      return {
        toasts: state.toasts.map((t) =>
          t.id === id ? { ...t, open: false } : t
        ),
      };
    }
    case "REMOVE_TOAST":
      return { toasts: state.toasts.filter((t) => t.id !== action.id) };
    default:
      return state;
  }
}

// ── Hook ───────────────────────────────────────────────────────────────────

function toast(payload: ToastPayload) {
  const id = genId();
  const dismiss = () => dispatch({ type: "DISMISS_TOAST", id });

  dispatch({
    type: "ADD_TOAST",
    payload: {
      ...payload,
      id,
      open: true,
      onOpenChange: (open) => {
        if (!open) dismiss();
      },
    },
  });

  return { id, dismiss };
}

function useToast() {
  const [state, setState] = React.useState<State>(memoryState);

  React.useEffect(() => {
    listeners.push(setState);
    return () => {
      const idx = listeners.indexOf(setState);
      if (idx > -1) listeners.splice(idx, 1);
    };
  }, []);

  return {
    toasts: state.toasts,
    toast,
    dismiss: (id: string) => dispatch({ type: "DISMISS_TOAST", id }),
  };
}

// ── Toaster — renders the toast viewport ──────────────────────────────────

function Toaster() {
  const { toasts } = useToast();

  return (
    <ToastProvider>
      {toasts.map(({ id, title, description, action, ...props }) => (
        <Toast key={id} {...props}>
          <div className="flex flex-col gap-0.5 flex-1 min-w-0">
            {title && <ToastTitle>{title}</ToastTitle>}
            {description && <ToastDescription>{description}</ToastDescription>}
          </div>
          {action && <ToastAction altText="Action">{action}</ToastAction>}
          <ToastClose />
        </Toast>
      ))}
      <ToastViewport />
    </ToastProvider>
  );
}

export { useToast, toast, Toaster };

