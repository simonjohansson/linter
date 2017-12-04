package lint.linters

import model.Error
import model.Result
import model.manifest.ITask
import model.manifest.Manifest
import model.manifest.Run
import reader.IReader

open class RunLinter(private val reader: IReader) : ILinter {
    override fun lint(task: ITask, manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun name() = "Run"

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(task: ITask): Result {
        val runTask = task as Run
        return Result(
                linter = this.name(),
                errors = commandLinter(runTask) + imageLinter(runTask)
        )
    }

    private fun imageLinter(runTask: Run): ArrayList<Error> {
        val errors: ArrayList<Error> = arrayListOf()
        if (runTask.image.isBlank()) {
            errors.add(Error(
                    message = "You must specify a image",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Run#missing_field-image"
            ))

        }
        return errors
    }

    private fun commandLinter(runTask: Run): ArrayList<Error> {
        val errors: ArrayList<Error> = arrayListOf()

        if (runTask.command.isBlank()) {
            errors.add(Error(
                    message = "You must specify a command",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Run#missing_field-command"
            ))
        } else {
            val fileExists = this.reader.fileExists(runTask.command)
            if (!fileExists) {
                errors.add(Error(
                        "File '${runTask.command}' is not found",
                        Error.Type.MISSING_FILE,
                        documentation = "https://github.com/simonjohansson/linter/wiki/Run#missing_file"
                ))

            } else {

                val fileExecutable = this.reader.fileExecutable(runTask.command)
                if (!fileExecutable) {
                    errors.add(Error(
                            "File '${runTask.command}' is not executable",
                            Error.Type.NOT_EXECUTABLE,
                            documentation = "https://github.com/simonjohansson/linter/wiki/Run#not_executable"
                    ))
                }
            }
        }
        return errors
    }
}