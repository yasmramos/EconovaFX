# DestDB Benchmark Module

Este módulo contiene los benchmarks de rendimiento para DestDB utilizando JMH (Java Microbenchmark Harness).

## Estructura

```
destdb-benchmark/
├── pom.xml                 # Configuración Maven con Shade Plugin
├── settings.xml            # Configuración de proxy y repositorios
└── src/
    └── main/
        ├── java/
        │   └── com/econovafx/benchmark/
        │       └── ...     # Clases de benchmark
        └── resources/
            └── ...         # Recursos de configuración
```

## Cómo usar

### Compilar el módulo

```bash
cd destdb-benchmark
mvn clean package
```

Esto generará un JAR ejecutable con todas las dependencias incluidas en:
```
target/destdb-benchmark-1.0.0-benchmarks.jar
```

### Ejecutar todos los benchmarks

```bash
java -jar target/destdb-benchmark-1.0.0-benchmarks.jar
```

### Ejecutar un benchmark específico

```bash
java -jar target/destdb-benchmark-1.0.0-benchmarks.jar NombreDelBenchmark
```

### Ejecutar con opciones específicas

```bash
# Ejecutar con 5 iteraciones de warmup y 10 iteraciones de medición
java -jar target/destdb-benchmark-1.0.0-benchmarks.jar -wi 5 -i 10

# Ejecutar con formato CSV
java -jar target/destdb-benchmark-1.0.0-benchmarks.jar -rff results.csv

# Ver ayuda completa
java -jar target/destdb-benchmark-1.0.0-benchmarks.jar -h
```

## Crear un nuevo benchmark

1. Crea una nueva clase en `src/main/java/com/econovafx/benchmark/`

2. Usa las anotaciones de JMH:

```java
package com.econovafx.benchmark;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class MiBenchmark {

    @Setup
    public void setup() {
        // Inicialización antes de cada ejecución
    }

    @Benchmark
    public void testOperacion() {
        // Código a benchmarkear
    }
}
```

3. Compila y ejecuta:

```bash
mvn clean package
java -jar target/destdb-benchmark-1.0.0-benchmarks.jar MiBenchmark
```

## Dependencias principales

- **JMH 1.37**: Framework para benchmarks en Java
- **Ebean ORM 17.11.0**: ORM para pruebas de base de datos
- **H2 Database 2.2.224**: Base de datos embebida para tests
- **Logback 1.4.14**: Logging

## Notas importantes

- Los benchmarks deben ser independientes y no compartir estado entre ejecuciones
- Usa `@State` apropiadamente (Thread, Benchmark, o Scope)
- Evita optimizaciones del compilador usando los valores retornados
- El Shade Plugin asegura que todas las dependencias estén incluidas en el JAR final
