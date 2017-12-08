package lint.linters

import model.Error
import model.Result
import model.manifest.ITask
import model.manifest.Manifest

open class RepoLinter : ILinter {

    override fun name() = "Repo"

    override fun lint() = throw DontUseMe()

    override fun lint(manifest: Manifest): Result {
        val errors: ArrayList<Error> = arrayListOf()
        try {
            manifest.getRepoName()
        } catch (e: RuntimeException) {
            errors.add(
                    model.Error(
                            message = "'${manifest.repo.uri}' does not look like a real repo!",
                            type = Error.Type.BAD_VALUE,
                            documentation = "https://github.com/simonjohansson/linter/wiki/Repo#bad_value"
                    )
            )
        }


        if (manifest.repo.uri.startsWith("git@github.com") and manifest.repo.private_key.isEmpty()) {
            errors.add(
                    model.Error(
                            message = "It looks like you are using SSH, but no private key provided in `repo.deploy_key`",
                            type = Error.Type.MISSING_FIELD,
                            documentation = "https://github.com/simonjohansson/linter/wiki/Repo#missing_field"
                    )
            )
        }

        return Result(
                errors = errors,
                linter = this.name()
        )
    }

    override fun lint(task: ITask) = throw DontUseMe()
}