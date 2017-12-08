package lint.linters

import model.Error
import model.Result
import model.manifest.Deploy
import model.manifest.Docker
import model.manifest.ITask
import model.manifest.Manifest

open class RequiredAsSecretLinter(): ILinter {
    override fun name() = "Required Secrets"

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(task: ITask): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun isSecret(value: String) = Regex("""\(\([a-zA-Z0-9\-_]+\.[a-zA-Z0-9\-_]+\)\)""").matches(value)

    private fun makeError(keyName: String) = Error(
            message = "'$keyName' must be a secret!",
            type = Error.Type.BAD_VALUE,
            documentation = "https://github.com/simonjohansson/linter/wiki/Required-Secrets#bad_value"
    )

    override fun lint(manifest: Manifest): Result {
        val errors: ArrayList<Error> = arrayListOf()
        if(manifest.repo.private_key.isNotEmpty() && !isSecret(manifest.repo.private_key)) {
            errors.add(makeError("repo.private_key"))
        }

        manifest.tasks.forEachIndexed { index, task ->
            when(task) {
                is Deploy -> {
                    if(task.password.isNotEmpty() && !isSecret(task.password)) {
                        errors.add(makeError("tasks[$index].password"))
                    }
                }
                is Docker -> {
                    if(task.password.isNotEmpty() && !isSecret(task.password)) {
                        errors.add(makeError("tasks[$index].password"))
                    }
                }
            }

        }

        return Result(
                linter = this.name(),
                errors = errors
        )
    }

}