// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.davidsteinsland.postgresvault;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {
    private final ComboBox<VaultAuthType> authType = new ComboBox<>(new DefaultComboBoxModel<>(VaultAuthType.values()));
    private final JPanel myMainPanel;
    private JPanel dynamicPannel = new JPanel();
    private final JBTextField vaultAddr = new JBTextField();
    private final JBTextField username = new JBTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JButton login = new JButton("Test Login");

    public AppSettingsComponent() {
        vaultAddr.getEmptyText().setText("https://vault-addr.example.org");

        dynamicPannel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Username: "), username, 1, false)
                .addLabeledComponent(new JBLabel("Password: "), password, 1, false)
                .getPanel();

        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("VAULT_ADDR: "), vaultAddr, 1, false)
                .addComponent(authType, 1)
                .addComponent(dynamicPannel, 1)
                .addComponent(login, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        authType.addActionListener(e -> {
            VaultAuthType value = (VaultAuthType)authType.getSelectedItem();
            System.out.println("Value " + value.name());

            switch (value) {
                case OKTA:
//                    dynamicPannel = FormBuilder.createFormBuilder()
//                            .addLabeledComponent(new JBLabel("Username: "), username, 1, false)
//                            .getPanel();

                    break;
                default:
                    System.out.println("nothing");
            }

            myMainPanel.revalidate();
            myMainPanel.repaint();

        });

        login.addActionListener(e -> {
            VaultAuthType value = (VaultAuthType)authType.getSelectedItem();
            Map<String, String> extraArgs = null;
            switch (value) {
                case OKTA:
                    extraArgs = Map.of("username", getUsername(), "password", getPassword());
                    break;
                default:
                    System.out.println("Nothing");
            }
            Vault vault = new Vault();
            if (vault.authenticate(value, extraArgs, true)) {
                System.out.println("Valid!");
            }
            else {
                System.out.println("Fail");
            }

        });
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

    public void setVaultAuthType(@NotNull VaultAuthType newType) {
        authType.setItem(newType);
    }

    public VaultAuthType getVaultAuthType() {
        return (VaultAuthType)authType.getSelectedItem();
    }

    public String getUsername() {
        return username.getText();
    }

    public void setUsername(@NotNull String username) {
        this.username.setText(username);
    }

    public String getPassword() {
        return String.valueOf(password.getPassword());
    }

    public void setPassword(@NotNull String password) {
        this.password.setText(password);
    }
}
