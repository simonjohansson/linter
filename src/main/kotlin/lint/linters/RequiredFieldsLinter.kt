package lint.linters

import model.Error
import model.ITask
import model.Manifest
import model.Result
import reader.IReader

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
            errors.add(model.Error("Required top level field 'org' missing", Error.Type.MISSING_FIELD))
        }

        if (manifest.tasks.isEmpty()) {
            errors.add(model.Error("Tasks is empty...", Error.Type.MISSING_FIELD))
        }

        if (manifest.repo.isEmpty()) {
            errors.add(model.Error("Required top level field 'repo' missing", Error.Type.MISSING_FIELD))
        }


        return Result(linter = this.name(), errors = errors)
    }
}