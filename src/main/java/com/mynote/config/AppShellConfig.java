package com.mynote.config;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;

/** Loads the Lumo theme once for the whole application. */
@StyleSheet(Lumo.STYLESHEET)
public class AppShellConfig implements AppShellConfigurator {
}
