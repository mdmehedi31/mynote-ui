package com.mynote.ui.views;


import com.mynote.service.NoteService;
import com.mynotelibrary.dto.NoteResponseDTO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.util.stream.Stream;

public class NotesListDialog extends Dialog {

    private final Grid<NoteResponseDTO> grid = new Grid<>(NoteResponseDTO.class, false);
    private final Div leftDrawer = new Div();
    private NoteResponseDTO selectedNote = null;
    private NoteService noteService;
    private String userId;
    private static final Logger log = LoggerFactory.getLogger(NotesListDialog.class);
    private boolean isClearingSelection=false;
    public NotesListDialog(NoteService noteService,String userId) {
        setHeaderTitle("All Notes");
        setWidth("800px");
        setHeight("550px");
        setCloseOnOutsideClick(true);
        this.noteService = noteService;
        this.userId = userId;
        grid.addColumn(NoteResponseDTO::getTitle).setHeader("Title").setFlexGrow(1);
        grid.setHeight("400px");

        leftDataProviderStyle();

        leftDrawer.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("right", "-300px")
                .set("width", "280px")
                .set("height", "100%")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("transition", "right 0.3s ease-in-out")
                .set("z-index", "10")
                .set("padding", "var(--lumo-space-m)")
                .set("box-sizing", "border-box");

        Div contentWrapper = new Div(leftDrawer, grid);
        contentWrapper.getStyle().set("position", "relative").set("overflow", "hidden").set("width", "100%");

        grid.addItemClickListener(event -> {
            NoteResponseDTO clickedNote = event.getItem();

            if (clickedNote == null) {
                return;
            }

            if (selectedNote != null && selectedNote.equals(clickedNote)) {
                grid.getSelectionModel().deselect(clickedNote);
                closeDrawer();
                selectedNote = null;
            } else {
                grid.getSelectionModel().deselectAll();
                grid.getSelectionModel().select(clickedNote);
                selectedNote = clickedNote;
                openDrawer(clickedNote);
            }
        });
        Button closeButton = new Button("Close", e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(closeButton);

        add(contentWrapper);
        refreshGrid(null);
    }

    private void setupDataProvider(String searchKeyword) {
        DataProvider<NoteResponseDTO, Void> dataProvider = DataProvider.<NoteResponseDTO>fromCallbacks(
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    Page<NoteResponseDTO> notePage =
                            noteService.getUserAllNotes(searchKeyword, page, size,userId);

                    log.info("Is note page is null : {}", notePage == null);
                    if (notePage != null && notePage.getContent() != null) {

                        log.info("note dialog :: Page size is : "+notePage.getTotalElements());
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

        grid.setDataProvider(dataProvider);
    }

    private void refreshGrid(String filterText) {
        log.info("Refresss with text: "+filterText);
        setupDataProvider(filterText);
        grid.getDataProvider().refreshAll();
    }
    private void openDrawer(NoteResponseDTO note) {
        leftDrawer.removeAll();
        H3 title = new H3(note.getTitle());
        Paragraph content = new Paragraph(note.getContent() != null ? note.getContent() : "No content available.");
        Button closeDrawerBtn = new Button("Close Details", e -> {
            closeDrawer();
            grid.asSingleSelect().clear();
            selectedNote = null;
        });
        closeDrawerBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        VerticalLayout drawerLayout = new VerticalLayout(title, content, closeDrawerBtn);
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(true);
        leftDrawer.add(drawerLayout);

        leftDrawer.getStyle().set("right", "0px");
    }

    private void closeDrawer() {
        leftDrawer.getStyle().set("right", "-300px");
    }

    private void leftDataProviderStyle() {
    }
}