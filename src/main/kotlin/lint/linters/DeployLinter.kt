package lint.linters

import model.Error
import model.Result
import model.manifest.Deploy
import model.manifest.ITask
import model.manifest.Manifest
import reader.IReader
import secrets.ISecrets


open class DeployLinter(val reader: IReader, val secrets: ISecrets) : ILinter {
    override fun lint(task: ITask): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun name() = "Deploy"

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun envLinter(deploy: Deploy): List<Error> {
        val errors: ArrayList<Error> = arrayListOf()
        if (deploy.env.isEmpty()) {
            errors.add(model.Error(
                    message = "Required field 'env' is missing",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Deploy#missing_field-env"
            ))
        }
        return errors
    }

    private fun manifestLinter(deploy: Deploy): List<Error> {
        val errors: ArrayList<Error> = arrayListOf()
        if (!reader.fileExists(deploy.manifest)) {
            errors.add(model.Error(
                    message = "Cannot find Cloud Foundry manifest at path '${deploy.manifest}'",
                    type = Error.Type.MISSING_FILE,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Deploy#missing_file"
            ))
        }
        return errors

    }

    private fun environmentVarsKeysLinter(deploy: Deploy) = deploy.vars.keys
            .filter { it != it.toUpperCase() }
            .map { key ->
                model.Error(
                        message = "Environment variable '$key' must be upper case, its a env var yo!",
                        type = Error.Type.BAD_VALUE,
                        documentation = "https://github.com/simonjohansson/linter/wiki/Deploy#bad_value-vars"
                )
            }

    private fun environmentSecretsLinter(deploy: Deploy, manifest: Manifest): List<Error> {
        return deploy.vars.values
                .filter { (it.startsWith("((") and it.endsWith("))")) }
                .filter { !secrets.exists(manifest.org, manifest.getRepoName(), it) }
                .map {
                    val key =it.replace("((", "").replace("))", "")
                    Error(
                            message = "Cannot resolve /concourse/${manifest.org}/${manifest.getRepoName()}/$key",
                            type = Error.Type.BAD_VALUE,
                            documentation = "https://github.com/simonjohansson/linter/wiki/Deploy#bad_value-secret-value"
                    )
                }
    }

    override fun lint(task: ITask, manifest: Manifest): Result {
        val deploy = task as Deploy
        val errors = envLinter(deploy) +
                manifestLinter(deploy) +
                environmentVarsKeysLinter(deploy) +
                environmentSecretsLinter(deploy, manifest)
        return Result(linter = this.name(), errors = errors)
    }
}