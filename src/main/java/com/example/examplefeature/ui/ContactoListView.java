package com.example.examplefeature.ui;


import com.example.base.ui.MainLayout;
import com.example.base.ui.ViewTitle;
import com.example.examplefeature.Contacto;
import com.example.examplefeature.ContactoService;
import com.vaadin.flow.component.Text;
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


@Route(value = "", layout = MainLayout.class)
@PageTitle("Contactos")
@Menu(order = 0, title = "Contactos")
public class ContactoListView extends VerticalLayout {

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
        nombre.clear();
        email.clear();
        telefono.clear();

        Notification.show("Contacto guardado", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.SUCCESS);

    }
}
