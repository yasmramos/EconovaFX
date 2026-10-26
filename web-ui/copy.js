// Copia el build estático a ../src/main/resources/web (multiplataforma)
import { cpSync, rmSync, mkdirSync } from 'node:fs';
import { resolve } from 'node:path';

const src = resolve('build');
const dest = resolve('../src/main/resources/web');

mkdirSync(dest, { recursive: true });
// limpia el destino excepto lo que no venga del build (por seguridad, dejamos todo)
try {
  rmSync(dest, { recursive: true, force: true });
  mkdirSync(dest, { recursive: true });
} catch {}
cpSync(src, dest, { recursive: true });
console.log('Copiado build →', dest);
