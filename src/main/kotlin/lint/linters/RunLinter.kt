package lint.linters

import lint.linters.helpers.environmentSecretsLinter
import lint.linters.helpers.environmentVarsKeysLinter
import model.Error
import model.Result
import model.manifest.ITask
import model.manifest.Manifest
import model.manifest.Run
import reader.IReader
import secrets.ISecrets
import secrets.Secrets

open class RunLinter(private val reader: IReader, private val secrets: ISecrets) : ILinter {
    override fun lint(task: ITask) = throw DontUseMe()

    override fun name() = "Run"

    override fun lint() = throw DontUseMe()

    override fun lint(manifest: Manifest) = throw DontUseMe()

    override fun lint(task: ITask, manifest: Manifest): Result {
        val runTask = task as Run
        return Result(
                linter = this.name(),
                errors = commandLinter(runTask) +
                        imageLinter(runTask) +
                        environmentVarsKeysLinter(task) +
                        environmentSecretsLinter(task, manifest, secrets)


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