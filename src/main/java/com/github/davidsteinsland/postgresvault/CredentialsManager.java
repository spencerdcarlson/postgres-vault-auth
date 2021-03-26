package com.github.davidsteinsland.postgresvault;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CredentialsManager {
    private static final String subSystem = "com.github.davidsteinsland.postgresvault";
    private static final CredentialAttributes oktaCredentialAttributes = new CredentialAttributes(CredentialAttributesKt.generateServiceName(subSystem, "okta"));

    public static Map<String, String> args(final VaultAuthMethod method) {
        switch (method) {
            case OKTA:
                return Map.of("username", oktaUsername(), "password", oktaPassword());
            default:
                return Map.of("", "");
        }
    }

    public static void setOktaCredentials(@Nullable final String username, @Nullable final String password) {
        PasswordSafe.getInstance().set(oktaCredentialAttributes, new Credentials(username, password));
    }

    @NotNull
    public static String oktaUsername() {
        final Credentials credentials = PasswordSafe.getInstance().get(oktaCredentialAttributes);
        return (credentials != null && credentials.getUserName() != null) ? credentials.getUserName() : "";
    }

    @NotNull
    public static String oktaPassword() {
        final String password = PasswordSafe.getInstance().getPassword(oktaCredentialAttributes);
        return (password == null) ? "" : password;
    }
}
