# Proyecto Vaadin: Contact Manager

Aprende Vaadin Framework 25.x paso a paso. Proyecto arrancado desde zip base con Vaadin 25.1.8 + Spring Boot 4. Construyes Contact Manager desde cero.

---

## Proyecto base

Tecnologías ya configuradas:
- Vaadin 25.1.8
- Spring Boot 4.0.7
- Spring Data JPA
- H2 embebida (memoria)
- Java 25

Archivos iniciales:

```
src/main/java/com/example/
├── Application.java              # Entry point + theme config
└── base/ui/
    └── MainLayout.java           # Layout con SideNav

src/main/resources/
├── application.properties
└── META-INF/resources/
    └── styles.css
```

---

## Ejecutar

```bash
mvn spring-boot:run
```

Abrir http://localhost:8080

---

## Fase 1: Crear entidad Contacto

Crea package `com.example.examplefeature` y archivo `Contacto.java`:

```java
package com.example.examplefeature;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "contacto")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "contacto_id")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", length = 150)
    @Nullable
    private String email;

    @Column(name = "telefono", length = 20)
    @Nullable
    private String telefono;

    public Contacto(String nombre, @Nullable String email, @Nullable String telefono) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }
}
```

Concepts:
- `GenerationType.SEQUENCE` — ID autogenerado por sequence de DB
- `@Nullable` de `org.jspecify.annotations` — nullabilidad para compiler
- `protected Contacto()` — constructor requerido por JPA/Hibernate (Lombok genera)
- Lombok: `@Getter @Setter @ToString @EqualsAndHashCode @NoArgsConstructor`
- Constructor 3-args manual — Lombok no puede generarlo sin `final` fields

---

## Fase 2: Crear Repository

```java
package com.example.examplefeature;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

interface ContactoRepository extends JpaRepository<Contacto, Long> {
    Slice<Contacto> findAllBy(Pageable pageable);
    Slice<Contacto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
```

Concepts:
- `JpaRepository<Contacto, Long>` — CRUD auto-implementado
- `Slice<T>` — mejor que `Page<T>` (1 query en vez de 2, sin count)
- Method naming convention: `findByNombreContainingIgnoreCase` = query automática

---

## Fase 3: Crear Service

```java
package com.example.examplefeature;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactoService {

    private final ContactoRepository repo;

    ContactoService(ContactoRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Slice<Contacto> list(Pageable pageable, @Nullable String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return repo.findAllBy(pageable);
        }
        return repo.findByNombreContainingIgnoreCase(filtro, pageable);
    }

    @Transactional
    public Contacto save(Contacto contacto) {
        return repo.saveAndFlush(contacto);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
```

Concepts:
- `@Transactional` — requeridos para operaciones de escritura en JPA
- `@Transactional(readOnly = true)` — optimizado por Hibernate, solo lectura
- `saveAndFlush` — guarda y flush inmediato (sincroniza con DB)
- `Slice<T>` — retorna solo datos de la página, sin total count

---

## Fase 4: Crear Vista principal (ContactoListView)

Crea package `com.example.examplefeature.ui` y archivo `ContactoListView.java`:

```java
package com.example.examplefeature.ui;

import com.example.base.ui.ViewTitle;
import com.example.examplefeature.Contacto;
import com.example.examplefeature.ContactoService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Contactos")
@Menu(order = 0, title = "Contactos")
class ContactoListView extends VerticalLayout {

    private final ContactoService service;
    private Grid<Contacto> grid = new Grid<>();

    ContactoListView(ContactoService service) {
        this.service = service;

        add(new ViewTitle("Contactos"));
        add(createToolbar());

        grid.addColumn(Contacto::getNombre).setHeader("Nombre");
        grid.addColumn(Contacto::getEmail).setHeader("Email");
        grid.addColumn(Contacto::getTelefono).setHeader("Teléfono");
        grid.setEmptyStateText("No hay contactos");
        grid.setSizeFull();

        add(grid);
        setSizeFull();
    }

    private HorizontalLayout createToolbar() {
        return new HorizontalLayout(new Text("Usa start.vaadin.com para filtrado"));
    }
}
```

Concepts:
- `@Route(value = "", layout = MainLayout.class)` — URL `/`, usa MainLayout
- `@Menu(order = 0, title = "Contactos")` — aparece en SideNav
- `extends VerticalLayout` — layout vertical que apila componentes
- `VaadinSpringDataHelpers.toSpringPageRequest` — convierte query de Vaadin a Spring Pageable (para lazy loading)
- `MainLayout` importado automáticamente por `@Layout` en MainLayout.java

**Ahora ejecutá `mvn spring-boot:run`** — deberías ver "Contactos" en el SideNav y Grid vacío.

---

## Fase 5: Agregar formulario de creación

Modificá `ContactoListView.java` — agregá el formulario al layout:

```java
package com.example.examplefeature.ui;

import com.example.base.ui.ViewTitle;
import com.example.examplefeature.Contacto;
import com.example.examplefeature.ContactoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Contactos")
@Menu(order = 0, title = "Contactos")
class ContactoListView extends VerticalLayout {

    private final ContactoService service;
    private Grid<Contacto> grid = new Grid<>();

    private final TextField nombre = new TextField("Nombre");
    private final TextField email = new TextField("Email");
    private final TextField telefono = new TextField("Teléfono");
    private final Button guardar = new Button("Guardar");

    ContactoListView(ContactoService service) {
        this.service = service;

        guardar.addClickListener(e -> guardarContacto());

        HorizontalLayout form = new HorizontalLayout(nombre, email, telefono, guardar);
        form.setAlignItems(FlexComponent.Alignment.END);
        form.setFlexGrow(0, guardar);

        grid.addColumn(Contacto::getNombre).setHeader("Nombre");
        grid.addColumn(Contacto::getEmail).setHeader("Email");
        grid.addColumn(Contacto::getTelefono).setHeader("Teléfono");
        grid.setEmptyStateText("No hay contactos");
        grid.setSizeFull();

        add(new ViewTitle("Contactos"), form, grid);
        setSizeFull();
    }

    private void guardarContacto() {
        if (nombre.getValue().isBlank()) {
            nombre.setInvalid(true);
            nombre.setErrorMessage("Nombre requerido");
            return;
        }

        Contacto contacto = new Contacto(
            nombre.getValue(),
            email.getValue(),
            telefono.getValue()
        );
        service.save(contacto);
        grid.getDataProvider().refreshAll();
        nombre.clear();
        email.clear();
        telefono.clear();
        Notification.show("Contacto guardado", 3000, Notification.Position.BOTTOM_END)
            .addThemeVariants(NotificationVariant.SUCCESS);
    }
}
```

Concepts:
- `TextField` con label — `new TextField("Label")` o `field.setLabel("Label")`
- `setInvalid(true)` + `setErrorMessage(...)` — validación visual del campo
- `grid.getDataProvider().refreshAll()` — fuerza al Grid a recargar datos del backend
- `Notification.show(...)` — toast notification en esquina

---

## Fase 6: Lazy loading con filtro

### Por qué lazy loading?

En la Fase 4-5 el Grid cargaba **todos** los contactos de una vez en memoria. Con muchos datos esto es:
- **Lento**: espera a cargar todo antes de mostrar
- **Costoso**: consume mucha memoria

Con lazy loading, el Grid pide datos **solo para la porción visible** y cuando necesita más (scroll).

### Cambios en ContactoListView.java

**1. Agregar import:**

```java
import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;
```

**2. Agregar campo filtro en la clase:**

```java
private final TextField filtro = new TextField("Buscar");
```

**3. Cambiar cómo el Grid obtiene datos — en el constructor:**

```java
// ANTES (carga todo de una vez):
grid.setItems(service.list(Pageable.unpaged(), null).stream());

// DESPUÉS (carga lazy, solo lo que el Grid necesita):
grid.setItems(query -> service.list(
    toSpringPageRequest(query),    // Vaadin query → Spring Pageable
    filtro.getValue()              // texto del filtro
).stream());
```

**4. Configurar el TextField de filtro — en createToolbar():**

```java
private HorizontalLayout createToolbar() {
    filtro.setPlaceholder("Buscar por nombre...");
    filtro.setClearButtonVisible(true);

    // Cada vez que el usuario escribe, recargar datos del Grid
    filtro.addValueChangeListener(e ->
        grid.getDataProvider().refreshAll()
    );

    return new HorizontalLayout(filtro);
}
```

### Cómo funciona el flujo

```
Usuario escribe "Juan"
    ↓
filtro.addValueChangeListener() dispara
    ↓
grid.getDataProvider().refreshAll()
    ↓
Grid pide datos invocando el callback
    ↓
service.list(toSpringPageRequest(query), "Juan")
    ↓
Repository ejecuta query con filtro LIKE '%Juan%'
    ↓
Grid muestra solo contactos que coinciden
```

### Conversión de query

`VaadinSpringDataHelpers.toSpringPageRequest(query)` convierte:

| Vaadin Query | Spring Pageable |
|--------------|-----------------|
| página 0, size 20 | `PageRequest.of(0, 20)` |
| página 2, size 20 | `PageRequest.of(2, 20)` |
| con orden por nombre | `PageRequest.of(..., Sort.by("nombre"))` |

### Concepts

- `setItems(query -> ...)` — callback de lazy loading. Se ejecuta **cada vez** que el Grid necesita datos (scroll, paginación, refresh)
- `VaadinSpringDataHelpers.toSpringPageRequest(query)` — traduce la query de Vaadin (página actual, tamaño, orden) a `Pageable` de Spring Data
- `addValueChangeListener(e -> ...)` — se dispara cuando el valor del TextField cambia y el usuario presiona Enter o el campo pierde foco
- `setClearButtonVisible(true)` — agrega botón X que limpia el campo y dispara el listener
- `refreshAll()` — le dice al Grid que olvide sus datos en caché y vuelva a invocar el callback

---

## Fase 7: Eliminar contacto

Agregar columna de acciones al Grid:

```java
grid.addComponentColumn(contacto -> {
    Button deleteBtn = new Button("Eliminar", e -> eliminarContacto(contacto));
    return deleteBtn;
}).setHeader("Acciones").setAutoWidth(false);

// Método eliminar:
private void eliminarContacto(Contacto contacto) {
    service.delete(contacto.getId());
    grid.getDataProvider().refreshAll();
    Notification.show("Contacto eliminado", 3000, Notification.Position.BOTTOM_END)
        .addThemeVariants(NotificationVariant.SUCCESS);
}
```

Concepts:
- `addComponentColumn` — para renderizar componentes (Button) en una columna
- `addColumn` — para valores simples (String, etc), llama `toString()`

---

## Fase 8: Editar contacto con Dialog

Crear `ContactoDialog.java` en `com.example.examplefeature.ui`:

```java
package com.example.examplefeature.ui;

import com.example.examplefeature.Contacto;
import com.vaadin.flow.component.Button;
import com.vaadin.flow.component.Dialog;
import com.vaadin.flow.component.TextField;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

public class ContactoDialog extends Dialog {

    private final TextField nombre = new TextField("Nombre");
    private final TextField email = new TextField("Email");
    private final TextField telefono = new TextField("Teléfono");
    private final Binder<Contacto> binder = new Binder<>(Contacto.class);
    private final Contacto contacto;

    public ContactoDialog() {
        this(new Contacto("", null, null));
    }

    public ContactoDialog(Contacto contacto) {
        this.contacto = contacto;

        FormLayout form = new FormLayout();
        form.add(nombre, email, telefono);

        Button guardar = new Button("Guardar", e -> guardar());
        Button cancelar = new Button("Cancelar", e -> close());

        add(form, guardar, cancelar);

        binder.forField(nombre)
            .withValidator(n -> n != null && !n.isBlank(), "Nombre requerido")
            .bind(Contacto::getNombre, Contacto::setNombre);
        binder.forField(email)
            .bind(Contacto::getEmail, Contacto::setEmail);
        binder.forField(telefono)
            .bind(Contacto::getTelefono, Contacto::setTelefono);

        binder.readBean(contacto);
    }

    private void guardar() {
        try {
            binder.writeBean(contacto);
            close();
        } catch (ValidationException e) {
            // Binder muestra errores automáticamente
        }
    }

    public Contacto getContacto() {
        return contacto;
    }
}
```

Concepts:
- `Dialog` — ventana modal. `open()` la muestra, `close()` la cierra
- `Binder<Contacto>` — bindea campos del form ↔ propiedades del bean
- `binder.forField(campo).withValidator(...).bind(...)` — configura validación y binding
- `binder.readBean(bean)` — copia valores del bean al form (para editar)
- `binder.writeBean(bean)` — copia valores del form al bean (con validación)
- `ValidationException` — capturada, Binder muestra errores automáticamente

### Agregar botón Editar en ContactoListView

```java
// En grid, columna de acciones:
grid.addComponentColumn(contacto -> {
    Button editBtn = new Button("Editar", e -> editarContacto(contacto));
    Button deleteBtn = new Button("Eliminar", e -> eliminarContacto(contacto));
    return new HorizontalLayout(editBtn, deleteBtn);
}).setHeader("Acciones").setAutoWidth(false);

// Método editar:
private void editarContacto(Contacto contacto) {
    ContactoDialog dialog = new ContactoDialog(contacto);
    dialog.addOpenedChangeListener(e -> {
        if (!e.isOpened()) {
            service.save(dialog.getContacto());
            grid.getDataProvider().refreshAll();
        }
    });
    dialog.open();
}
```

---

## Estructura final

```
src/main/java/com/example/
├── Application.java
├── base/ui/
│   └── MainLayout.java
└── examplefeature/
    ├── Contacto.java
    ├── ContactoRepository.java
    ├── ContactoService.java
    └── ui/
        ├── ContactoDialog.java
        └── ContactoListView.java

src/main/resources/
├── application.properties
└── META-INF/resources/
    └── styles.css

pom.xml
```

---

## Comandos

```bash
mvn spring-boot:run        # Dev
mvn clean package -DskipTests  # Build producción
java -jar target/app-1.0-SNAPSHOT.jar
```

---

## Concepts por fase

| Fase | Concepto |
|------|----------|
| 1 | Entidad JPA, `@Entity`, `@Id`, `@GeneratedValue`, `@Column` |
| 2 | `JpaRepository`, naming conventions para queries |
| 3 | `@Service`, `@Transactional`, `Slice<T>` |
| 4 | `@Route`, `@Menu`, `@Layout`, `Grid`, `TextField`, `VerticalLayout` |
| 5 | Validación con `setInvalid`, `Notification`, `DataProvider.refreshAll()` |
| 6 | Lazy loading con `setItems(query -> ...)`, `VaadinSpringDataHelpers` |
| 7 | `addComponentColumn`, columna de acciones, delete |
| 8 | `Dialog`, `Binder`, `readBean`/`writeBean`, edit completo |

---

## Próximos pasos

1. Validación con annotations (`@NotBlank`, `@Email` en entidad)
2. Paginación numérica (añadir `Paging` o `Pagination` component)
3. Ordenar por columna (`Grid.Column` con `setSortable`)
4. Navegación a vista "Acerca de"
5. Estilos CSS custom

---

## Recursos

- [Vaadin Docs](https://vaadin.com/docs/)
- [Book of Vaadin](https://vaadin.com/docs/book/)
- [Vaadin GitHub](https://github.com/vaadin)
