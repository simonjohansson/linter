package secrets

interface ISecrets {
    fun exists(org: String, repoName: String, secret_key: String): Boolean
    fun haveCredentials(): Boolean

}

class Secrets: ISecrets {
    override fun haveCredentials() = true

    override fun exists(org: String, repoName: String, secret_key: String) = false
}