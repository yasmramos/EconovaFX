// utilidades mínimas equivalentes a `cn()` de shadcn
export function cn(...classes) {
  return classes.flat(Infinity).filter(Boolean).join(' ');
}
