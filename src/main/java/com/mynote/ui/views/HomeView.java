package com.mynote.ui.views;

import com.vaadin.flow.component.Key;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("My Note")
public class HomeView extends VerticalLayout implements BeforeEnterObserver {


    public HomeView() {
         setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        FlexLayout forms = new FlexLayout(loginForm(), registerForm());
        forms.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        forms.setJustifyContentMode(JustifyContentMode.CENTER);

        add(new H1("My Note"), new Paragraph("Simple notes and posts."), forms);
    }

    private VerticalLayout loginForm() {
        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        Button login = new Button("Log in", e -> {

        });
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout box = card(new H3("Log in"), username, password, login);
        // Enter submits login only while focus is inside this card.
        login.addClickShortcut(Key.ENTER).listenOn(box);
        return box;
    }

    private VerticalLayout registerForm() {
        TextField username = new TextField("Username");
        EmailField email = new EmailField("Email");
        PasswordField password = new PasswordField("Password");
        password.setHelperText("At least 8 characters");

        Button register = new Button("Create account", e -> {

        });

        return card(new H3("Register"), username, email, password, register);
    }

    private VerticalLayout card(com.vaadin.flow.component.Component... children) {
        VerticalLayout box = new VerticalLayout(children);
        box.setWidth("300px");
        box.setSpacing(true);
        return box;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        /*if (auth.currentUser().isPresent()) {
            event.forwardTo(NotesView.class);
        }*/
    }
}
