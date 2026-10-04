"use client";

import * as React from "react";
import { cn } from "@/lib/utils";

/**
 * FormField — composes Label + Input/Select + HelperText + ErrorText.
 *
 * This is the single reusable form composition pattern.
 * Always use this instead of assembling label/input/error separately.
 *
 * Usage:
 *   <FormField
 *     label="SKU"
 *     htmlFor="sku"
 *     required
 *     error="SKU is required"
 *     hint="Unique product identifier"
 *   >
 *     <Input id="sku" error={!!error} ... />
 *   </FormField>
 */

interface FormFieldProps {
  label: string;
  htmlFor?: string;
  required?: boolean;
  /** Helper text shown below the input. Hidden when error is present. */
  hint?: string;
  /** Error message. When provided, renders in danger colour. */
  error?: string;
  children: React.ReactNode;
  className?: string;
}

export function FormField({
  label,
  htmlFor,
  required,
  hint,
  error,
  children,
  className,
}: FormFieldProps) {
  return (
    <div className={cn("flex flex-col gap-1.5", className)}>
      <label
        htmlFor={htmlFor}
        className="text-sm font-medium text-[--color-ink] leading-none"
      >
        {label}
        {required && (
          <span
            aria-hidden="true"
            className="ml-1 text-[--color-danger]"
          >
            *
          </span>
        )}
      </label>
      {children}
      {error ? (
        <p className="text-xs text-[--color-danger] leading-snug" role="alert">
          {error}
        </p>
      ) : hint ? (
        <p className="text-xs text-[--color-muted] leading-snug">{hint}</p>
      ) : null}
    </div>
  );
}

/**
 * FormSection — visually groups related form fields.
 *
 * Usage:
 *   <FormSection title="Product details" description="Basic identification">
 *     <FormField ... />
 *   </FormSection>
 */
interface FormSectionProps {
  title?: string;
  description?: string;
  children: React.ReactNode;
  className?: string;
}

export function FormSection({
  title,
  description,
  children,
  className,
}: FormSectionProps) {
  return (
    <div className={cn("flex flex-col gap-4", className)}>
      {(title || description) && (
        <div className="flex flex-col gap-0.5 pb-3 border-b border-[--color-border]">
          {title && (
            <h3 className="text-sm font-semibold text-[--color-ink]">{title}</h3>
          )}
          {description && (
            <p className="text-xs text-[--color-muted]">{description}</p>
          )}
        </div>
      )}
      {children}
    </div>
  );
}

/**
 * FormActions — positions primary and secondary actions at the bottom of a form.
 * Aligns actions to the right, secondary first (natural reading order for keyboard nav).
 */
interface FormActionsProps {
  children: React.ReactNode;
  className?: string;
}

export function FormActions({ children, className }: FormActionsProps) {
  return (
    <div
      className={cn(
        "flex items-center justify-end gap-2",
        "pt-4 border-t border-[--color-border]",
        className
      )}
    >
      {children}
    </div>
  );
}

