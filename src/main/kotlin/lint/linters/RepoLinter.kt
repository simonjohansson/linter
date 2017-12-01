package lint.linters

import model.Error
import model.ITask
import model.Manifest
import model.Result

open class RepoLinter : ILinter {
    override fun name() = "Repo"

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(manifest: Manifest): Result {
        try {
            manifest.getRepoName()
        } catch (e: RuntimeException) {
            return Result(
                    linter = this.name(),
                    errors = listOf(
                            model.Error("'${manifest.repo}' does not look like a real repo!", Error.Type.BAD_VALUE)
                    )
            )
        }
        return Result(linter = this.name())
    }

    override fun lint(task: ITask): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}