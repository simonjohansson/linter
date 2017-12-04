package lint.linters

import model.*
import model.manifest.ITask
import model.manifest.Manifest
import reader.IReader

open class RequiredFilesLinter(private val reader: IReader) : ILinter {
    override fun name() = "Required Files"

    override fun lint(): Result {
        val result = Result(this.name())
        if(!reader.fileExists(".ci.yml")) {
            return result.copy(
               errors = listOf(model.Error(
                       message = "'.ci.yml' file is missing",
                       type = Error.Type.MISSING_FILE,
                       documentation = "https://github.com/simonjohansson/linter/wiki/Required-Files#missing_file"))
            )
        }
        return result
    }

    override fun lint(manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(task: ITask): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}