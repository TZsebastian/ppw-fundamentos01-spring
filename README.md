# Programación y Plataformas Web

## Práctica 1: Instalación, Configuración Inicial y Primer Endpoint

**Estudiante:** Sebastian Zurita  
**Carrera:** Ingeniería en Ciencias de la Computación  
**Institución:** Universidad Politécnica Salesiana  

---

## 1. Introducción al Framework
Spring Boot es un framework moderno diseñado bajo la filosofía *opinionated*, lo que significa que proporciona configuraciones por defecto para simplificar el inicio de proyectos Java orientados a la web. Su principal ventaja es la inclusión de servidores HTTP embebidos, permitiendo que las aplicaciones se ejecuten de forma autónoma (stand-alone) sin necesidad de despliegues externos complejos.

---

## 10. Resultados y Evidencias

### 1. Captura de verificación de Java
Verificación de la instalación del entorno de ejecución Java en su versión estable 17 (Eclipse Temurin LTS), asegurando la compatibilidad con el ecosistema del proyecto.



![Verificación de Java 17](/assets/javaVersion.png)

### 2. Captura del servidor Spring Boot ejecutándose
Inicialización de la aplicación utilizando el wrapper de Gradle. Se evidencia el banner oficial de Spring Boot y el levantamiento automático del servidor Tomcat embebido en el puerto por defecto.

![Boot](/assets/boot.png)

### 3. Captura del endpoint /api/status funcionando
Prueba de conectividad y respuesta HTTP desde el cliente (navegador web) hacia el endpoint configurado, retornando la estructura de datos correspondiente en formato JSON.

URL de acceso: http://localhost:8080/api/status

![api](/assets/api.png)

### 4. Captura del comando de verificación en terminal
Listado del directorio de controladores para comprobar la correcta ubicación y existencia del archivo fuente que maneja la lógica de las peticiones HTTP.

![api](/assets/ls.png)

## Práctica 3: API Rest

**Estudiante:** Sebastian Zurita  
**Carrera:** Ingeniería en Ciencias de la Computación  
**Institución:** Universidad Politécnica Salesiana  

## Evidencias 18/06

### 1.- Localhost del nuevo recurso Students:

![apiStudents](/assets/apistudents.png)


### 2.- Students/count:

![apiStudentsCount](/assets/apistudentscount.png)


## Práctica 5: Persistencia real con PostgreSQL, Entidades JPA y Repositorios

**Estudiante:** Sebastian Zurita  
**Carrera:** Ingeniería en Ciencias de la Computación  
**Institución:** Universidad Politécnica Salesiana  

---

## 1. Introducción

En esta práctica se reemplazó el almacenamiento en memoria (ArrayList) por una base de datos real usando PostgreSQL, Spring Data JPA e Hibernate. Se implementó el módulo `products` con entidades JPA, repositorios y mappers siguiendo la misma arquitectura del módulo `users`.

---

## 10. Resultados y Evidencias

### 1. Aplicación Docker Desktop
Software de Docker funcionando con el contenedor `postgres-dev` activo.

![Docker Desktop](/assets/dockerdesk.png)

### 2. Verificación en PostgreSQL — usuarios
Verificación de la tabla `users` creada automáticamente por Hibernate al iniciar la aplicación.

![Verificación usuarios PostgreSQL](/assets/postuser.png)

### 3. Creación de clases en `products`

Se implementó la misma arquitectura del módulo `users`:
- `ProductEntity` — extiende `BaseEntity`, representa la tabla `products`
- `ProductRepository` — extiende `JpaRepository`
- `ProductMapper` — convierte entre DTOs, modelos y entidades
- `ProductServiceImpl` — usa el repositorio, sin lista en memoria
- `ProductsController` — delega al servicio

### Evidencias

#### 3.1 Inserción de 5 productos desde Bruno
Se insertaron 5 registros de productos a través de Bruno usando el método POST.

![Inserción desde Bruno](/assets/bruno.png)

#### 3.2 Visualización de productos — GET /api/products
Listado completo de los 5 productos retornados por la API REST.

![Lista de productos](/assets/apiproduct.png)

#### 3.3 Visualización de producto específico — GET /api/products/3
Consulta de un producto por id retornado por la API REST.

![Producto específico](/assets/apiproduct3.png)

#### 3.4 Verificación en PostgreSQL — productos
Consulta `SELECT * FROM products` mostrando los 5 registros almacenados en la base de datos.

![Verificación productos PostgreSQL](/assets/postproduct.png)

---
### 4. Explicar el flujo:

**Desde API REST hacia PostgreSQL:**

El cliente envía una petición HTTP con un JSON, el ProductsController recibe un objeto de transferencia de datos (CreateProductDto). Este DTO es enviado al ProductServiceImpl, el cual utiliza el componente ProductMapper para transformar los datos de entrada al ProductModel y posteriormente a ProductEntity. El servicio llama al método `.save()` de ProductRepository, lo que provoca que Hibernate convierta la entidad en una sentencia SQL INSERT que se ejecuta en el contenedor de PostgreSQL. Durante este proceso, los interceptores de la clase abstracta BaseEntity (como `@PrePersist`) se encargan de establecer automáticamente los valores de auditoría, como la fecha de creación en createdAt y el estado lógico de eliminación.

**De PostgreSQL hacia la API REST:**

Una vez realizada la inserción, PostgreSQL genera el IDENTITY correspondiente. Hibernate toma este registro y lo convierte de vuelta en un objeto ProductEntity. El procesamiento regresa a la capa de servicio, donde el ProductMapper se encarga de transformar la entidad en un ProductResponseDto. Este DTO es el que finalmente devuelve el controlador al cliente en formato JSON, ocultando las propiedades internas de la base de datos y exponiendo únicamente la información necesaria.

## Práctica 6: Validación de DTOs y Control de Datos de Entrada

**Estudiante:** Sebastian Zurita  
**Carrera:** Ingeniería en Ciencias de la Computación  
**Institución:** Universidad Politécnica Salesiana  

---

## 1. Introducción

En esta práctica se implementó la validación de datos de entrada usando Jakarta Validation sobre el módulo `products`. Se agregaron anotaciones de validación a los DTOs, se activó `@Valid` en el controlador y se implementaron reglas de negocio en el servicio para proteger la integridad de los datos antes de que lleguen a PostgreSQL.


## 2. DTOs con validación

Se aplicaron las siguientes reglas mínimas:

| Campo   | Regla                                      |
|---------|--------------------------------------------|
| `name`  | obligatorio, mínimo 3, máximo 150 caracteres |
| `price` | obligatorio, mínimo 0                      |
| `stock` | obligatorio, mínimo 0                      |

- **`CreateProductDto`** y **`UpdateProductDto`**: todos los campos obligatorios (`@NotBlank`, `@NotNull`, `@Min`, `@Size`).
- **`PartialUpdateProductDto`**: sin `@NotBlank` ni `@NotNull` porque los campos son opcionales en un PATCH. Solo se validan formato y rango si el campo es enviado.

---

## 3. Validación en el controlador

Se agregó `@Valid` antes de `@RequestBody` en los endpoints `POST`, `PUT` y `PATCH`:

```java
@PostMapping
public ProductResponseDto create(@Valid @RequestBody CreateProductDto dto) {
    return service.create(dto);
}
```

---

## 4. Reglas de negocio en el servicio

Se implementaron tres reglas de negocio en `ProductServiceImpl`:

1. **`findAll` no devuelve productos eliminados** — se filtra con `.filter(p -> !p.isDeleted())`.
2. **No se puede actualizar un producto eliminado** — se verifica `isDeleted()` en `update` y `partialUpdate` antes de aplicar cambios.
3. **No se puede eliminar un producto ya eliminado** — se verifica `isDeleted()` en `delete` antes de hacer el soft delete.

## 5. Resultados y Evidencias

### 1. POST con datos inválidos — 400 Bad Request

Se envió un body con `name` vacío, `price` negativo y `stock` negativo. Spring Boot rechazó la petición con un **400 Bad Request** antes de llegar al servicio, detallando los 4 errores de validación encontrados.

![POST inválido — 400 Bad Request](/assets/post_invalido.png)

### 2. PATCH con nombre muy corto — 400 Bad Request

Se intentó actualizar parcialmente un producto enviando un `name` de un solo carácter. La validación `@Size(min = 3)` rechazó la petición con **400 Bad Request**.

![PATCH nombre corto — 400 Bad Request](/assets/patch_nombre_corto.png)

### 3. GET /api/products — productos eliminados no aparecen

![Eliminar Producto](/assets/productElimin.png)

![Producto eliminado](/assets/productEliminado.png)

### 4. PUT sobre producto eliminado — error de negocio

Se intentó actualizar un producto con `deleted = true`. El servicio lanzó una `IllegalStateException` con el mensaje *"No se puede actualizar un producto eliminado"*.

![PUT producto eliminado](/assets/putProduct.png)

### 5. DELETE sobre producto ya eliminado — error de negocio

Se intentó eliminar un producto que ya tenía `deleted = true`. El servicio lanzó una `IllegalStateException` con el mensaje *"El producto ya fue eliminado"*.

![DELETE producto ya eliminado](/assets/productDelete.png)