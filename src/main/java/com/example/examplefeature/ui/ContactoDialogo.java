package com.example.examplefeature.ui;

import com.example.examplefeature.Contacto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import lombok.Getter;

public class ContactoDialogo extends Dialog {

    private final Binder<Contacto> binder = new Binder<>(Contacto.class);

    @Getter
    private final Contacto contacto;

    @Getter
    private boolean guardado = false;

    public ContactoDialogo() {
        this(new Contacto("", null, null), null);
    }

    public ContactoDialogo(Contacto contacto, Long idOriginal) {
        this.contacto = new Contacto(contacto.getNombre(), contacto.getEmail(), contacto.getTelefono());
        if (idOriginal != null) {
            this.contacto.setId(idOriginal);
        }

        FormLayout form = new FormLayout();
        TextField nombre = new TextField("Nombre");
        TextField email = new TextField("Email");
        TextField telefono = new TextField("Teléfono");
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

        binder.readBean(this.contacto);
    }

    private void guardar() {
        try {
            binder.writeBean(contacto);
            guardado = true;
            close();
        } catch (ValidationException e) {
            guardado = false;
        }
    }

}
