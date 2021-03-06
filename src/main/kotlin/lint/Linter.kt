package lint.linters

import model.Error
import model.Result
import model.manifest.Deploy
import model.manifest.Docker
import model.manifest.Manifest
import model.manifest.Run
import parser.IParser

class Linter(
        private val requiredFilesLinter: RequiredFilesLinter,
        private val requiredFieldsLinter: RequiredFieldsLinter,
        private val requiredAsSecretLinter: RequiredAsSecretLinter,
        private val runLinter: RunLinter,
        private val deployLinter: DeployLinter,
        private val repoLinter: RepoLinter,
        private val dockerLinter: DockerLinter,
        private val secretsLinter: SecretsLinter,
        private val parser: IParser) {

    fun lint(): ArrayList<Result> {
        val result = arrayListOf(requiredFilesLinter.lint())

        parser.parseManifest().map { manifest ->

            if (manifest == Manifest()) {
                //manifest is empty..
                result.add(
                        Result(
                                "Linter",
                                listOf(model.Error(
                                        message = "Manifest looks empty",
                                        type = Error.Type.LINTER_ERROR,
                                        documentation = "Todo"
                                ))
                        )
                )
            } else {
                result.add(requiredFieldsLinter.lint(manifest))
                result.add(repoLinter.lint(manifest))
                result.add(secretsLinter.lint(manifest))
                result.add(requiredAsSecretLinter.lint(manifest))

                for (task in manifest.tasks) {
                    when (task) {
                        is Run -> result.add(runLinter.lint(task))
                        is Deploy -> result.add(deployLinter.lint(task))
                        is Docker -> result.add(dockerLinter.lint(task))
                    }
                }
            }
        }

        return result
    }

}