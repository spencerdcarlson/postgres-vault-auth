// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.davidsteinsland.postgresvault;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {
    private static final Logger logger = Logger.getInstance(AppSettingsComponent.class);
    private final ComboBox<VaultAuthMethod> authMethod = new ComboBox<>(new DefaultComboBoxModel<>(VaultAuthMethod.values()));
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JBTextField vaultAddr = new JBTextField();
    private final JButton testButton = new JButton("Test Login");
    private final AuthForm authForm = new AuthForm();

    public AppSettingsComponent() {
        mainPanel.add(buildForm());

        authMethod.addActionListener(e -> {
            final VaultAuthMethod method = (VaultAuthMethod) authMethod.getSelectedItem();
            repaint(method);
        });

        testButton.addActionListener(e -> {
            final VaultAuthMethod method = (VaultAuthMethod) authMethod.getSelectedItem();
            final String title = new StringBuilder("vault login --method=").append(method.name().toLowerCase()).toString();
            final Project project = ProjectManager.getInstance().getDefaultProject();
            final Boolean isSuccess = ProgressManager.getInstance().run(new Task.WithResult<>(project, title, false) {
                @Override
                protected Boolean compute(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(true);
                    indicator.setText(progressText(method));

                    final Vault vault = new Vault();
                    vault.setAddr(getVaultAddrText());
                    return vault.authenticate(CredentialsManager.args(method), true);
                }
            });
            repaint(method, isSuccess);
        });
    }

    private String progressText(VaultAuthMethod method) {
        switch (method) {
            case OKTA:
                return "Check your phone";
            case OIDC:
                return "Check your browser";
        }
        return "";
    }

    private void repaint(final VaultAuthMethod method) {
        repaint(method, null);
    }

    private void repaint(final VaultAuthMethod method, final Boolean isSuccess) {
        mainPanel.removeAll();
        mainPanel.add(buildForm(method, isSuccess));
        mainPanel.revalidate();
        mainPanel.updateUI();
    }

    private JPanel buildForm() {
        return buildForm(null, null);
    }

    private JPanel buildForm(final VaultAuthMethod method, final Boolean isSucess) {
        authForm.setMethod(method);

        return FormBuilder.createFormBuilder()
                .addComponent(UI.PanelFactory.panel(vaultAddr).withLabel("VAULT_ADDR:").withComment("https://vault-addr.example.org").createPanel(), 1)
                .addComponent(new TitledSeparator("Authentication"))
                .addComponent(UI.PanelFactory.panel(authMethod).withLabel("Method:").createPanel(), 1)
                .addComponent(authForm.getPanel(), 1)
                .addComponent(UI.PanelFactory.panel(testButton).createPanel())
                .addComponent(new ResultLabel(isSucess).getLabel())
                .addComponentFillVertically(new JPanel(), 0).getPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
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

    public VaultAuthMethod getAuthMethod() {
        return (VaultAuthMethod) authMethod.getSelectedItem();
    }

    public void setAuthMethod(@NotNull VaultAuthMethod method) {
        this.authMethod.setItem(method);
    }

    public String getOktaUserName() {
        return this.authForm.getOktaUsername();
    }

    public String getOktaPassword() {
        return this.authForm.getOktaPassword();
    }

    public void setOktaPassword(@Nullable final String password) {
        this.authForm.setOktaPassword(password);
    }

    public void setOktaUsername(@Nullable final String username) {
        this.authForm.setOktaUsername(username);
    }

}
