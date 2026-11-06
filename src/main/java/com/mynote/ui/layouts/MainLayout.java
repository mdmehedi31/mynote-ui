package com.mynote.ui.layouts;

import com.mynote.ui.views.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

/** App shell for all authenticated views: header with logout, drawer with the side navigation. */
public class MainLayout extends AppLayout implements BeforeEnterObserver {


    public MainLayout() {


        Span title = new Span("My Note");
        title.getStyle().set("font-weight", "600");

        Button logout = new Button("Log out", VaadinIcon.SIGN_OUT.create(), e -> {

            UI.getCurrent().navigate(HomeView.class);
        });
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title, logout);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);
        header.getStyle().set("padding-inline-end", "var(--vaadin-padding-m, 1rem)");

        addToNavbar(header);
        addToDrawer(new Scroller(buildNav()));
    }

    private SideNav buildNav() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Profile", ProfileView.class, VaadinIcon.USER.create()));
        nav.addItem(new SideNavItem("Notes", NotesView.class, VaadinIcon.NOTEBOOK.create()));
        nav.addItem(new SideNavItem("Post View", PostView.class, VaadinIcon.NEWSPAPER.create()));
        nav.addItem(new SideNavItem("User Interest Match View", UserInterestMatchView.class, VaadinIcon.AIRPLANE.create()));
       if (/*auth.isAdmin()*/ true) {
            nav.addItem(new SideNavItem("Admin User List", AdminUserListView.class,
                    VaadinIcon.USERS.create()));
        }
        return nav;
    }

    /** Anything rendered inside this layout requires a logged-in user. */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
       /* if (auth.currentUser().isEmpty()) {
            event.forwardTo(HomeView.class);
        }*/
    }
}
