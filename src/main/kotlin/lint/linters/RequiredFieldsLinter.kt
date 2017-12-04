package lint.linters

import model.Error
import model.manifest.ITask
import model.manifest.Manifest
import model.Result

open class RequiredFieldsLinter() : ILinter {
    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(task: ITask): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun name() = "Required Fields"

    override fun lint(manifest: Manifest): Result {
        val errors: ArrayList<Error> = arrayListOf()

        if (manifest.org.isEmpty()) {
            errors.add(model.Error(
                    message = "Required top level field 'org' missing",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Required-Fields#missing_field-org"
            ))
        }

        if (manifest.tasks.isEmpty()) {
            errors.add(model.Error(
                    message = "Tasks is empty...",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Required-Fields#missing_field-tasks"
            ))
        }

        if (manifest.repo.isEmpty()) {
            errors.add(model.Error(
                    message = "Required top level field 'repo' missing",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Required-Fields#missing_field-repo"
            ))
        }


        return Result(linter = this.name(), errors = errors)
    }
}