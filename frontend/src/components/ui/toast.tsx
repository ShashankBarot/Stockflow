"use client";

import * as React from "react";
import * as ToastPrimitive from "@radix-ui/react-toast";
import { XIcon } from "lucide-react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

/**
 * Toast — design-system notification component.
 *
 * Variants:
 *   default     → neutral system message
 *   success     → operation completed
 *   warning     → non-blocking warning
 *   destructive → error / destructive action result
 *
 * Rules:
 * - Toasts appear bottom-right. For transient feedback only.
 * - Use inline error messages for form validation — not toasts.
 * - No exclamation marks in success messages.
 * - Active voice: "Product saved." not "Your product has been saved!"
 */

const ToastProvider = ToastPrimitive.Provider;

const ToastViewport = React.forwardRef<
  React.ComponentRef<typeof ToastPrimitive.Viewport>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitive.Viewport>
>(({ className, ...props }, ref) => (
  <ToastPrimitive.Viewport
    ref={ref}
    className={cn(
      "fixed bottom-0 right-0 z-[60]",
      "flex max-h-screen flex-col-reverse gap-2",
      "p-4 sm:max-w-[380px]",
      className
    )}
    {...props}
  />
));
ToastViewport.displayName = ToastPrimitive.Viewport.displayName;

const toastVariants = cva(
  [
    "group pointer-events-auto relative flex w-full items-start gap-3",
    "overflow-hidden rounded-[--radius-lg]",
    "border border-[--color-border]",
    "bg-[--color-surface] shadow-[--shadow-lg]",
    "px-4 py-3",
    "transition-all duration-200 ease-out",
    "data-[swipe=cancel]:translate-x-0",
    "data-[swipe=end]:translate-x-[var(--radix-toast-swipe-end-x)]",
    "data-[swipe=move]:translate-x-[var(--radix-toast-swipe-move-x)]",
    "data-[swipe=move]:transition-none",
    "data-[state=open]:animate-in",
    "data-[state=closed]:animate-out",
    "data-[state=closed]:fade-out-80",
    "data-[state=closed]:slide-out-to-right-full",
    "data-[state=open]:slide-in-from-bottom-full",
  ].join(" "),
  {
    variants: {
      variant: {
        default: "",
        success:     "border-l-2 border-l-[--color-success]",
        warning:     "border-l-2 border-l-[--color-warning]",
        destructive: "border-l-2 border-l-[--color-danger]",
      },
    },
    defaultVariants: { variant: "default" },
  }
);

const Toast = React.forwardRef<
  React.ComponentRef<typeof ToastPrimitive.Root>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitive.Root> &
    VariantProps<typeof toastVariants>
>(({ className, variant, ...props }, ref) => (
  <ToastPrimitive.Root
    ref={ref}
    className={cn(toastVariants({ variant }), className)}
    {...props}
  />
));
Toast.displayName = ToastPrimitive.Root.displayName;

const ToastAction = React.forwardRef<
  React.ComponentRef<typeof ToastPrimitive.Action>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitive.Action>
>(({ className, ...props }, ref) => (
  <ToastPrimitive.Action
    ref={ref}
    className={cn(
      "inline-flex shrink-0 items-center justify-center",
      "rounded-[--radius-md] border border-[--color-border]",
      "bg-transparent px-3 h-7 text-xs font-medium",
      "text-[--color-ink]",
      "transition-colors duration-100",
      "hover:bg-[--color-recessed]",
      "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[--color-accent]",
      "disabled:pointer-events-none disabled:opacity-50",
      className
    )}
    {...props}
  />
));
ToastAction.displayName = ToastPrimitive.Action.displayName;

const ToastClose = React.forwardRef<
  React.ComponentRef<typeof ToastPrimitive.Close>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitive.Close>
>(({ className, ...props }, ref) => (
  <ToastPrimitive.Close
    ref={ref}
    toast-close=""
    className={cn(
      "ml-auto -mr-1 flex shrink-0 h-6 w-6 items-center justify-center",
      "rounded-[--radius-md] text-[--color-muted]",
      "transition-colors duration-100",
      "hover:text-[--color-ink]",
      "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[--color-accent]",
      className
    )}
    aria-label="Close"
    {...props}
  >
    <XIcon className="size-3.5" />
  </ToastPrimitive.Close>
));
ToastClose.displayName = ToastPrimitive.Close.displayName;

const ToastTitle = React.forwardRef<
  React.ComponentRef<typeof ToastPrimitive.Title>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitive.Title>
>(({ className, ...props }, ref) => (
  <ToastPrimitive.Title
    ref={ref}
    className={cn("text-sm font-semibold text-[--color-ink] leading-snug", className)}
    {...props}
  />
));
ToastTitle.displayName = ToastPrimitive.Title.displayName;

const ToastDescription = React.forwardRef<
  React.ComponentRef<typeof ToastPrimitive.Description>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitive.Description>
>(({ className, ...props }, ref) => (
  <ToastPrimitive.Description
    ref={ref}
    className={cn("text-xs text-[--color-muted] leading-relaxed mt-0.5", className)}
    {...props}
  />
));
ToastDescription.displayName = ToastPrimitive.Description.displayName;

type ToastProps = React.ComponentPropsWithoutRef<typeof Toast>;
type ToastActionElement = React.ReactElement<typeof ToastAction>;

export {
  type ToastProps,
  type ToastActionElement,
  ToastProvider,
  ToastViewport,
  Toast,
  ToastTitle,
  ToastDescription,
  ToastClose,
  ToastAction,
};

