package lint.linters.helpers

import model.manifest.Deploy
import model.manifest.Docker
import model.manifest.ITask
import model.manifest.Run

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
                        documentation = "https://half-pipe-landing.apps.public.gcp.springernature.io/docs/linter/#bad-value-upper-case"
                )
            }
}