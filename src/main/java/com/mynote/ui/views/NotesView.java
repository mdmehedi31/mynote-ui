package com.mynote.ui.views;

import com.mynote.config.SecurityService;
import com.mynote.service.NoteService;
import com.mynote.ui.layouts.MainLayout;
import com.mynotelibrary.dto.NoteRequestDTO;
import com.mynotelibrary.dto.NoteResponseDTO;
import com.mynotelibrary.enums.SUCCESS;
import com.mynotelibrary.exceptionmessage.ApiResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.util.stream.Stream;

@Route(value = "notes", layout = MainLayout.class)
@PageTitle("Notes | My Note")
@PermitAll
public class NotesView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(NotesView.class);
    private final NoteService noteService;

    private final TextField title = new TextField();
    private final TextArea content = new TextArea();
    private final Button save = new Button("Save", VaadinIcon.DOWNLOAD_ALT.create());
    private final Button clear = new Button("Clear", VaadinIcon.REFRESH.create());
    private final Button btnDelete = new Button("Delete", VaadinIcon.TRASH.create());
    private final Binder<NoteResponseDTO> binder = new Binder<>(NoteResponseDTO.class);
    private ListDataProvider<NoteResponseDTO> dataProvider;
    private String userId;
    private SecurityService securityService;

    Grid<NoteResponseDTO> targetGrid = new Grid<>(NoteResponseDTO.class, false);
    private TextField searchField;
    public NotesView(NoteService noteService, SecurityService securityService) {
        this.noteService = noteService;
        this.securityService = securityService;
        userId=securityService.getCurrentUserId();

        setSizeFull();
        setMaxWidth("720px");
        getStyle().set("margin", "0 auto");

        searchField = new TextField();
        searchField.setPlaceholder("Search notes...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshGrid(e.getValue()));

        add(buildForm(), buildButtons(), searchField,buildGrid());
        bindForm();
        refreshGrid(null);
    }

    private VerticalLayout buildForm() {
        title.setPlaceholder("Title");
        title.setWidthFull();

        content.setPlaceholder("Write your note here...");
        content.setWidthFull();
        content.setMinHeight("120px");

        VerticalLayout form = new VerticalLayout(title, content);
        form.setPadding(false);
        return form;
    }

    private HorizontalLayout buildButtons() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> saveNote());
        clear.addClickListener(e -> clearForm());
        btnDelete.addClickListener(e->{

            NoteResponseDTO note = binder.getBean();
            if(note!=null && note.getId()!=null) {
                ApiResponse response = noteService.deleteById(note.getId());
                if (response.getMessage().equalsIgnoreCase(SUCCESS.SUCCESS.name())){
                    Notification.show("Deleted successfully");
                    refreshGrid(null);
                }
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(save, btnDelete,clear);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return buttons;
    }

    private Grid<NoteResponseDTO> buildGrid() {

        targetGrid.addColumn(new com.vaadin.flow.data.renderer.ComponentRenderer<>(note -> {
                    var icon = VaadinIcon.FILE_TEXT_O.create();
                    icon.setColor("var(--lumo-primary-color)");
                    var name = new Span(note.getTitle());

                    Button btnViewNote = new Button();
                    Icon iconViewNote = VaadinIcon.EYE.create();
                    iconViewNote.getStyle().set("color", "blue");
                    btnViewNote.setIcon(iconViewNote);

                    var row = new HorizontalLayout(icon, name, btnViewNote);
                    btnViewNote.addClickListener(e->{
                        viewNote(note);
                    });
                    row.setAlignItems(FlexComponent.Alignment.CENTER);
                    return row;
                }))
                .setComparator(NoteResponseDTO::getTitle)
                .setSortable(true)
                .setFlexGrow(1);

        targetGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        targetGrid.setAllRowsVisible(false);
        targetGrid.setHeight("400px");

        targetGrid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                binder.setBean(e.getValue());
            }
        });

        return targetGrid;
    }

    private void viewNote(NoteResponseDTO note) {

        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3(note.getTitle());
        Paragraph content = new Paragraph(note.getContent());
        Button btnClose = new Button("Close");
        layout.add(title, content, btnClose);

        Dialog dialog = new Dialog();
        dialog.setWidth("60vh");
        dialog.setHeight("auto");
        dialog.add(layout);
        dialog.setResizable(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.open();

        btnClose.addClickListener(e -> {
            dialog.close();
        });
    }


    private void setupDataProvider(String searchKeyword) {
        DataProvider<NoteResponseDTO, Void> dataProvider = DataProvider.<NoteResponseDTO>fromCallbacks(
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    Page<NoteResponseDTO> notePage =
                            noteService.getUserAllNotes(searchKeyword, page, size,userId);

                    if (notePage != null && notePage.getContent() != null) {

                        log.info("Page size is : "+notePage.getTotalElements());
                        return notePage.getContent().stream();
                    }

                    log.info("list is empty");
                    return Stream.empty();
                },
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    org.springframework.data.domain.Page<NoteResponseDTO> notePage =
                            noteService.getUserAllNotes(searchKeyword, page, size,userId);

                    return notePage != null ? (int) notePage.getTotalElements() : 0;
                }
        );

        targetGrid.setDataProvider(dataProvider);
    }

    private void refreshGrid(String filterText) {
        log.info("Refresss with text: "+filterText);
        setupDataProvider(filterText);
        targetGrid.getDataProvider().refreshAll();
    }

    private void bindForm() {
       binder.forField(title)
                .bind(NoteResponseDTO::getTitle, NoteResponseDTO::setTitle);
        binder.forField(content).asRequired("Content is required")
                .bind(NoteResponseDTO::getContent, NoteResponseDTO::setContent);
        binder.setBean(new NoteResponseDTO());
    }

    private void saveNote() {
        NoteResponseDTO note = binder.getBean();
        if (binder.validate().isOk()) {
            NoteRequestDTO requestDTO = new NoteRequestDTO();
            requestDTO.setTitle(note.getTitle() != null ? note.getTitle() : null);
            requestDTO.setContent(note.getContent());
            requestDTO.setId(note.getId() != null ? note.getId() : null);
            requestDTO.setUserId(userId);

            NoteResponseDTO saveResponse = noteService.createNote(requestDTO);
            if (saveResponse != null) {

                Notification.show("Note saved", 2000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                clearForm();
                refreshGrid(null);
            }
        }
    }

    private void clearForm() {
        targetGrid.deselectAll();
        title.clear();
        content.clear();
    }

}
