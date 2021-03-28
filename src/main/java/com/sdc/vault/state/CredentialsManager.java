package com.sdc.vault.state;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CredentialsManager {
    private static final String subSystem = "com.sdc.vault";
    private static final CredentialAttributes oktaCredentialAttributes = new CredentialAttributes(CredentialAttributesKt.generateServiceName(subSystem, "okta"));

    public static void setOktaCredentials(@Nullable final String username, @Nullable final String password) {
        PasswordSafe.getInstance().set(oktaCredentialAttributes, new Credentials(username, password));
    }

    @NotNull
    public static String getOKTAUsername() {
        final Credentials credentials = PasswordSafe.getInstance().get(oktaCredentialAttributes);
        return (credentials != null && credentials.getUserName() != null) ? credentials.getUserName() : "";
    }

    @NotNull
    public static String getOKTAPassword() {
        final String password = PasswordSafe.getInstance().getPassword(oktaCredentialAttributes);
        return (password == null) ? "" : password;
    }
}
