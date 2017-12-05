package lint.linters

import lint.linters.helpers.environmentSecretsLinter
import lint.linters.helpers.environmentVarsKeysLinter
import model.Error
import model.Result
import model.manifest.Deploy
import model.manifest.ITask
import model.manifest.Manifest
import reader.IReader
import secrets.ISecrets


open class DeployLinter(val reader: IReader, val secrets: ISecrets) : ILinter {
    override fun lint(task: ITask) = throw DontUseMe()

    override fun name() = "Deploy"

    override fun lint() = throw DontUseMe()

    override fun lint(manifest: Manifest) = throw DontUseMe()

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

    override fun lint(task: ITask, manifest: Manifest): Result {
        val deploy = task as Deploy
        val errors = envLinter(deploy) +
                manifestLinter(deploy) +
                environmentVarsKeysLinter(deploy) +
                environmentSecretsLinter(deploy, manifest, secrets)
        return Result(linter = this.name(), errors = errors)
    }
}