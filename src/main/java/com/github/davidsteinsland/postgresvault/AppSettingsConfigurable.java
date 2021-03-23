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

    private AppSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Vault";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = !mySettingsComponent.getVaultAddrText().equals(settings.vaultAddr);
        modified |= mySettingsComponent.getVaultAuthType() != settings.type;
        modified |= mySettingsComponent.getUsername().equals(settings.oktaUsername);
        modified |= mySettingsComponent.getPassword().equals(settings.oktaPassword);
        return modified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.vaultAddr = mySettingsComponent.getVaultAddrText();
        settings.type = mySettingsComponent.getVaultAuthType();
        settings.oktaUsername = mySettingsComponent.getUsername();
        settings.oktaPassword = mySettingsComponent.getPassword();
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setVaultAddrText(settings.vaultAddr);
        mySettingsComponent.setVaultAuthType(settings.type);
        mySettingsComponent.setUsername(settings.oktaUsername);
        mySettingsComponent.setPassword(settings.oktaPassword);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
