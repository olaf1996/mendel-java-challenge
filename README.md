Java Code Challenge
Nos gustaría tener un servicio web RESTful que almacene transacciones (en memoria está bien) y
devuelva información sobre esas transacciones.
Las transacciones a almacenar tienen un tipo y un monto. El servicio debe admitir la devolución de
todas las transacciones para su tipo. Además, las transacciones se pueden vincular entre sí (usando un
"parent_id") y, por otro lado, necesitamos saber el monto total involucrado para todas las transacciónes
vinculadas a una transacción en particular.

1. Por favor, completar en Spring Boot (Java o Kotlin) y en no mas de 3 días consecutivos.
2. Completar el proyecto en Bitbucket o Github, para que podamos revisar el código.
3. No usar SQL.
   Requerido:
   • Tests de integración.
   • Aplicación dockerizada.
   • Java 11 o superior.
   • Claridad del código.
   • Correctitud en diseño de arquitectura.
   Se valorará positivamente:
   • Uso de TDD.
   • Desarrollo incremental de la solución mediante el uso de commits.
   • Aplicación de los principios SOLID.
   • Documentacion.

Mendel Java Code Challenge 2
Especificación del servicio
PUT /transactions/$transaction_id
1
2 Body:
3
4 { "amount":double,"type":string,"parent_id":long }
5
6
Codigo 1: PUT transaction
En dónde:
• transaction_id Es de tipo 'long' identificador de una nueva transacción.
• amount Es de tipo 'double' espcificando el monto.
• type es un 'string' que identifica el tipo de la transacción.
• parent_id Es de tipo 'long', opcional. El cual especifica el id de la transacción padre.
GET /transactions/types/$type
1
2 Returns:
3
4 [ long, long, .... ]
5
6
Codigo 2: GET by type
Una lista json de todos los ids de las transacciones para el tipo especificado.
GET /transactions/sum/$transaction_id
1
2 Returns:
3
4 { "sum", double }
5
6
Codigo 3: GET SUM
La suma de todas las transacciones que estan transitivamente conectadas por su parent_id a
$transaction_id.
Prohibida su difusión, publicación o distribución, directa o indirectamente.

Mendel Java Code Challenge 3
Algunos ejemplos simples podrian ser:
1
2 PUT /transactions/10 { "amount": 5000, "type": "cars" } => { "status": "ok" }
3
4 PUT /transactions/11 { "amount": 10000, "type": "shopping", "parent_id": 10 }
5 PUT /transactions/12 { "amount": 5000, "type": "shopping", "parent_id": 11 }
6
7 GET /transactions/types/cars => [10]
8 GET /transactions/sum/10 => {"sum":20000}
9 GET /transactions/sum/11 => {"sum":15000}
10
11
Codigo 4: Ejemplos
Prohibida su difusión, publicación o distribución, directa o indirectamente.

---

## Cómo ejecutar

### Requisitos

- **Java 21** (o superior; el proyecto está configurado con Java 21).
- **Maven 3.6+** (para ejecutar sin Docker).
- **Docker** y **Docker Compose** (solo si querés levantar la app en contenedor).

### Con Maven

```bash
# Compilar y ejecutar tests
mvn clean test

# Levantar la aplicación (puerto 8080)
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

### Con Docker

```bash
# Construir y levantar el servicio
docker compose up --build

```

El servicio escucha en el puerto **8080**. Para detener: `docker compose down`.

---

## Decisiones de diseño

- **`parent_id` inexistente:** Si en el body del PUT se envía un `parent_id` que no corresponde a ninguna transacción guardada, la API responde **400 Bad Request** con un mensaje explícito. Así se evitan referencias rotas y datos inconsistentes.

- **Ciclos en `parent_id`:** No se permiten. El grafo de transacciones se modela como un conjunto de árboles: cada transacción tiene como máximo un padre y no puede ser ancestro de sí misma. Al crear o actualizar, se valida que asignar ese `parent_id` no forme un ciclo; si se detecta, se responde **400 Bad Request**.

- **GET `/transactions/sum/{id}` con id inexistente:** Se responde **404 Not Found**. La transacción no existe. Devolver 200 con `sum: 0` podría ocultar errores del cliente (por ejemplo, un id mal escrito).

### Validación de entrada

- **Campos obligatorios:** `amount` y `type` son obligatorios en el body del PUT. Si faltan o son inválidos, se responde **400** con detalle por campo (Bean Validation).

- **`type` no vacío:** El campo `type` no puede ser null, vacío ni solo espacios. En ese caso **400 Bad Request**.

- **`amount` finito:** No se aceptan `NaN` ni infinito para `amount`. Se responde **400** con mensaje claro.

- **IDs no negativos:** Tanto `transaction_id` (en la URL) como `parent_id` (en el body, cuando se envía) deben ser ≥ 0. Valores negativos devuelven **400**.

- **Path numérico:** Los parámetros de path `transaction_id` (PUT y GET sum) deben ser números válidos (long). Si se envía algo no numérico (ej. `/transactions/abc`), se responde **400** con mensaje indicando que debe ser un número válido, en lugar de 500.

- **JSON válido:** Si el body del PUT no es JSON válido o no se puede deserializar, se responde **400 Bad Request** con un mensaje de error de parsing, sin exponer detalles internos innecesarios.

### Respuestas de error

- **Cuerpo unificado:** Todas las respuestas de error 4xx (y en general cualquier error manejado) usan el mismo formato: `{"status": <código>, "error": "<frase HTTP>", "message": "<detalle>"}`. Así los clientes pueden tratar los errores de forma uniforme.

- **Manejo centralizado:** Un único `@RestControllerAdvice` traduce excepciones de negocio y de framework (validación, JSON, tipo de path) a respuestas HTTP coherentes.

### Persistencia y concurrencia

- **Almacenamiento en memoria:** Sin base de datos, tal como indica el enunciado. Los datos se pierden al reiniciar la aplicación.

- **Thread-safety:** El repositorio en memoria usa `ConcurrentHashMap` e índices concurrentes para que las peticiones simultáneas no corrompan datos ni provoquen condiciones de carrera.
