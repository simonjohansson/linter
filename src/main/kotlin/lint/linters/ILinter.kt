package lint.linters

import model.manifest.ITask
import model.manifest.Manifest
import model.Result

interface ILinter {
    fun name(): String
    fun lint(): Result
    fun lint(manifest: Manifest): Result
    fun lint(task: ITask): Result
}