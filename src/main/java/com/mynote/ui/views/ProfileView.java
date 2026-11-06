package com.mynote.ui.views;

import com.mynote.enums.InterestEnum;
import com.mynote.service.UserService;
import com.mynote.ui.layouts.MainLayout;
import com.mynotelibrary.dto.UserRequestDTO;
import com.mynotelibrary.dto.UserResponseDTO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profile | My Note")
public class ProfileView extends VerticalLayout {


    private final TextField nameField = new TextField("Full Name");
    private final EmailField emailField = new EmailField("Email Address");
    private final PasswordField passwordField = new PasswordField("New Password (Leave blank to keep current)");

    private final MultiSelectComboBox<InterestEnum> interestsComboBox = new MultiSelectComboBox<>("Interests");

    private final Binder<UserResponseDTO> binder = new Binder<>(UserResponseDTO.class);

    private final UserService userService;
    private String userId;

    public ProfileView(UserService userService) {
        this.userService = userService;
        this.userId="6aed7124a6907782d54c53fc";
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        setMaxWidth("600px");
        getStyle().set("margin", "0 auto");

        add(new H2("User Profile"));
        add(createProfileFormSection());

        configureInterestsComboBox();
        configureBinder();

        loadUserProfile();
    }

    private VerticalLayout createProfileFormSection() {
        nameField.setWidthFull();
        emailField.setWidthFull();
        passwordField.setWidthFull();
        interestsComboBox.setWidthFull();

        interestsComboBox.setPlaceholder("Select your interests...");

        Button saveButton = new Button("Save Changes", e -> handleSaveProfile());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        nameField.setReadOnly(true);
        emailField.setReadOnly(true);

        VerticalLayout container = new VerticalLayout(
                nameField,
                emailField,
               /* passwordField,*/
                interestsComboBox,
                buttonLayout
        );

        container.setWidthFull();
        container.setPadding(true);
        container.setSpacing(true);
        container.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.12)")
                .set("border", "1px solid var(--lumo-contrast-10pct)");

        return container;
    }

    private void configureInterestsComboBox() {
        interestsComboBox.setItems(Arrays.asList(InterestEnum.values()));

        interestsComboBox.setItemLabelGenerator(InterestEnum::getDisplayName);
    }

    private void configureBinder() {
        binder.forField(nameField)
                .asRequired("Full Name is required")
                .bind(UserResponseDTO::getName, UserResponseDTO::setName);

        binder.forField(emailField)
                .asRequired("Email is required")
                .withValidator(new EmailValidator("Please enter a valid email address"))
                .bind(UserResponseDTO::getEmail, UserResponseDTO::setEmail);

        binder.forField(interestsComboBox)
                .bind(
                        dto -> dto.getInterests() != null ?
                                dto.getInterests().stream()
                                        .map(InterestEnum::valueOf)
                                        .collect(Collectors.toSet()) : Set.of(),
                        (dto, selectedEnums) -> dto.setInterests(
                                selectedEnums.stream()
                                        .map(Enum::name)
                                        .collect(Collectors.toList())
                        )
                );
    }

    private void loadUserProfile() {

        UserResponseDTO profileDto = userService.getUserById(userId);
        binder.setBean(profileDto);
    }

    private void handleSaveProfile() {
        UserRequestDTO profileDto = new UserRequestDTO();
        if (binder.isValid()) {
            UserResponseDTO response = binder.getBean();
            profileDto.setId(response.getId());
            profileDto.setInterests(response.getInterests());
            profileDto.setRole(response.getRole());

            UserResponseDTO userResponseDTO= userService.updateUser(profileDto);

            if (userResponseDTO != null) {
                Notification.show("Profile updated successfully!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            }
        } else {
            Notification.show("Please fix the validation errors.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
