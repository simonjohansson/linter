package secrets

import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import com.bettercloud.vault.VaultException

interface ISecrets {
    fun exists(org: String, repoName: String, secret_key: String): Boolean
    fun haveToken(): Boolean

}

class Secrets(private val vaultUrl: String = "https://vault:8200", private val vaultToken: String) : ISecrets {
    override fun haveToken() = vaultToken.isNotEmpty()

    override fun exists(org: String, repoName: String, secret_key: String): Boolean {
        if (!haveToken()) {
            throw RuntimeException("You must create object with real vault token!")
        }

        val config = VaultConfig()
                .address("https://10.244.18.2:8200")
                .token(vaultToken)
                .openTimeout(5)
                .readTimeout(30)
                .sslVerify(false)
                .build()

        val vault = Vault(config)
        return try {
            val key = secret_key.replace("(", "").replace(")", "")
            val path = "/concourse/$org/$repoName/$key"
            val read = vault.logical().read(path).data
            (key in read)
        } catch (e: VaultException) {
            false
        }
    }
}