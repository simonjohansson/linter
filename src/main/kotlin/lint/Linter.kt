package lint.linters

import model.Result
import model.manifest.Deploy
import model.manifest.Run
import parser.IParser

class Linter(
        private val requiredFilesLinter: RequiredFilesLinter,
        private val requiredFieldsLinter: RequiredFieldsLinter,
        private val runLinter: RunLinter,
        private val deployLinter: DeployLinter,
        private val repoLinter: RepoLinter,
        private val parser: IParser) {

    fun lint(): ArrayList<Result> {
        val result = arrayListOf(requiredFilesLinter.lint())

        parser.parseManifest().map { manifest ->
            result.add(requiredFieldsLinter.lint(manifest))
            result.add(repoLinter.lint(manifest))
            for (task in manifest.tasks) {
                when(task) {
                    is Run ->  result.add(runLinter.lint(task))
                    is Deploy -> result.add(deployLinter.lint(task))
                }
            }
        }

        return result
    }

}