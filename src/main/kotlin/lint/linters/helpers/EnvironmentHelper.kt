package lint.linters.helpers

import model.Error
import model.manifest.Deploy
import model.manifest.ITask
import model.manifest.Manifest
import model.manifest.Run
import secrets.ISecrets

fun environmentVarsKeysLinter(task: ITask): List<model.Error> {
    val varsKey = when (task) {
        is Run -> task.vars.keys
        is Deploy -> task.vars.keys
        else -> throw RuntimeException()
    }

    return varsKey.filter { it != it.toUpperCase() }
            .map { key ->
                model.Error(
                        message = "Environment variable '$key' must be upper case, its a env var yo!",
                        type = model.Error.Type.BAD_VALUE,
                        documentation = "https://github.com/simonjohansson/linter/wiki/Vault#bad_value-upper-case"
                )
            }
}

fun environmentSecretsLinter(task: ITask, manifest: Manifest, secrets: ISecrets): List<model.Error> {
    val varsValues = when (task) {
        is Run -> task.vars.values
        is Deploy -> task.vars.values
        else -> throw RuntimeException()
    }
    val secretValues = varsValues
            .filter { (it.startsWith("((") and it.endsWith("))")) }

    val invalidValues = secretValues.filter {
        val vaultKey = it.replace("(", "").replace(")", "")
        vaultKey.split(".").count() != 2
    }

    val errors: ArrayList<Error> = arrayListOf()
    if (secretValues.isNotEmpty()) {
        if (!secrets.haveToken()) {
            errors.add(Error(
                    message = "You have secrets in your env map, cannot lint unless you pass a vault token with " +
                            "`-v vaultToken` to linter!",
                    type = Error.Type.LINTER_ERROR,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Vault#linter_error"))
        }

        errors.addAll(invalidValues.map { invalidValue ->
            Error(
                    message = "Your secret keys must be in the format of '((map-name.key-name))' got '$invalidValue'",
                    type = Error.Type.BAD_VALUE,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Vault#bad_value-key-error")
        })

        errors.addAll(secretValues
                .filter { secrets.haveToken() }
                .filter { it !in invalidValues }
                .filter { !secrets.exists(manifest.org, manifest.getRepoName(), it) }
                .map { secretError(it, manifest, secrets) }
        )
    }

    return errors
}

fun secretError(secret: String, manifest: Manifest, secrets: ISecrets): Error {
    val key = secret.replace("((", "").replace("))", "")
    val (map, value) = key.split(".")


    val message = "Cannot resolve '$value' in '/${secrets.prefix()}/${manifest.org}/${manifest.getRepoName()}/$map' or '/${secrets.prefix()}/${manifest.org}/$map'"
    return Error(
            message = message,
            type = Error.Type.BAD_VALUE,
            documentation = "https://github.com/simonjohansson/linter/wiki/Vault#bad_value-cannot-resolve"
    )
}

