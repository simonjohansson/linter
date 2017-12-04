package secrets

interface ISecrets {
    fun exists(org: String, repoName: String, secret_key: String): Boolean

}

class Secrets: ISecrets {
    override fun exists(org: String, repoName: String, secret_key: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}