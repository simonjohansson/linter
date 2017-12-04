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

data class Run(val command: String = "", val image: String = ""): ITask
data class Deploy(val env: String = "", val manifest: String = "manifest.yml"): ITask

data class Manifest(
        val org: String = "",
        val repo: String = "",
        val tasks: List<ITask> = listOf()
) {
    fun getRepoName(): String {
        if(this.repo.isEmpty()) {
            throw RuntimeException()
        }
        val regex = Regex(""".*/(.*)\.git""")

        val find = regex.find(this.repo) ?: throw RuntimeException()
        return find.groups.last()!!.value
    }
}
