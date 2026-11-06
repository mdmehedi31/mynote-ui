package com.mynote.ui.views;


import com.mynote.service.PostService;
import com.mynote.ui.layouts.MainLayout;
import com.mynotelibrary.dto.NoteResponseDTO;
import com.mynotelibrary.dto.UserPostRequestDTO;
import com.mynotelibrary.dto.UserPostResponse;
import com.mynotelibrary.enums.SUCCESS;
import com.mynotelibrary.exceptionmessage.ApiResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
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
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Page;

import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;


@Route(value = "posts", layout = MainLayout.class)
@PageTitle("Posts | My Note")
public class PostView extends VerticalLayout {

    private final TextArea postTextArea = new TextArea();
    private final Grid<UserPostResponse> postGrid = new Grid<>();
    private final PostService postService;
    private String userId="6aed7124a6907782d54c53fc";

    public PostView(PostService postService) {
        this.postService = postService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");
        getStyle().set("margin", "0 auto");

        add(createTopFormSection());
        add(createFeedSection());

        refreshGrid();
    }

    private VerticalLayout createTopFormSection() {
        postTextArea.setWidthFull();
        postTextArea.setPlaceholder("What's on your mind?");
        postTextArea.setMinHeight("110px");
        postTextArea.setMaxHeight("220px");

        Button saveButton = new Button("Post", e -> handleSavePost());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button clearButton = new Button("Clear", e -> handleClearForm());

        HorizontalLayout buttonLayout = new HorizontalLayout(clearButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        VerticalLayout cardContainer = new VerticalLayout(postTextArea, buttonLayout);
        cardContainer.setWidthFull();
        cardContainer.setPadding(true);
        cardContainer.setSpacing(true);

        cardContainer.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.12)")
                .set("border", "1px solid var(--lumo-contrast-10pct)");

        return cardContainer;
    }

    private void setupDataProvider() {
        DataProvider<UserPostResponse, Void> dataProvider = DataProvider.<UserPostResponse>fromCallbacks(
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    Page<UserPostResponse> notePage =
                            postService.getUserAllNotes(page, size);

                    if (notePage != null && notePage.getContent() != null) {
                        return notePage.getContent().stream();
                    }

                    return Stream.empty();
                },
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    Page<UserPostResponse> notePage =
                            postService.getUserAllNotes( page, size);

                    return notePage != null ? (int) notePage.getTotalElements() : 0;
                }
        );

        postGrid.setDataProvider(dataProvider);
    }

    private Div createFeedSection() {
        postGrid.setSizeFull();
        postGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);

        postGrid.addColumn(new ComponentRenderer<>(this::createPostCard))
                .setAutoWidth(true);

        Div wrapper = new Div(postGrid);
        wrapper.setSizeFull();
        return wrapper;
    }

    private VerticalLayout createPostCard(UserPostResponse post) {

        Span authorName = new Span(post.getCreatedByName() != null ? post.getCreatedByName() : "Anonymous");
        authorName.getStyle().set("font-weight", "600").set("font-size", "15px");

        String formattedDate = post.getCreatedAt() != null ?
                post.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")) : "";
        Span postTime = new Span(formattedDate);
        postTime.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "12px");

        Button btnDelete = new Button();

        btnDelete.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_PRIMARY);
        VaadinIcon icon = VaadinIcon.TRASH;
        Icon ic = new Icon(icon);
        ic.getStyle().setColor("red");
        btnDelete.setIcon(ic);

        HorizontalLayout nameAndButtonLayout = new HorizontalLayout(authorName,btnDelete);
        nameAndButtonLayout.setWidthFull();
        nameAndButtonLayout.getStyle().setJustifyContent(Style.JustifyContent.SPACE_BETWEEN);
        VerticalLayout headerText = new VerticalLayout(nameAndButtonLayout, postTime);
        headerText.setPadding(false);
        headerText.setSpacing(false);

        Paragraph contentParagraph = new Paragraph(post.getContent());
        contentParagraph.getStyle()
                .set("margin-top", "12px")
                .set("margin-bottom", "0")
                .set("white-space", "pre-wrap")
                .set("font-size", "14px")
                .set("line-height", "1.5");


        VerticalLayout card = new VerticalLayout(headerText, contentParagraph);
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(false);

        card.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("margin-bottom", "12px");

        btnDelete.addClickListener(e->{
            ApiResponse response = postService.deletePost(post.getId());
            if (response !=null && response.getMessage().equalsIgnoreCase(SUCCESS.SUCCESS.name())){

                Notification.show("Post Delete Successfully!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                card.removeFromParent();
            }
        });

        return card;
    }

    private void handleSavePost() {
        String content = postTextArea.getValue();
        if (content == null || content.trim().isEmpty()) {
            Notification.show("Post content cannot be empty.");
            return;
        }

        UserPostRequestDTO post = new UserPostRequestDTO();
        post.setContent(content);
        post.setCreatedById(userId);

        UserPostResponse saveResponse = postService.createPost(post);
        if (saveResponse != null) {
            Notification.show("Post published!");
            handleClearForm();
            refreshGrid();
        }
    }

    private void handleClearForm() {
        postTextArea.clear();
    }

    private void refreshGrid() {
        setupDataProvider();
        postGrid.getDataProvider().refreshAll();
    }
}
