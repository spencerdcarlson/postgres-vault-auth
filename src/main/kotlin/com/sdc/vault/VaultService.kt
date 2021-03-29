package com.sdc.vault

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.diagnostic.Logger
import com.sdc.vault.state.AppSettingsState
import com.sdc.vault.state.CredentialsManager
import org.apache.log4j.Level
import java.net.URI
import kotlin.streams.toList

internal class VaultService() {
    private val logger = Logger.getInstance(VaultService::class.java)
    private val client = VaultHTTPClient()

    init {
        logger.setLevel(Level.DEBUG)
    }

    fun isAuthenticated(host: URI = URI.create(AppSettingsState.getInstance().vaultAddr), token: String = CredentialsManager.getVaultToken(getHost(host))): Boolean {
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
        token: String = CredentialsManager.getVaultToken(getHost(host)),
        path : String
    ): Credentials {
        logger.debug("Reading credentials for host=${host.host} host=${getHost(host).host} $path token = $token")
        val json =  client.read(getHost(host), token, path)
        json?.let {
            val username = it.path("data").path("username").asText()
            val password = it.path("data").path("password").asText()
            logger.debug("Got credentials. username=$username")
            logger.trace("Got credentials. password=$password")
            return Credentials(username, password)
        }
        return Credentials("", "")
    }

    private fun getHost(host: URI) = if (host.host.isNullOrEmpty()) URI.create(AppSettingsState.getInstance().vaultAddr) else host
}
