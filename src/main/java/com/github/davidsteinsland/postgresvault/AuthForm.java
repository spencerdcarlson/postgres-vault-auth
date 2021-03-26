package com.github.davidsteinsland.postgresvault;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JPasswordField;

import static com.github.davidsteinsland.postgresvault.VaultAuthMethod.OIDC;

public class AuthForm {
    private static final String subSystem = "com.github.davidsteinsland.postgresvault";
    private VaultAuthMethod method = null;
    private final JBTextField oktaUsername = new JBTextField();
    private final JPasswordField oktaPassword = new JPasswordField();
    private final CredentialAttributes credentialAttributes = new CredentialAttributes(CredentialAttributesKt.generateServiceName(subSystem, "okta"));
    private Credentials oktaCredentials;


    public void setMethod(VaultAuthMethod method) {
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
        final String username = this.oktaUsername.getText();
        System.out.println("getOktaUsername: " + username);
        return username;
    }

    public void setOktaUsername(String username) {
        this.oktaUsername.setText(username);
    }

    public String getOktaPassword() {
        final String password = String.valueOf(this.oktaPassword.getPassword());
        return password;
    }

    public void setOktaPassword(final String password) {
        this.oktaPassword.setText(password);
    }
}
