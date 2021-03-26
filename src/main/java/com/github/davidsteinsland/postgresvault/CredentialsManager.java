package com.github.davidsteinsland.postgresvault;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import org.jetbrains.annotations.Nullable;

public class CredentialsManager {
    private static final String subSystem = "com.github.davidsteinsland.postgresvault";
    private final CredentialAttributes credentialAttributes;
    private static CredentialsManager instance;

    private CredentialsManager(){
        this.credentialAttributes = new CredentialAttributes(CredentialAttributesKt.generateServiceName(subSystem, "okta"));
    }

    public static CredentialsManager getInstance() {
        if (instance == null) {
            instance = new CredentialsManager();
        }
        return instance;
    }

    public static void setOktaCredentials(final String username, final String password) {
        getInstance().setUsernameAndPassword(username, password);
    }

    public static String oktaUsername() {
        return getInstance().getOktaUsername();
    }

    public static String oktaPassword() {
        return getInstance().getOktaPassword();
    }

    public String getOktaUsername() {
        final Credentials credentials = PasswordSafe.getInstance().get(credentialAttributes);
        if (credentials != null) {
            if (credentials.getUserName() == null) {
                return "";
            }
            return credentials.getUserName();
        }
        return "";
    }

    public String getOktaPassword() {
        final String password = PasswordSafe.getInstance().getPassword(credentialAttributes);
        if (password == null) {
            return "";
        }
        return password;
    }

    public void setUsernameAndPassword(@Nullable final String username, @Nullable final String password) {
        System.out.println("Save username and password");
        PasswordSafe.getInstance().set(credentialAttributes, new Credentials(username, password));
    }
}
