package com.example.examplefeature.ui;


import com.example.base.ui.MainLayout;
import com.example.base.ui.ViewTitle;
import com.example.examplefeature.Contacto;
import com.example.examplefeature.ContactoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;


@Route(value = "", layout = MainLayout.class)
@PageTitle("Contactos")
@Menu(order = 0, title = "Contactos")
public class ContactoListView extends VerticalLayout {

    private final ContactoService service;
    private final Grid<Contacto> grid = new Grid<>();

    private final TextField nombre = new TextField("Nombre");
    private final TextField email = new TextField("Email");
    private final TextField telefono = new TextField("Teléfono");
    private final TextField filtro = new TextField("Buscar");

    ContactoListView(ContactoService service) {
        this.service = service;

        nombre.addValueChangeListener(e -> nombre.setInvalid(false));
        email.addValueChangeListener(e -> email.setInvalid(false));
        telefono.addValueChangeListener(e -> telefono.setInvalid(false));

        Button guardar = new Button("Guardar");
        guardar.addClickListener(e -> guardarContacto());

        filtro.setPlaceholder("Buscar...");
        filtro.setClearButtonVisible(true);
        filtro.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        HorizontalLayout toolbar = new HorizontalLayout(filtro);

        HorizontalLayout form = new HorizontalLayout(nombre, email, telefono, guardar);
        form.setAlignItems(FlexComponent.Alignment.END);
        form.setFlexGrow(0, guardar);

        grid.addColumn(Contacto::getNombre).setHeader("Nombre");
        grid.addColumn(Contacto::getEmail).setHeader("Email");
        grid.addColumn(Contacto::getTelefono).setHeader("Teléfono");

        grid.addComponentColumn(contacto -> {
            Button editBtn = new Button("Editar", e -> editarContacto(contacto));
            Button deleteBtn = new Button("Eliminar", e -> eliminarContacto(contacto));
            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Acciones").setAutoWidth(false);

        grid.setEmptyStateText("No hay contactos");
        grid.setSizeFull();
        grid.setItems(query -> service.list(
                toSpringPageRequest(query),
                filtro.getValue()
        ).stream());

        add(new ViewTitle("Contactos"), toolbar, form, grid);
        setSizeFull();
    }

    private void editarContacto(Contacto contacto) {
        Contacto copy = new Contacto(contacto.getNombre(), contacto.getEmail(), contacto.getTelefono());
        ContactoDialogo dialogo = new ContactoDialogo(copy, contacto.getId());
        dialogo.addOpenedChangeListener(e -> {
            if (!e.isOpened() && dialogo.isGuardado()) {
                Contacto actualizado = dialogo.getContacto();
                actualizado.setId(contacto.getId());
                service.save(actualizado);
                grid.getDataProvider().refreshAll();
                grid.asSingleSelect().clear();
            }
        });
        dialogo.open();
    }

    private void guardarContacto() {
        if (nombre.getValue().isBlank()) {
            nombre.setInvalid(true);
            nombre.setErrorMessage("El nombre es obligatorio");
            return;
        }

        Contacto contacto = new Contacto(
                nombre.getValue(),
                email.getValue(),
                telefono.getValue()
        );

        service.save(contacto);
        grid.getDataProvider().refreshAll();
        grid.asSingleSelect().clear();
        nombre.clear();
        email.clear();
        telefono.clear();

        Notification.show("Contacto guardado", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.SUCCESS);
    }

    private void eliminarContacto(Contacto contacto) {
        service.delete(contacto.getId());
        grid.getDataProvider().refreshAll();
        grid.asSingleSelect().clear();

        Notification.show("Contacto eliminado", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.SUCCESS);
    }

}
