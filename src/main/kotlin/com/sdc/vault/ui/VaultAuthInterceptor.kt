package com.sdc.vault.ui

import com.intellij.application.ApplicationThreadPool
import com.intellij.database.access.DatabaseCredentials
import com.intellij.database.dataSource.DatabaseAuthProvider
import com.intellij.database.dataSource.DatabaseConnectionInterceptor.ProtoConnection
import com.intellij.database.dataSource.DatabaseCredentialsAuthProvider
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.url.template.MutableParametersHolder
import com.intellij.database.dataSource.url.template.ParametersHolder
import com.intellij.database.dataSource.url.ui.UrlPropertiesPanel.createLabelConstraints
import com.intellij.database.dataSource.url.ui.UrlPropertiesPanel.createSimpleConstraints
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.uiDesigner.core.GridLayoutManager
import com.sdc.vault.VaultBundle.property
import com.sdc.vault.VaultService
import com.sdc.vault.state.AppSettingsState
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import java.net.URI
import java.util.concurrent.CompletionStage
import javax.swing.JPanel

class VaultAuthInterceptor : DatabaseAuthProvider, CoroutineScope {
    private val logger = Logger.getInstance(VaultAuthInterceptor::class.java)

    override val coroutineContext = SupervisorJob() + Dispatchers.ApplicationThreadPool + CoroutineName("VaultAuth")
    override fun getId() = "vault"

    override fun getDisplayName() = property("name")

    override fun isApplicable(dataSource: LocalDataSource) = true

    override fun createWidget(
            project: Project?,
            credentials: DatabaseCredentials,
            dataSource: LocalDataSource
    ): DatabaseAuthProvider.AuthWidget {
        return VaultWidget()
    }

    override fun intercept(connection: ProtoConnection, silent: Boolean): CompletionStage<ProtoConnection>? {
        val path = connection.connectionPoint.additionalJdbcProperties["vault.path"] ?: ""

        if (path.isNullOrEmpty()) throw VaultAuthException(property("invalidMountPath"))

        return future {
            val host = URI.create(connection.connectionPoint.additionalJdbcProperties["vault.addr"])
            val credentials = VaultService().read(host, path)

            logger.debug("Vault read response was successful. username=${credentials.userName}")
            logger.trace("password=${credentials.password}")

            if (credentials.userName.isNullOrEmpty() || credentials.password.isNullOrEmpty()) {
                throw VaultAuthException(property("invalidResponse"))
            }

            // FIXME: ConnectionException is thrown when using SSH on "Test Connection" click
            //  2021-03-26 16:13:34,444 [ 157993]   INFO - om.intellij.ssh.impl.sshj.sshj -
            //  Error from SSHJ local tunnel for SshjSshConnection(ssh-user@ssh-host)@6c9a5e18:
            //  localhost:60695 ==> db-host:5432 while was closing < direct-tcpip
            //  channel: id=0, recipient=0, localWin=[winSize=2094562], remoteWin=[winSize=2095692] >
            //  net.schmizz.sshj.connection.ConnectionException: Disconnected
            //
            //  Connection still ends up working.
            DatabaseCredentialsAuthProvider.applyCredentials(
                    connection,
                    credentials,
                    true
            )
        }
    }

    @Suppress("TooManyFunctions", "EmptyFunctionBlock", "MagicNumber")
    private class VaultWidget : DatabaseAuthProvider.AuthWidget {
        private val pathField = JBTextField()
        private val addrField = JBTextField()
        private val panel = JPanel(GridLayoutManager(2, 6)).apply {
            addrField.emptyText.text = AppSettingsState.getInstance().vaultAddr
            val pathLabel = JBLabel(property("pathLabel"))
            val addrLabel = JBLabel(property("vaultAddrLabel"))

            add(pathLabel, createLabelConstraints(0, 0, pathLabel.preferredSize.getWidth()))
            add(pathField, createSimpleConstraints(0, 1, 3))
            add(addrLabel, createLabelConstraints(1, 0, pathLabel.preferredSize.getWidth()))
            add(addrField, createSimpleConstraints(1, 1, 3))
        }

        override fun save(dataSource: LocalDataSource, copyCredentials: Boolean) {
            dataSource.additionalJdbcProperties["vault.path"] = pathField.text
            dataSource.additionalJdbcProperties["vault.addr"] = addrField.text
        }

        override fun reset(dataSource: LocalDataSource, copyCredentials: Boolean) {
            pathField.text = dataSource.additionalJdbcProperties["vault.path"] ?: ""
            addrField.text = dataSource.additionalJdbcProperties["vault.addr"] ?: ""
        }

        override fun updateUrl(holder: MutableParametersHolder) {}

        override fun updateFromUrl(holder: ParametersHolder) {}

        override fun isPasswordChanged() = false

        override fun hidePassword() {}

        override fun reloadCredentials() {}

        override fun getComponent() = panel

        override fun getPreferredFocusedComponent() = pathField

        override fun forceSave() {}
    }

    internal class VaultAuthException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)
}
