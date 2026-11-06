package com.mynote.ui.views;

import com.mynote.service.PostService;
import com.mynote.service.UserService;
import com.mynote.ui.layouts.MainLayout;
import com.mynotelibrary.dto.UserPostResponse;
import com.mynotelibrary.dto.UserResponseDTO;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import java.util.stream.Stream;


@Route(value = "user-post", layout = MainLayout.class)
@PageTitle("Profile | User post view")
public class UserProfilePostsView extends VerticalLayout implements HasUrlParameter<String> {

    private final H3 nameHeading;
    private final Paragraph emailPara;
    private final Paragraph interestsPara;
    private final Grid<UserPostResponse> postGrid;
    private final PostService postService;
    private final UserService userService;
    private HorizontalLayout interestsLayout= new HorizontalLayout();
    private static final Logger log = LoggerFactory.getLogger(UserProfilePostsView.class);
    public UserProfilePostsView(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
        interestsLayout= new HorizontalLayout();

        nameHeading = new H3();
        emailPara = new Paragraph();
        interestsPara = new Paragraph();

        postGrid = new Grid<>(UserPostResponse.class, false);
        postGrid.addColumn(new ComponentRenderer<>(this::createPostCard))
                .setAutoWidth(true);
        postGrid.setSizeFull();

        setSizeFull();
        setPadding(true);

        add(nameHeading, emailPara, interestsLayout, postGrid);
        setFlexGrow(1, postGrid);
    }

    private VerticalLayout createPostCard(UserPostResponse post) {

        Paragraph contentParagraph = new Paragraph(post.getContent());
        contentParagraph.getStyle()
                .set("margin-top", "12px")
                .set("margin-bottom", "0")
                .set("white-space", "pre-wrap")
                .set("font-size", "14px")
                .set("line-height", "1.5");


        VerticalLayout card = new VerticalLayout(contentParagraph);
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(false);

        card.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("margin-bottom", "12px");
        return card;
    }


    private void loadData(String userId){
        UserResponseDTO userResponseDTO = userService.getUserById(userId);
        if(userResponseDTO != null){
            nameHeading.setText("Name: "+userResponseDTO.getName()!=null?userResponseDTO.getName():"");
            emailPara.setText("Email: "+userResponseDTO.getEmail()!=null?userResponseDTO.getEmail():"");

            for(String interest : userResponseDTO.getInterests()){
                for (String ints : userResponseDTO.getInterests()) {
                    Span spInt= new Span(interest);
                    spInt.setText(ints);
                    spInt.getStyle().set("font-size", "medium");
                    spInt.getStyle().setColor("green");
                    interestsLayout.add(spInt);
                }
            }
        }
        refreshGrid(userId);
    }

    private void refreshGrid(String userId) {
        setupDataProvider(userId);
        postGrid.getDataProvider().refreshAll();
    }

    private void setupDataProvider(String userId) {
        DataProvider<UserPostResponse, Void> dataProvider = DataProvider.<UserPostResponse>fromCallbacks(
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    Page<UserPostResponse> notePage =
                            postService.getUserAllPostByUserId(page,size,userId);

                    if (notePage != null && notePage.getContent() != null) {

                        return notePage.getContent().stream();
                    }

                    return Stream.empty();
                },
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();


                    Page<UserPostResponse> notePage =
                            postService.getUserAllPostByUserId(page,size,userId);

                    return notePage != null ? (int) notePage.getTotalElements() : 0;
                }
        );

        postGrid.setDataProvider(dataProvider);
    }

    @Override
    public void setParameter(BeforeEvent event, String userId) {
        if (userId != null && !userId.isEmpty()) {
            log.info("User id is " + userId);
            loadData(userId);
        }
    }

}