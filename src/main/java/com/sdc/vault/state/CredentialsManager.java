package com.sdc.vault.state;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

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

    public static void setVaultToken(final URI vaultAddress, final String token) {
        final CredentialAttributes credentialAttributes = new CredentialAttributes(CredentialAttributesKt.generateServiceName(subSystem, vaultAddress.getHost()));
        PasswordSafe.getInstance().set(credentialAttributes, new Credentials(vaultAddress.getHost(), token));
    }

    public static String getVaultToken(final URI vaultAddress) {
        final CredentialAttributes credentialAttributes = new CredentialAttributes(CredentialAttributesKt.generateServiceName(subSystem, vaultAddress.getHost()));
        final String token = PasswordSafe.getInstance().getPassword(credentialAttributes);
        return (token == null) ? "" : token;
    }
}
