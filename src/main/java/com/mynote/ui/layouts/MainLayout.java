package com.mynote.ui.layouts;

import com.mynote.config.SecurityService;
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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;

@PermitAll
public class MainLayout extends AppLayout /*implements BeforeEnterObserver */{

    private final SecurityService securityService;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;


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
        addToDrawer(new Scroller(buildDrawerContent()));
    }

  /*  private SideNav buildNav() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Profile", ProfileView.class, VaadinIcon.USER.create()));
        nav.addItem(new SideNavItem("Notes", NotesView.class, VaadinIcon.NOTEBOOK.create()));
        nav.addItem(new SideNavItem("Post View", PostView.class, VaadinIcon.NEWSPAPER.create()));
        nav.addItem(new SideNavItem("User Interest Match View", UserInterestMatchView.class, VaadinIcon.AIRPLANE.create()));
       if (securityService.isAdmin()) {
            nav.addItem(new SideNavItem("Admin User List", AdminUserListView.class,
                    VaadinIcon.USERS.create()));
        }
        return nav;
    }*/

    private Scroller buildDrawerContent() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Profile", ProfileView.class, VaadinIcon.USER.create()));
        nav.addItem(new SideNavItem("Notes", NotesView.class, VaadinIcon.NOTEBOOK.create()));
        nav.addItem(new SideNavItem("Post View", PostView.class, VaadinIcon.NEWSPAPER.create()));
        nav.addItem(new SideNavItem("User Interest Match View", UserInterestMatchView.class, VaadinIcon.AIRPLANE.create()));
        if (securityService.isAdmin()) {
            nav.addItem(new SideNavItem("Admin User List", AdminUserListView.class, VaadinIcon.USERS.create()));
        }

        Button footerLogout = new Button("Log out", VaadinIcon.SIGN_OUT.create(), e -> {
            SecurityContextHolder.clearContext();
            if (VaadinSession.getCurrent() != null && VaadinSession.getCurrent().getSession() != null) {
                VaadinSession.getCurrent().getSession().invalidate();
            }
            VaadinSession.getCurrent().close();
            UI.getCurrent().getPage().setLocation("/");
        });
        footerLogout.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        footerLogout.setWidthFull();

        VerticalLayout drawerLayout = new VerticalLayout(nav, footerLogout);
        drawerLayout.setSizeFull();
        drawerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        return new Scroller(drawerLayout);
    }

   /* @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (auth.currentUser().isEmpty()) {
            event.forwardTo(HomeView.class);
        }
    }*/
}
