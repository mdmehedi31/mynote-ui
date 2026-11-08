package com.mynote.ui.views;

import com.mynote.service.UserAuthService;
import com.mynotelibrary.dto.UserAuthInfo;
import com.mynotelibrary.dto.UserRequestDTO;
import com.mynotelibrary.enums.SUCCESS;
import com.vaadin.flow.component.Key;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Route("/login")
@PageTitle("My Note")
@PermitAll
public class HomeView extends VerticalLayout implements BeforeEnterObserver {


    private UserAuthService userAuthService;

    public HomeView(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
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
            String userEmail = username.getValue();
            String passwordValue = password.getValue();

            if (userEmail.isBlank() || passwordValue.isBlank()) {
                Notification.show("Invalid username or password", 2000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            UserAuthInfo userAuthInfo = userAuthService.userLogin(userEmail, passwordValue);
            if (userAuthInfo != null) {
                if (userAuthInfo.getMessage().equals(SUCCESS.SUCCESS.name())) {
                    redirectToPostView(userAuthInfo);
                }else{

                    Notification.show(userAuthInfo.getMessage()!=null && !userAuthInfo.getMessage().isBlank() ?
                                    userAuthInfo.getMessage() :"Login Failed Please try again !", 2000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show(userAuthInfo.getMessage()!=null && !userAuthInfo.getMessage().isBlank() ?
                                userAuthInfo.getMessage() : "Login Failed Please try again !", 2000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout box = card(new H3("Log in"), username, password, login);
        login.addClickShortcut(Key.ENTER).listenOn(box);
        return box;
    }

    private void redirectToPostView(UserAuthInfo userAuthInfo){
        if (userAuthInfo.getMessage() != null && userAuthInfo.getMessage().equalsIgnoreCase(SUCCESS.SUCCESS.name())) {
            if (userAuthInfo.getJwtToken() != null && !userAuthInfo.getJwtToken().isBlank()) {
                VaadinSession.getCurrent().setAttribute("jwt_token", userAuthInfo.getJwtToken());
                VaadinSession.getCurrent().setAttribute("user_id", userAuthInfo.getUser().getId());

                var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userAuthInfo.getUser().getRole()));
                var principal = new User(userAuthInfo.getUser().getEmail(), "", authorities);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                var securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);

                VaadinSession.getCurrent().getSession().setAttribute(
                        "SPRING_SECURITY_CONTEXT", securityContext
                );
            }

            Notification.show("Login Successful!", 2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            UI.getCurrent().navigate(PostView.class);
        }else {
            Notification.show("Login Failed Please try again !", 2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private VerticalLayout registerForm() {
        TextField username = new TextField("Username");

        EmailField email = new EmailField("Email");

        email.setRequired(true);
        PasswordField password = new PasswordField("Password");
        password.setRequired(true);

        Button register = new Button("Create account", e -> {
            if(email.getValue() == null || password.getValue() == null || password.getValue().isBlank()) {

                Notification.show("Required information not inputed", 2000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (email.isInvalid()){

                Notification.show("Email is invalid, please try with valid email", 2000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            UserRequestDTO user = new UserRequestDTO();
            user.setEmail(email.getValue());
            user.setPassword(password.getValue());
            user.setName(username.getValue());
            UserAuthInfo create= userAuthService.userCreate(user);

            if(create != null && create.getMessage().equals(SUCCESS.SUCCESS.name())) {
                redirectToPostView(create);
            }else {
                Notification.show(create.getMessage()!=null && !create.getMessage().isBlank() ? create.getMessage() : "User creation Failed Please try again !", 2000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
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
