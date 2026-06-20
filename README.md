# Contact Manager — Vaadin Learning Project

App CRUD de contactos para aprender Vaadin Framework 25.x paso a paso.

## Tech Stack

- **Vaadin** 25.1.8 (Flow)
- **Spring Boot** 4.0.7
- **Spring Data JPA**
- **H2** (in-memory DB)
- **Java** 25
- **Lombok**

## Estructura

```
src/main/java/com/example/
├── Application.java              # Entry point
├── base/ui/
│   └── MainLayout.java          # Layout con SideNav
└── examplefeature/
    ├── Contacto.java            # Entidad JPA
    ├── ContactoRepository.java # Spring Data repository
    ├── ContactoService.java     # Lógica de negocio
    └── ui/
        └── ContactoListView.java # Vista principal
```

## Ejecutar

```bash
mvn spring-boot:run
```

Abrir http://localhost:8080

## Build

```bash
mvn clean package -DskipTests
java -jar target/app-1.0-SNAPSHOT.jar
```

## Roadmap

- [x] Fase 1-4: Entidad, Repository, Service, Vista básica con Grid
- [ ] Fase 5: Formulario de creación
- [ ] Fase 6: Lazy loading con filtro
- [ ] Fase 7: Eliminar contacto
- [ ] Fase 8: Editar contacto con Dialog

Ver [vaadin-learning-project.md](./vaadin-learning-project.md) para guía paso a paso.

## Recursos

- [Vaadin Docs](https://vaadin.com/docs/)
- [Book of Vaadin](https://vaadin.com/docs/book/)
