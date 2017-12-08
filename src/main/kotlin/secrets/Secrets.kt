package secrets

import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import com.bettercloud.vault.VaultException

interface ISecrets {
    fun exists(org: String, repoName: String, secret_key: String): Boolean
    fun haveToken(): Boolean
    fun prefix(): String

}

class Secrets(private val vaultUrl: String = "https://10.244.18.2:8200",
              private val sslVerify: Boolean = true,
              private val vaultPrefix: String = "springernature",
              private val vaultToken: String,
              private val vault: Vault? = null) : ISecrets {
    private val client: Vault

    override fun haveToken() = vaultToken.isNotEmpty()
    override fun prefix() = vaultPrefix

    init {
        if (vault != null) {
            this.client = vault
        } else {
            val config = VaultConfig()
                    .address(vaultUrl)
                    .token(vaultToken)
                    .openTimeout(5)
                    .readTimeout(30)
                    .sslVerify(sslVerify)
                    .build()
            this.client = Vault(config)
        }
    }

    override fun exists(org: String, repoName: String, secret_key: String): Boolean {
        if (!haveToken()) {
            throw RuntimeException("You must create object with real vault token!")
        }

        return keyInRepo(org, repoName, secret_key) || keyInOrg(org, secret_key)
    }

    private fun getMapValue(secret: String): Pair<String, String> {
        val regex = Regex("""\(\(([a-zA-Z0-9\-_]+)\.([a-zA-Z0-9\-_]+)\)\)""")
        val groupValues = regex.find(secret)!!.groupValues
        if(groupValues.size != 3)
            throw RuntimeException("Could not parse out key value from $secret")

        return (groupValues[1] to groupValues[2])
    }

    private fun keyInRepo(org: String, repoName: String, secret: String): Boolean {
        val (map, value) = getMapValue(secret)
        val path = "/$vaultPrefix/$org/$repoName/$map"
        return valueInPath(value, path)
    }

    private fun keyInOrg(org: String, secret: String): Boolean {
        val (map, value) = getMapValue(secret)
        val path = "/$vaultPrefix/$org/$map"
        return valueInPath(value, path)
    }

    private fun valueInPath(value: String, path: String): Boolean {
        return try {
            val read = client.logical().read(path).data
            return (value in read)
        } catch (e: VaultException) {
            false
        }

    }
}