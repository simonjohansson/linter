package lint.linters

import model.*
import reader.IReader

open class TestLinter(private val reader: IReader) : ILinter {
    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(task: ITask): Result = Result(
            linter = this.name(),
            errors = requiredFilesErrors(task as Test)
    )

    override fun name() = "Test Task"


    private fun requiredFilesErrors(test: Test): List<Error> {
        val filePath = if (test.command.isNotEmpty()) {
            test.command
        } else {
            "test.sh"
        }

        val errors: ArrayList<Error> = arrayListOf()
        if (filePath.isNotBlank()) {
            if (!reader.fileExists(filePath)) {
                errors.add(model.Error(
                        message = "File '$filePath' is not found",
                        type = model.Error.Type.MISSING_FILE,
                        documentation = "https://github.com/simonjohansson/linter/wiki/Test#missing_file"))
            }
            if (reader.fileExists(filePath) and !reader.fileExecutable(filePath)) {
                errors.add(Error(
                        message = "File '$filePath' is not executable",
                        type = model.Error.Type.NOT_EXECUTABLE,
                        documentation = "https://github.com/simonjohansson/linter/wiki/Test#not_executable"))
            }
        }
        return errors
    }

}