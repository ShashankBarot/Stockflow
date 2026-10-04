"use client";

import { useState, useCallback, useRef } from "react";

export interface PointedData {
  index: number;
  value: number;
  label?: string;
  xPercent: number;
  yPercent: number;
}

/**
 * Hook for tracking pointed/hovered point along a chart or list.
 */
export function usePointed<T = unknown>() {
  const [pointed, setPointed] = useState<PointedData | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);

  const handlePointerMove = useCallback((e: React.PointerEvent<HTMLDivElement>, totalPoints: number) => {
    if (!containerRef.current || totalPoints <= 0) return;
    const rect = containerRef.current.getBoundingClientRect();
    const clientX = e.clientX - rect.left;
    const ratio = Math.max(0, Math.min(1, clientX / rect.width));
    const index = Math.min(totalPoints - 1, Math.round(ratio * (totalPoints - 1)));
    setPointed({
      index,
      value: 0,
      xPercent: ratio * 100,
      yPercent: 50,
    });
  }, []);

  const handlePointerLeave = useCallback(() => {
    setPointed(null);
  }, []);

  return {
    pointed,
    setPointed,
    containerRef,
    handlePointerMove,
    handlePointerLeave,
  };
}
