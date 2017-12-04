package lint.linters

import model.Result
import model.manifest.ITask
import model.manifest.Manifest

interface ILinter {
    fun name(): String
    fun lint(): Result
    fun lint(manifest: Manifest): Result
    fun lint(task: ITask): Result
    fun lint(task: ITask, manifest: Manifest): Result
}