package lint.linters

import model.ITask
import model.Manifest
import model.Result

interface ILinter {
    fun name(): String
    fun lint(): Result
    fun lint(manifest: Manifest): Result
    fun lint(task: ITask): Result
}