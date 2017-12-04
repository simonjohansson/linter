package lint.linters

import model.Error
import model.Result
import model.manifest.ITask
import model.manifest.Manifest

open class RepoLinter : ILinter {
    override fun lint(task: ITask, manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun name() = "Repo"

    override fun lint() = throw DontUseMe()

    override fun lint(manifest: Manifest): Result {
        try {
            manifest.getRepoName()
        } catch (e: RuntimeException) {
            return Result(
                    linter = this.name(),
                    errors = listOf(
                            model.Error(
                                    message = "'${manifest.repo}' does not look like a real repo!",
                                    type = Error.Type.BAD_VALUE,
                                    documentation = "https://github.com/simonjohansson/linter/wiki/Repo#bad_value"
                            ))
            )
        }
        return Result(linter = this.name())
    }

    override fun lint(task: ITask) = throw DontUseMe()
}