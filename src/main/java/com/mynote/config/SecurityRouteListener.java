package com.mynote.config;

import com.mynote.ui.views.HomeView;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public class SecurityRouteListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().addBeforeEnterListener(beforeEnterEvent -> {
                Class<?> targetView = beforeEnterEvent.getNavigationTarget();

                if (targetView.equals(HomeView.class)) {
                    return;
                }

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                boolean isAuthenticated = auth != null &&
                        auth.isAuthenticated() &&
                        !auth.getPrincipal().equals("anonymousUser");

                if (!isAuthenticated) {
                    beforeEnterEvent.rerouteTo(HomeView.class);
                }
            });
        });
    }
}
