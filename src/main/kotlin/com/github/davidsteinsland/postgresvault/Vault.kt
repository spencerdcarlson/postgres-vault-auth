package com.github.davidsteinsland.postgresvault

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.streams.toList

internal class Vault {
    var addr: String = "https://vault.podium.com"
        get() = field
        set(value) { field = value }

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

    fun readJson(path: String): ObjectNode {
        authenticate()
        return executeAndReturnJson(vaultExec, "read", "-format=json", path)
    }

    fun authenticate(type: VaultAuthType = VaultAuthType.OIDC, args: Map<String, String>? = null, force: Boolean = false): Boolean {
        if (isAuthenticated() && !force) return true
        val extraArgs = args?.entries?.stream()?.map { it.toString() }?.toList()?.toTypedArray() ?: arrayOf("")
        println("type: ${type.name}. extra args: ${extraArgs.contentToString()}")
        try {
            executeAndReturnJson(vaultExec, "login", "-method=${type.name.toLowerCase()}", *extraArgs, "-format=json")
        }
        catch (e: IOException) {
            print(e.localizedMessage)
            return false
        }
        return true
    }

    private fun isAuthenticated(): Boolean =
        execute(vaultExec, "token", "lookup") {
            val errorText = it.errorStream.bufferedReader().readText()
            if (it.exitValue() != 0 && !errorText.contains("permission denied")) {
                throw IOException(VaultBundle.message("authenticationFailed", errorText))
            }
            it.exitValue() == 0
        }

    private fun <R> execute(vararg command: String, onSuccess: (Process) -> R) =
        execute(ProcessBuilder(*command), onSuccess)

    private fun executeAndReturnJson(vararg command: String) =
        execute(ProcessBuilder(*command)) {
            if (it.exitValue() != 0) {
                val errorText = it.errorStream.bufferedReader().readText()
                throw IOException(VaultBundle.message("processFailed", command, errorText))
            }
            mapper.readValue(it.inputStream, ObjectNode::class.java)
        }

    private fun <R> execute(pb: ProcessBuilder, onSuccess: (Process) -> R) =
        try {
            pb.environment()["VAULT_ADDR"] = addr
            pb.start()
        } catch (err: IOException) {
            throw IOException(
                VaultBundle.message(
                    "processFailed",
                    pb.command().joinToString(separator = " "),
                    err.message ?: ""
                )
            )
        }.also { it.waitFor() }.let(onSuccess)
}
