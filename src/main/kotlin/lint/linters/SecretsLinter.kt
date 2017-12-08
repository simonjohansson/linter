package lint.linters

import model.Result
import model.manifest.ITask
import model.manifest.Manifest
import secrets.ISecrets
import kotlin.reflect.declaredMemberProperties

open class SecretsLinter(val secrets: ISecrets) : ILinter {
    override fun name() = "Secrets"

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /*
    Here be dragons.
    For any object returns all the properties of that object that are strings, recursively calls for the others fields.
    In the end we filter out all fields that starts with (( and ends with )).
     */
    fun findSecretLookingStrings(obj: Any): List<String> = when (obj) {
        is List<*> -> obj.flatMap { findSecretLookingStrings(it!!) }
        is Map<*, *> -> obj.values
                .filter { it is String } as List<String> +
                obj.values.filter { it !is String }
                        .flatMap { findSecretLookingStrings(it!!) }
        else -> (obj.javaClass.kotlin.declaredMemberProperties
                .map { it.get(obj) }
                .filter { it is String } as List<String> +
                obj.javaClass.kotlin.declaredMemberProperties
                        .map { it.get(obj) }
                        .filter { it !is String }
                        .flatMap { findSecretLookingStrings(it!!) })
                .filter { it.startsWith("((") and it.endsWith("))") }
    }

    private fun badFormatError(badSecret: String) = model.Error(
            message = "Your secret keys must be in the format of '((map-name.key-name))' got '$badSecret'",
            type = model.Error.Type.BAD_VALUE,
            documentation = "https://github.com/simonjohansson/linter/wiki/Vault#bad_value-key-error")


    private fun secretNotFoundError(secret: String, manifest: Manifest): model.Error {
        val key = secret.replace("((", "").replace("))", "")
        val (map, value) = key.split(".")


        val message = "Cannot resolve '$value' in '/${secrets.prefix()}/${manifest.org}/${manifest.getRepoName()}/$map' or '/${secrets.prefix()}/${manifest.org}/$map'"
        return model.Error(
                message = message,
                type = model.Error.Type.BAD_VALUE,
                documentation = "https://github.com/simonjohansson/linter/wiki/Vault#bad_value-cannot-resolve"
        )
    }

    private fun badKeys(secretsValues: List<String>) =
            secretsValues
                    .filter { !it.contains(".") }
                    .map { badFormatError(it) }

    private fun missingKeys(secretsValues: List<String>, manifest: Manifest) =
            secretsValues
                    .filter { it.contains(".") && this.secrets.haveToken() }
                    .filter { !secrets.exists(manifest.org, manifest.getRepoName(), it) }
                    .map { secretNotFoundError(it, manifest) }

    private fun missingToken(secretsValues: List<String>): List<model.Error> {
        if (!secrets.haveToken() && secretsValues.filter { it.contains(".") }.isNotEmpty()) {
            return listOf(
                    model.Error(
                            message = "You have secrets in your env map, cannot lint unless you pass a vault token with " +
                                    "`-v vaultToken` to linter!",
                            type = model.Error.Type.LINTER_ERROR,
                            documentation = "https://github.com/simonjohansson/linter/wiki/Vault#linter_error"))
        }
        return emptyList()
    }

    override fun lint(manifest: Manifest): Result {
        val secretsValues = findSecretLookingStrings(manifest)

        val errors = missingToken(secretsValues) +
                badKeys(secretsValues) +
                missingKeys(secretsValues, manifest)

        return Result(
                linter = this.name(),
                errors = errors
        )
    }

    override fun lint(task: ITask): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}