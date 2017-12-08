package lint.linters.helpers

import model.Error
import model.manifest.*
import secrets.ISecrets

fun environmentVarsKeysLinter(task: ITask): List<model.Error> {
    val varsKey = when (task) {
        is Run -> task.vars.keys
        is Deploy -> task.vars.keys
        is Docker -> TODO()
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