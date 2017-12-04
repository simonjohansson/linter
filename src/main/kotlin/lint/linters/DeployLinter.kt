package lint.linters

import model.Error
import model.Result
import model.manifest.Deploy
import model.manifest.ITask
import model.manifest.Manifest
import reader.IReader


open class DeployLinter(val reader: IReader) : ILinter {
    override fun name() = "Deploy"

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(task: ITask): Result {
        val deploy = task as Deploy

        val errors: ArrayList<Error> = arrayListOf()

        if (deploy.env.isEmpty()) {
            errors.add(model.Error(
                    message = "Required field 'env' is missing",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Deploy#missing_field-env"
            ))
        } else {
            if(!reader.fileExists(task.manifest)) {
                errors.add(model.Error(
                        message = "Cannot find Cloud Foundry manifest at path '${task.manifest}'",
                        type = Error.Type.MISSING_FILE,
                        documentation = "https://github.com/simonjohansson/linter/wiki/Deploy#missing_file"
                ))
            }
        }

        return Result(linter = this.name(), errors = errors)
    }

}