package model

interface ITask

data class Build(val command: String = "", val image: String = ""): ITask
data class Test(val command: String = "", val image: String = ""): ITask
data class Deploy(val target: String = ""): ITask

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
