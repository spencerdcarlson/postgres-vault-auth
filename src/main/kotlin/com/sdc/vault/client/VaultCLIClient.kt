package com.sdc.vault.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.diagnostic.Logger
import com.sdc.vault.VaultAuthMethod
import com.sdc.vault.VaultBundle
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.nio.file.Paths
import kotlin.streams.toList

/**
 * VaultCLIClient is used to authenticate so we don't have to build out all of the
 * possible flows, open web browser, oath, etc
 */
class VaultCLIClient {
    private val logger = Logger.getInstance(VaultCLIClient::class.java)
    private val mapper = jacksonObjectMapper()
    private val vaultExec get() = findExecutable("vault")
    private val executableSearchPaths = listOf(
            "/usr/local/bin",
            "/usr/bin",
            "/bin",
            "/usr/sbin",
            "/sbin",
            System.getProperty("user.home") + "/bin"
    )

    private fun findExecutable(exec: String) =
            executableSearchPaths
                    .firstOrNull { File(it, exec).exists() }
                    ?.let { "$it/$exec" }
                    ?: exec

    fun authenticate(host: URI, method: VaultAuthMethod, args: Map<String, String>): Boolean {
        val extraArgs = args.entries.stream().map { it.toString() }.toList().toTypedArray()

        logger.debug("vault login -method=${method.name.toLowerCase()} ... -format=json")
        logger.trace("vault login -method=${method.name.toLowerCase()} ${extraArgs.contentToString()} -format=json")

        val json = try {
            executeAndReturnJson(
                    host,
                    vaultExec,
                    "login",
                    "-method=${method.name.toLowerCase()}",
                    *extraArgs,
                    "-format=json"
            )
        } catch (e: JsonProcessingException) {
            logger.error("Error parsing result during authentication.", e)
            return false
        } catch (err: IOException) {
            logger.error("Error executing process.", err)
            return false
        }
        val username = json.path("auth").path("metadata").path("username").asText()
        val policies = json.path("auth").path("token_policies").elements().asSequence().toList()
        logger.debug("Login was successful. user=$username policies=$policies")
        return true
    }

    private fun executeAndReturnJson(host: URI, vararg command: String) =
            execute(host, ProcessBuilder(*command)) {
                if (it.exitValue() != 0) {
                    val errorText = it.errorStream.bufferedReader().readText()
                    throw IOException(VaultBundle.property("processFailed", command, errorText))
                }
                mapper.readValue(it.inputStream, ObjectNode::class.java)
            }

    private fun <R> execute(host: URI, pb: ProcessBuilder, onSuccess: (Process) -> R) =
            try {
                logger.debug("VAULT_ADDR=${host}")
                pb.environment()["VAULT_ADDR"] = host.toString()
                pb.start()
            } catch (err: IOException) {
                if (err.message?.contains("""Cannot run program "vault"""") == true) {
                    BrowserUtil.browse(URI("https://learn.hashicorp.com/tutorials/vault/getting-started-install"))
                }
                logger.trace("Failed to execute the following command. " + pb.command().joinToString(separator = " "))
                throw IOException(
                        VaultBundle.property(
                                "processFailed",
                                "vault cmd",
                                err.message ?: ""
                        )
                )
            }.also { it.waitFor() }.let(onSuccess)

    fun readToken(): String {
        return try {
            File(Paths.get(System.getProperty("user.home"), ".vault-token").toString()).readText(Charsets.UTF_8)
        } catch (ex: FileNotFoundException) {
            logger.error("Error reading token", ex)
            return ""
        }
    }
}