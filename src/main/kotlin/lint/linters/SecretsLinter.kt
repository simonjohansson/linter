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
            documentation = "https://half-pipe-landing.apps.public.gcp.springernature.io/docs/linter/#vault-key-error")


    private fun secretNotFoundError(secret: String, manifest: Manifest): model.Error {
        val key = secret.replace("((", "").replace("))", "")
        val (map, value) = key.split(".")


        val message = "Cannot resolve '$value' in '/${secrets.prefix()}/${manifest.team}/${manifest.getRepoName()}/$map' or '/${secrets.prefix()}/${manifest.team}/$map'"
        return model.Error(
                message = message,
                type = model.Error.Type.BAD_VALUE,
                documentation = "https://half-pipe-landing.apps.public.gcp.springernature.io/docs/linter/#vault-cannot-resolve"
        )
    }

    private fun isSecret(secret: String) = Regex("""\(\(([a-zA-Z0-9\-_]+)\.([a-zA-Z0-9\-_]+)\)\)""")
            .matches(secret)


    private fun badKeys(secretsValues: List<String>) =
            secretsValues
                    .filter { !isSecret(it) }
                    .map { badFormatError(it) }

    private fun missingKeys(secretsValues: List<String>, manifest: Manifest) =
            secretsValues
                    .filter { isSecret(it) && this.secrets.haveToken() }
                    .filter { !secrets.exists(manifest.team, manifest.getRepoName(), it) }
                    .map { secretNotFoundError(it, manifest) }

    private fun missingToken(secretsValues: List<String>): List<model.Error> {
        if (!secrets.haveToken() && secretsValues.filter { isSecret(it) }.isNotEmpty()) {
            return listOf(
                    model.Error(
                            message = "You have secrets in your env map, cannot lint unless you pass a vault token with " +
                                    "`-v vaultToken` to linter!",
                            type = model.Error.Type.LINTER_ERROR,
                            documentation = "https://half-pipe-landing.apps.public.gcp.springernature.io/docs/linter/#vault-linter-error"))
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