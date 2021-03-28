package com.sdc.vault.settings.ui;

import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import com.sdc.vault.VaultAuthMethod;

import javax.swing.*;

import static com.sdc.vault.VaultAuthMethod.OIDC;

public class AuthForm {
    private final JBTextField oktaUsername = new JBTextField();
    private final JPasswordField oktaPassword = new JPasswordField();
    private VaultAuthMethod method = null;


    public void setMethod(final VaultAuthMethod method) {
        this.method = method;
    }

    public JPanel getPanel() {
        if (this.method == null || (this.method != null && method.equals(OIDC))) {
            return new JPanel();
        }
        return FormBuilder.createFormBuilder()
                .addComponent(UI.PanelFactory.panel(oktaUsername).withLabel("Username:").createPanel(), 1)
                .addComponent(UI.PanelFactory.panel(oktaPassword).withLabel("Password:").createPanel(), 1)
                .getPanel();
    }

    public String getOktaUsername() {
        return this.oktaUsername.getText();
    }

    public void setOktaUsername(final String username) {
        this.oktaUsername.setText(username);
    }

    public String getOktaPassword() {
        return String.valueOf(this.oktaPassword.getPassword());
    }

    public void setOktaPassword(final String password) {
        this.oktaPassword.setText(password);
    }
}
