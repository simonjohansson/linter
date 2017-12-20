package model.manifest

sealed class ITask {
    abstract fun name(): String
}

data class Run(
        val command: String = "",
        val image: String = "",
        val vars: Map<String, String> = emptyMap()) : ITask() {
    override fun name() = this.command.replace("/", ".")
}

data class Deploy(
        val api: String = "",
        val org: String = "",
        val space: String = "",
        val username: String = "((cf-credentials.username))",
        val password: String = "((cf-credentials.password))",
        val manifest: String = "manifest.yml",
        val skip_cert_check: Boolean = false,
        val vars: Map<String, String> = emptyMap()
) : ITask() {
    override fun name() = "deploy-${this.space}"
}

data class Docker(
        val username: String = "",
        val password: String = "",
        val repository: String = ""
) : ITask() {
    override fun name() = "docker-push"
}

data class Repo(val uri: String = "", val private_key: String = "")

data class Manifest(
        val org: String = "",
        val repo: Repo = Repo(),
        val tasks: List<ITask> = listOf()
) {
    fun getRepoName(): String {
        if (this.repo.uri.isEmpty()) {
            throw RuntimeException()
        }
        val regex = Regex(""".*/(.*)\.git""")

        val find = regex.find(this.repo.uri) ?: throw RuntimeException()
        return find.groups.last()!!.value
    }
}
