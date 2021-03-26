// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.davidsteinsland.postgresvault;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides controller functionality for application settings.
 */
public class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent component;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Vault";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        component = new AppSettingsComponent();
        return component.getPanel();
    }

    @Override
    public boolean isModified() {
        final AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = !component.getVaultAddrText().equals(settings.vaultAddr);
        modified |= component.getVaultAuthMethod() != settings.method;
        modified |= !component.getOktaUserName().equals(CredentialsManager.oktaUsername());
        modified |= !component.getOktaPassword().equals(CredentialsManager.oktaPassword());
        return modified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.vaultAddr = component.getVaultAddrText();
        settings.method = component.getVaultAuthMethod();
        CredentialsManager.setOktaCredentials(component.getOktaUserName(), component.getOktaPassword());
    }

    @Override
    public void reset() {
        final AppSettingsState settings = AppSettingsState.getInstance();
        component.setVaultAddrText(settings.vaultAddr);
        component.setVaultAuthMethod(settings.method);
        component.setOktaUsername(CredentialsManager.oktaUsername());
        component.setOktaPassword(CredentialsManager.oktaPassword());
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }

}
