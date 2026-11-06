package com.mynote.ui.views;


import com.mynote.enums.InterestEnum;
import com.mynote.service.UserService;
import com.mynote.ui.layouts.MainLayout;
import com.mynotelibrary.dto.NoteResponseDTO;
import com.mynotelibrary.dto.UserResponseDTO;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.stream.Stream;

@Route(value = "user-interest-view", layout = MainLayout.class)
@PageTitle("Profile | User interest view")
public class UserInterestMatchView extends VerticalLayout {

    private final MultiSelectComboBox<InterestEnum> interestSelect;
    private final Button searchButton;
    private final Grid<UserResponseDTO> grid;
    private UserService userService;
    public UserInterestMatchView(UserService userService) {
        this.userService = userService;
        interestSelect = new MultiSelectComboBox<>();
        interestSelect.setPlaceholder("Select interests...");
        interestSelect.setItems(Arrays.asList(InterestEnum.values()));
        interestSelect.setItemLabelGenerator(InterestEnum::getDisplayName);

        searchButton = new Button("Search");

        HorizontalLayout headerLayout = new HorizontalLayout(interestSelect, searchButton);
        headerLayout.setAlignItems(Alignment.BASELINE);

        grid = new Grid<>(UserResponseDTO.class, false);
        grid.addColumn(UserResponseDTO::getName).setHeader("Name");
       // grid.addColumn(UserResponseDTO::getEmail).setHeader("Email");

        grid.addColumn(new ComponentRenderer<>(user -> {
            Anchor nameLink = new Anchor();
            nameLink.setText(user.getEmail());
            nameLink.setHref("user-post/" + user.getId());
            nameLink.getElement().addEventListener("click", event -> {
                UI.getCurrent().navigate(UserProfilePostsView.class, user.getId());
            });

            return nameLink;
        })).setHeader("Email");

        grid.addComponentColumn(e->{
            HorizontalLayout interestLayout = new HorizontalLayout();
            if(e.getInterests() !=null && e.getInterests().size()>0){
                for (String ints : e.getInterests()) {
                    Span spInt= new Span();
                    spInt.setText(ints);
                    spInt.getStyle().set("font-size", "medium");
                    spInt.getStyle().setColor("green");
                    interestLayout.add(spInt);
                }
            }
            return interestLayout;
        }).setHeader("Interest");
        grid.setSizeFull();

        searchButton.addClickListener(event -> {
            refreshGrid();
        });

        setSizeFull();
        add(headerLayout, grid);
        setFlexGrow(1, grid);
        refreshGrid();
    }
    private void refreshGrid() {
        setupDataProvider();
        grid.getDataProvider().refreshAll();
    }

    private void setupDataProvider() {
        DataProvider<UserResponseDTO, Void> dataProvider = DataProvider.<UserResponseDTO>fromCallbacks(
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    Page<UserResponseDTO> notePage =
                            userService.getAllMatchInterestUser(page,size,interestSelect.getValue());

                    if (notePage != null && notePage.getContent() != null) {

                        return notePage.getContent().stream();
                    }

                    return Stream.empty();
                },
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int size = query.getLimit();

                    Page<UserResponseDTO> notePage =
                            userService.getAllMatchInterestUser(page, size,interestSelect.getValue());

                    return notePage != null ? (int) notePage.getTotalElements() : 0;
                }
        );

        grid.setDataProvider(dataProvider);
    }
}
