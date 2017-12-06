package model.manifest

interface ITask {
    fun name() = when(this) {
        is Run -> this.command
        is Deploy -> "deploy-${this.env}"

        else -> {
            throw RuntimeException()
        }
    }
}


data class Run(val command: String = "", val image: String = "", val vars: Map<String, String> = emptyMap()): ITask
data class Deploy(val env: String = "", val manifest: String = "manifest.yml", val vars: Map<String, String> = emptyMap()): ITask

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
