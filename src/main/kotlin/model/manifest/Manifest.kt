package model.manifest

interface ITask {
    fun name() = when(this) {
        is Run -> this.command
        is Deploy -> "deploy-${this.organization}-${this.space}"
        is Docker -> "docker-push"

        else -> {
            throw RuntimeException()
        }
    }
}

data class Run(
        val command: String = "",
        val image: String = "",
        val vars: Map<String, String> = emptyMap()): ITask

data class Deploy(
        val api: String = "",
        val username: String = "",
        val password: String = "",
        val organization: String = "",
        val space: String = "",
        val manifest: String = "manifest.yml",
        val skip_cert_check: Boolean = false,
        val vars: Map<String, String> = emptyMap()
): ITask

data class Docker(
        val email: String = "",
        val username: String = "",
        val password: String = "",
        val repository: String = ""
): ITask

data class Repo(val uri: String = "", val private_key: String = "")

data class Manifest(
        val org: String = "",
        val repo: Repo = Repo(),
        val tasks: List<ITask> = listOf()
) {
    fun getRepoName(): String {
        if(this.repo.uri.isEmpty()) {
            throw RuntimeException()
        }
        val regex = Regex(""".*/(.*)\.git""")

        val find = regex.find(this.repo.uri) ?: throw RuntimeException()
        return find.groups.last()!!.value
    }
}
