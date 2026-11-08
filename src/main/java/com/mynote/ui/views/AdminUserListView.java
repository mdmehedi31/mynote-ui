package com.mynote.ui.views;

import com.mynote.config.SecurityService;
import com.mynote.service.NoteService;
import com.mynote.service.UserService;
import com.mynote.ui.layouts.MainLayout;
import com.mynotelibrary.dto.UserRequestDTO;
import com.mynotelibrary.dto.UserResponseDTO;
import com.mynotelibrary.enums.SUCCESS;
import com.mynotelibrary.enums.UserRole;
import com.mynotelibrary.exceptionmessage.ApiResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Stream;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Users | My Note")
@PermitAll
public class AdminUserListView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(AdminUserListView.class);
    private final Grid<UserResponseDTO> userGrid = new Grid<>(UserResponseDTO.class, false);
    private final UserService userService;
    private final NoteService noteService;
    private String userId;
    private final SecurityService securityService;

    public AdminUserListView(UserService userService, NoteService noteService, SecurityService securityService) {
        this.userService = userService;
        this.noteService = noteService;
        this.securityService = securityService;
        this.userId = securityService.getCurrentUserId();
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        H3 title = new H3("User Directory");
        title.getStyle().set("margin-top", "0");
        add(title);
        configureGrid();
        add(userGrid);
        refreshGrid();
    }

    private void configureGrid() {
        userGrid.setSizeFull();

        userGrid.addColumn(UserResponseDTO::getName)
                .setHeader("User Name")
                .setFlexGrow(1).setSortProperty("name").setSortable(true);

        userGrid.addColumn(UserResponseDTO::getEmail)
                .setHeader("Email Address")
                .setFlexGrow(1).setSortProperty("email").setSortable(true);

        userGrid.addColumn(new ComponentRenderer<>(user -> {
            Button viewNoteButton = new Button("View Note", e -> openNoteDialog(user.getId()));
            viewNoteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

            Button editButton = new Button("Edit User", e -> openEditDialog(user));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

            Button btnDelete= new Button("Removed",e->{
                removeUser(user);
            });
            btnDelete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
            HorizontalLayout actions = new HorizontalLayout(viewNoteButton, editButton,btnDelete);
            actions.setSpacing(true);
            return actions;
        })).setHeader("Actions").setAutoWidth(true);
    }

    private void removeUser(UserResponseDTO userResponseDTO) {

        ApiResponse response= userService.deleteUserById(userResponseDTO.getId());
        if (response != null){
            if (response.getMessage().equals(SUCCESS.SUCCESS.name())){

                Notification.show("User deleted successfully!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                userGrid.getDataProvider().fetch(new Query<>())
                        .filter(item -> item.equals(userResponseDTO))
                        .findFirst();

                ((ListDataProvider<UserResponseDTO>) userGrid.getDataProvider()).getItems().remove(userResponseDTO);
                userGrid.getDataProvider().refreshAll();
            }else{

                Notification.show(response.getMessage()!=null && !response.getMessage().isBlank() ? response.getMessage() : "User deleted failed!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            userGrid.getDataProvider().refreshAll();
        }else{

            Notification.show("User deleted failed!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private void refreshGrid() {
        setupDataProvider();
        userGrid.getDataProvider().refreshAll();
    }

    private void setupDataProvider() {
        DataProvider<UserResponseDTO, Void> dataProvider = DataProvider.<UserResponseDTO>fromCallbacks(
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    String sortBy = null;
                    String order = null;

                    if (!query.getSortOrders().isEmpty()) {
                        QuerySortOrder sortOrder = query.getSortOrders().get(0);
                        sortBy = sortOrder.getSorted();
                        order = sortOrder.getDirection().name().toLowerCase();
                    }

                    Page<UserResponseDTO> userPage =
                            userService.getAllUsers(page, size, sortBy, order,userId);

                    if (userPage != null && userPage.getContent() != null) {
                        return userPage.getContent().stream();
                    }
                    return Stream.empty();
                },
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    String sortBy = null;
                    String order = null;

                    if (!query.getSortOrders().isEmpty()) {
                        QuerySortOrder sortOrder = query.getSortOrders().get(0);
                        sortBy = sortOrder.getSorted();
                        order = sortOrder.getDirection().name().toLowerCase();
                    }

                    Page<UserResponseDTO> userPage =
                            userService.getAllUsers(page, size, sortBy, order,userId);

                    return userPage != null ? (int) userPage.getTotalElements() : 0;
                }
        );

        userGrid.setDataProvider(dataProvider);
    }

    private void openNoteDialog(String userId) {

        NotesListDialog notesListDialog = new NotesListDialog(noteService,userId);
        notesListDialog.setSizeFull();
        notesListDialog.setResizable(false);
        notesListDialog.setCloseOnEsc(false);
        notesListDialog.open();

    }


    private void openEditDialog(UserResponseDTO user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit User Information");
        dialog.setWidth("450px");

        TextField nameField = new TextField("User Name");
        nameField.setWidthFull();

        TextField emailDisplayField = new TextField("Email (Read-only)");
        emailDisplayField.setValue(user.getEmail());
        emailDisplayField.setReadOnly(true);
        emailDisplayField.setWidthFull();

        Select<UserRole> userRoleSelect = new Select<>();
        userRoleSelect.setItemLabelGenerator(UserRole::name);
        userRoleSelect.setItems(UserRole.values());

        FormLayout formLayout = new FormLayout(nameField, emailDisplayField, userRoleSelect);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Binder<UserResponseDTO> binder = new Binder<>(UserResponseDTO.class);
        binder.forField(nameField)
                .bind(UserResponseDTO::getName, UserResponseDTO::setName);

        binder.forField(userRoleSelect).bind(UserResponseDTO::getRole, UserResponseDTO::setRole);

        binder.setBean(user);

        Button saveButton = new Button("Save", e -> {

            if (binder.isValid()) {

                UserResponseDTO sourceUserRe = binder.getBean();
                UserRequestDTO targetDto = new UserRequestDTO();
                targetDto.setId(sourceUserRe.getId());
                targetDto.setEmail(sourceUserRe.getEmail());
                targetDto.setRole(sourceUserRe.getRole());
                targetDto.setName(sourceUserRe.getName());

                log.info("Name : " + targetDto.getName()+", Role : " + targetDto.getRole());
                UserResponseDTO saveDone = userService.updateUser(targetDto);

                if (saveDone != null) {
                    Notification.show("User updated successfully!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    refreshGrid();
                }else{

                    Notification.show("User updated successfully!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }

                dialog.close();
            } else {
                Notification.show("Please fix validation errors.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        VerticalLayout dialogLayout = new VerticalLayout(formLayout, buttonLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
      /*  if (!auth.isAdmin()) {
            event.forwardTo(NotesView.class);
            return;
        }
        grid.setItems(users.findAll());*/
    }
}
