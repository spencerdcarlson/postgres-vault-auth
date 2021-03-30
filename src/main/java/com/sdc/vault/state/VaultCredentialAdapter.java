package com.sdc.vault.state;

import com.sdc.vault.VaultAuthMethod;
import com.sdc.vault.settings.ui.AppSettingsComponent;

import java.net.URI;
import java.util.Map;

public class VaultCredentialAdapter {
    private final VaultAuthMethod method;

    public VaultCredentialAdapter(VaultAuthMethod method) {
        this.method = method;
    }

    /**
     * Adapts {@link CredentialsManager} to {@link com.sdc.vault.VaultService#authenticate(URI, VaultAuthMethod, Map, boolean)}
     *
     * @return
     */
    public Map<String, String> getCredentials() {
        switch (method) {
            case OKTA:
                return Map.of("username", CredentialsManager.getOKTAUsername(), "password", CredentialsManager.getOKTAPassword());
            default:
                return Map.of("", "");
        }
    }

    /**
     * Adapts {@link AppSettingsComponent} to {@link com.sdc.vault.VaultService#authenticate(URI, VaultAuthMethod, Map, boolean)}
     *
     * @param component
     * @return
     */
    public Map<String, String> getCredentials(final AppSettingsComponent component) {
        switch (method) {
            case OKTA:
                return Map.of("username", component.getOktaUserName(), "password", component.getOktaPassword());
            default:
                return Map.of("", "");
        }
    }

}
