package lint.linters

import model.Build
import model.Manifest
import model.Result
import model.Test
import parser.IParser
import reader.IReader

class Linter(
        private val requiredFilesLinter: RequiredFilesLinter,
        private val requiredFieldsLinter: RequiredFieldsLinter,
        private val testLinter: TestLinter,
        private val buildLinter: BuildLinter,
        private val repoLinter: RepoLinter,
        private val parser: IParser) {

    fun lint(): ArrayList<Result> {
        val result = arrayListOf(requiredFilesLinter.lint())

        parser.parseManifest().map { manifest ->
            result.add(requiredFieldsLinter.lint(manifest))
            result.add(repoLinter.lint(manifest))
            for (task in manifest.tasks) {
                when(task) {
                    is Test -> result.add(testLinter.lint(task))
                    is Build -> result.add(buildLinter.lint(task))
                }
            }
        }

        return result
    }

}