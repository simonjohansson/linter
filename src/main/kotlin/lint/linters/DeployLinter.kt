package lint.linters

import lint.linters.helpers.environmentVarsKeysLinter
import model.Error
import model.Result
import model.manifest.Deploy
import model.manifest.ITask
import model.manifest.Manifest
import reader.IReader

open class DeployLinter(val reader: IReader) : ILinter {

    override fun name() = "Deploy"

    override fun lint() = throw DontUseMe()

    override fun lint(manifest: Manifest) = throw DontUseMe()

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

    private fun requiredFieldsLinter(deploy: Deploy): List<Error> {
        val errors: ArrayList<Error> = arrayListOf()

        val missingRequiredFields: ArrayList<String> = arrayListOf()
        if (deploy.api.isEmpty())
            missingRequiredFields.add("api")
        if (deploy.organization.isEmpty())
            missingRequiredFields.add("organization")
        if (deploy.password.isEmpty())
            missingRequiredFields.add("password")
        if (deploy.username.isEmpty())
            missingRequiredFields.add("username")
        if (deploy.space.isEmpty())
            missingRequiredFields.add("space")

        if (missingRequiredFields.isNotEmpty()) {
            missingRequiredFields.sort()
            errors.add(model.Error(
                    message = "Required fields '${missingRequiredFields.joinToString(", ")}' are missing",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Deploy#missing_field"
            ))
        }

        return errors

    }

    override fun lint(task: ITask): Result {
        val deploy = task as Deploy
        val errors = requiredFieldsLinter(deploy) +
                manifestLinter(deploy) +
                environmentVarsKeysLinter(deploy)
        return Result(linter = this.name(), errors = errors)
    }
}