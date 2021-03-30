package com.sdc.vault.client

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.diagnostic.Logger
import java.net.Authenticator
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration


class VaultHTTPClient {
    private val logger = Logger.getInstance(VaultHTTPClient::class.java)
    private val mapper = jacksonObjectMapper()
    private val client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .authenticator(Authenticator.getDefault())
            .build()

    fun read(host: URI, token: String, path: String): ObjectNode? {
        logger.debug("Read secret host=${host.host} path=$path")
        val request = HttpRequest.newBuilder()
                .uri(host.resolve(URI.create("/v1/").resolve(URI.create(path))))
                .timeout(Duration.ofMinutes(2))
                .header("X-Vault-Token", token)
                .build()

        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply { obj: HttpResponse<String> -> obj.body() }
                .thenApply { mapper.readValue(it, ObjectNode::class.java) }
                .get()
    }

    fun lookupToken(host: URI, token: String): ObjectNode? {
        // https://www.vaultproject.io/api/auth/token#lookup-a-token-self
        logger.debug("Look up token. host=${host.host}")
        val request = HttpRequest.newBuilder()
                .uri(host.resolve(URI.create("/v1/auth/token/lookup-self")))
                .timeout(Duration.ofMinutes(2))
                .header("X-Vault-Token", token)
                .build()

        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply { obj: HttpResponse<String> -> obj.body() }
                .thenApply { mapper.readValue(it, ObjectNode::class.java) }
                .get()
    }
}
