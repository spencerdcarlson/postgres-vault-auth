// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.davidsteinsland.postgresvault;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

import static com.github.davidsteinsland.postgresvault.VaultAuthMethod.OIDC;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {
    private final ComboBox<VaultAuthMethod> authMethod = new ComboBox<>(new DefaultComboBoxModel<>(VaultAuthMethod.values()));
    private final JPanel myMainPanel = new JPanel();
    private final JBTextField vaultAddr = new JBTextField();
    private final JBTextField username = new JBTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JButton testButton = new JButton("Test Login");

    public AppSettingsComponent() {
        myMainPanel.add(buildForm());

        authMethod.addActionListener(e -> {
            final VaultAuthMethod method = (VaultAuthMethod) authMethod.getSelectedItem();
            repaint(method);
        });

        testButton.addActionListener(e -> {
            final VaultAuthMethod method = (VaultAuthMethod) authMethod.getSelectedItem();
            final Vault vault = new Vault();
            vault.setAddr(getVaultAddrText());
            vault.setAuthMethod(method);
            repaint(method, vault.authenticate(getExtraArgs(method), true));
        });
    }

    private void repaint(final VaultAuthMethod method) {
        repaint(method, null);
    }

    private void repaint(final VaultAuthMethod method, final Boolean isValid) {
        myMainPanel.removeAll();
        myMainPanel.add(buildForm(method, isValid));
        myMainPanel.revalidate();
        myMainPanel.updateUI();
    }

    private Map<String, String> getExtraArgs(final VaultAuthMethod method) {
        switch (method) {
            case OKTA:
                return Map.of("username", getOktaUsername(), "password", getOktaPassword());
            default:
                return Map.of("", "");
        }
    }

    private JPanel buildForm() {
        return buildForm(null, null);
    }

    private JPanel buildForm(final VaultAuthMethod value, final Boolean valid) {
        final FormBuilder builder = FormBuilder.createFormBuilder()
                .addComponent(UI.PanelFactory.panel(vaultAddr).withLabel("Username:").withComment("https://vault-addr.example.org").createPanel(), 1)
                .addComponent(UI.PanelFactory.panel(authMethod).withLabel("Auth Method:").createPanel(), 1)
                .addComponent(buildDynamicForm(value), 1);

        if (valid == null) {
            builder.addComponent(UI.PanelFactory.panel(testButton).createPanel());
        }
        else if (valid) {
            builder.addComponent(UI.PanelFactory.panel(testButton).withComment("<p style='color:green;'>Success</p>").createPanel());
        }
        else {
            builder.addComponent(UI.PanelFactory.panel(testButton).withComment("<p style='color:red;'>Failure</p>").createPanel());
        }
        builder.addComponentFillVertically(new JPanel(), 0);
        return builder.getPanel();
    }

    private JPanel buildDynamicForm(final VaultAuthMethod value) {
        if (value == null || (value != null && value.equals(OIDC))) {
            return new JPanel();
        }

        return FormBuilder.createFormBuilder()
                .addComponent(UI.PanelFactory.panel(username).withLabel("Username:").createPanel(), 1)
                .addComponent(UI.PanelFactory.panel(password).withLabel("Password:").createPanel(), 1)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return vaultAddr;
    }

    @NotNull
    public String getVaultAddrText() {
        return vaultAddr.getText();
    }

    public void setVaultAddrText(@NotNull String newText) {
        vaultAddr.setText(newText);
    }

    public void setVaultAuthMethod(@NotNull VaultAuthMethod method) {
        this.authMethod.setItem(method);
    }

    public VaultAuthMethod getVaultAuthMethod() {
        return (VaultAuthMethod) authMethod.getSelectedItem();
    }

    public String getOktaUsername() {
        return username.getText();
    }

    public void setUsername(@NotNull String username) {
        this.username.setText(username);
    }

    public String getOktaPassword() {
        return String.valueOf(password.getPassword());
    }

    public void setPassword(@NotNull String password) {
        this.password.setText(password);
    }
}
