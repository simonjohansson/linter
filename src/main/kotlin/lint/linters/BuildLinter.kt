package lint.linters

import model.*
import reader.IReader

open class BuildLinter(private val reader: IReader) : ILinter {

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(manifest: Manifest): Result {
        TODO("not implemented")
    }

    override fun lint(task: ITask) = Result(
            linter = this.name(),
            errors = requiredFilesErrors(task as Build)
    )

    override fun name() = "Build Task"

    private fun requiredFilesErrors(test: Build): List<Error> {
        val filePath = if (test.command.isNotEmpty()) {
            test.command
        } else {
            "build.sh"
        }

        val errors: ArrayList<Error> = arrayListOf()
        if (filePath.isNotBlank()) {
            if (!reader.fileExists(filePath)) {
                errors.add(model.Error("File '$filePath' is not found", model.Error.Type.MISSING_FILE))
            }
            if (reader.fileExists(filePath) and !reader.fileExecutable(filePath)) {
                errors.add(Error("File '$filePath' is not executable", model.Error.Type.NOT_EXECUTABLE))
            }
        }
        return errors
    }

}