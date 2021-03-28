package com.sdc.vault

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.diagnostic.Logger
import com.sdc.vault.state.AppSettingsState
import com.sdc.vault.state.CredentialsManager
import com.sdc.vault.state.VaultCredentialAdapter
import java.io.File
import java.io.IOException
import kotlin.streams.toList

internal class Vault(private val address: String? = AppSettingsState.getInstance().vaultAddr) {
    private val logger = Logger.getInstance(Vault::class.java)

    private companion object {
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
    }

    fun read(path: String): ObjectNode {
        authenticate()
        logger.debug("vault read $path -format=json")
        return executeAndReturnJson(vaultExec, "read", path, "-format=json")
    }

    fun authenticate(
        method: VaultAuthMethod = AppSettingsState.getInstance().method,
        args: Map<String, String> = VaultCredentialAdapter(method).getCredentials(CredentialsManager()),
        force: Boolean = false
    ): Boolean {
        if (isAuthenticated() && !force) return true

        val extraArgs = args.entries.stream().map { it.toString() }.toList().toTypedArray()

        logger.debug("vault login -method=${method.name.toLowerCase()} ... -format=json")
        logger.trace("vault login -method=${method.name.toLowerCase()} ${extraArgs.contentToString()} -format=json")

        val json = try {
            executeAndReturnJson(vaultExec, "login", "-method=${method.name.toLowerCase()}", *extraArgs, "-format=json")
        } catch (err: JsonProcessingException) {
            logger.error("Error parsing result during authentication.", err)
            return false
        }
        val username = json.path("auth").path("metadata").path("username").asText()
        val policies = json.path("auth").path("token_policies").elements().asSequence().toList()
        logger.debug("Login was successful. user=$username policies=$policies")
        return true
    }

    private fun isAuthenticated(): Boolean {
        logger.debug("vault token lookup -format=json")

        return execute(vaultExec, "token", "lookup", "-format=json") {
            val isAuthenticated = it.exitValue() == 0
            if (!isAuthenticated) {
                val errorText = it.errorStream.bufferedReader().readText()
                logger.debug("isAuth error: $errorText")
            } else {
                try {
                    val json = mapper.readValue(it.inputStream, ObjectNode::class.java)
                    val username = json.path("data").path("meta").path("username").asText()
                    val policies = json.path("data").path("policies").elements().asSequence().toList()
                    logger.debug("isAuthenticated: ${it.exitValue() == 0} user=$username policies=$policies")
                } catch (e: JsonProcessingException) {
                    logger.debug("Could not parse username and policies during isAuthenticated.")
                }
            }
            isAuthenticated
        }
    }


    private fun <R> execute(vararg command: String, onSuccess: (Process) -> R) =
        execute(ProcessBuilder(*command), onSuccess)

    private fun executeAndReturnJson(vararg command: String) =
        execute(ProcessBuilder(*command)) {
            if (it.exitValue() != 0) {
                val errorText = it.errorStream.bufferedReader().readText()
                throw IOException(VaultBundle.property("processFailed", command, errorText))
            }
            mapper.readValue(it.inputStream, ObjectNode::class.java)
        }

    private fun <R> execute(pb: ProcessBuilder, onSuccess: (Process) -> R) =
        try {
            val finalAddress = if (address.isNullOrEmpty()) AppSettingsState.getInstance().vaultAddr else address
            logger.debug("VAULT_ADDR=${finalAddress}")

            // TODO delete ~/.vault-token after executing each command to restore session?
            //  or move any existing ~/.vault-token to ~/.vault-token.backup and then restore after.
            pb.environment()["VAULT_ADDR"] = finalAddress
            pb.start()
        } catch (err: IOException) {
            throw IOException(
                VaultBundle.property(
                    "processFailed",
                    pb.command().joinToString(separator = " "),
                    err.message ?: ""
                )
            )
        }.also { it.waitFor() }.let(onSuccess)
}
