package com.sdc.vault

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.diagnostic.Logger
import com.sdc.vault.client.VaultCLIClient
import com.sdc.vault.client.VaultHTTPClient
import com.sdc.vault.state.AppSettingsState
import com.sdc.vault.state.CredentialsManager
import com.sdc.vault.state.VaultCredentialAdapter
import org.apache.log4j.Level
import java.net.URI

internal class VaultService {
    private val logger = Logger.getInstance(VaultService::class.java)
    private val client = VaultHTTPClient()

    fun authenticate(
        host: URI = URI.create(AppSettingsState.getInstance().vaultAddr),
        method: VaultAuthMethod = VaultAuthMethod.OIDC,
        args: Map<String, String> = VaultCredentialAdapter(method).credentials,
        force: Boolean = false,
        refresh: Boolean = false
    ): Boolean {
        logger.debug("Authenticate. force=$force method=$method")
        logger.trace("Authenticate. args=$args")
        if (!force && isAuthenticated(host)) return true
        val client = VaultCLIClient()
        return if (client.authenticate(getHost(host), method, args)) {
            logger.debug("Authenticate was successful. Saving auth token.")
            if (refresh) CredentialsManager.setVaultToken(getHost(host), client.readToken())
            true
        } else false
    }

    private fun isAuthenticated(
        host: URI = URI.create(AppSettingsState.getInstance().vaultAddr),
        token: String = CredentialsManager.getVaultToken(getHost(host))
    ): Boolean {
        logger.debug("Is the user authenticated. host=${host.host}")
        logger.trace("Is the user authenticated. token=$token")
        val json = client.lookupToken(getHost(host), token)
        json?.let {
            val username = it.path("data").path("meta").path("username").asText()
            val policies = it.path("data").path("policies").elements().asSequence().toList()
            val ttl = it.path("data").path("ttl").asInt(0)
            logger.debug("User is authenticated. user=$username policies=$policies ttl=$ttl")
            return ttl > 0
        }
        return false
    }

    fun read(
        host: URI = URI.create(AppSettingsState.getInstance().vaultAddr),
        path: String,
        token: String = CredentialsManager.getVaultToken(getHost(host))
    ): Credentials {
        logger.debug("Reading secret. host=${getHost(host).host} path=$path")
        logger.trace("Reading secret. token=$token")
        authenticate(host)

        logger.debug("Reading secret. Authenticated")
        val json = client.read(getHost(host), token, path)
        json?.let {
            val username = it.path("data").path("username").asText()
            val password = it.path("data").path("password").asText()
            logger.debug("Got credentials. username=$username")
            logger.trace("Got credentials. password=$password")
            return Credentials(username, password)
        }
        return Credentials("", "")
    }

    private fun getHost(host: URI) =
        if (host.host.isNullOrEmpty()) URI.create(AppSettingsState.getInstance().vaultAddr) else host
}
