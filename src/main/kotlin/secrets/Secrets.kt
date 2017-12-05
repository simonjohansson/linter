package secrets

import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import com.bettercloud.vault.VaultException

interface ISecrets {
    fun exists(org: String, repoName: String, secret_key: String): Boolean
    fun haveToken(): Boolean

}

class Secrets(private val vaultUrl: String = "https://10.244.18.2:8200",
              private val vaultToken: String,
              private val vault: Vault? = null) : ISecrets {
    override fun haveToken() = vaultToken.isNotEmpty()

    private fun getVaultClient(): Vault {
        if (!haveToken()) {
            throw RuntimeException("You must create object with real vault token!")
        }

        if (vault != null) {
            return vault
        }

        val config = VaultConfig()
                .address(vaultUrl)
                .token(vaultToken)
                .openTimeout(5)
                .readTimeout(30)
                .sslVerify(false)
                .build()

        return Vault(config)
    }

    override fun exists(org: String, repoName: String, secret_key: String): Boolean {
        val client = getVaultClient()
        return try {
            val (map, value) = secret_key.replace("(", "").replace(")", "").split(".")
            val path = "/concourse/$org/$repoName/$map"
            val read = client.logical().read(path).data
            (value in read)
        } catch (e: VaultException) {
            false
        }
    }
}