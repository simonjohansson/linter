package lint.linters

import model.Result
import model.manifest.*
import secrets.ISecrets
import kotlin.reflect.KProperty1
import kotlin.reflect.memberProperties

class SecretsLinter(val secrets: ISecrets) : ILinter {
    override fun name() = "Secrets"

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun <T, R> KProperty1<T, R>.getValue(reciever: T): R {
        return this.get(reciever)!!
    }

    private fun Any.isString() =
            when (this) {
                is String -> true
                else -> false
            }


    private fun traverse(obj: Any): List<String> {
        return when (obj) {
            is Manifest -> {
                val strings = Manifest::class.memberProperties
                        .map { it.getValue(obj)!! }
                        .filter { it.isString() }

                return (strings as List<String> + Manifest::class.memberProperties
                        .map { it.getValue(obj)!! }
                        .filter { !it.isString() }
                        .flatMap { traverse(it) })
                        .filter { it.startsWith("((") and it.endsWith("))") }
            }

            is Repo -> {
                val strings = Repo::class.memberProperties
                        .map { it.getValue(obj)!! }
                        .filter { it.isString() }

                return strings as List<String> + Repo::class.memberProperties
                        .map { it.getValue(obj)!! }
                        .filter { !it.isString() }
                        .flatMap { traverse(it) }
            }
//
            is ITask -> {
                when (obj) {
                    is Run -> {
                        val strings = Run::class.memberProperties
                                .map { it.getValue(obj)!! }
                                .filter { it.isString() }

                        return strings as List<String> + Run::class.memberProperties
                                .map { it.getValue(obj)!! }
                                .filter { !it.isString() }
                                .flatMap { traverse(it) }

                    }

                    is Deploy -> {
                        val strings = Deploy::class.memberProperties
                                .map { it.getValue(obj)!! }
                                .filter { it.isString() }

                        return strings as List<String> + Deploy::class.memberProperties
                                .map { it.getValue(obj)!! }
                                .filter { !it.isString() }
                                .flatMap { traverse(it) }
                    }

                    is Docker -> {
                        val strings = Docker::class.memberProperties
                                .map { it.getValue(obj)!! }
                                .filter { it.isString() }

                        return strings as List<String> + Docker::class.memberProperties
                                .map { it.getValue(obj)!! }
                                .filter { !it.isString() }
                                .flatMap { traverse(it) }
                    }


                    else -> {
                        throw RuntimeException("Not implemented for ${obj::class}")
                    }
                }
            }

            is List<*> -> obj.flatMap { traverse(it!!) }

            is Boolean -> emptyList()

            is Map<*, *> -> {
                val strings = obj.values
                        .filter { it is String }

                return strings as List<String> +
                        obj.values.filter { it !is String }
                                .flatMap { traverse(it!!) }
            }

            else -> {
                throw RuntimeException("Not implemented for ${obj::class}")
            }
        }
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
                    .filter { it.contains(".") && this.secrets.haveToken()}
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
        val secretsValues = traverse(manifest)

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

    override fun lint(task: ITask, manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}