package lint.linters

import model.Error
import model.Result
import model.manifest.ITask
import model.manifest.Manifest
import model.manifest.Repo

open class RequiredFieldsLinter() : ILinter {

    override fun lint() = throw DontUseMe()

    override fun lint(task: ITask) = throw DontUseMe()

    override fun name() = "Required Fields"

    override fun lint(manifest: Manifest): Result {
        val errors: ArrayList<Error> = arrayListOf()

        if (manifest.team.isEmpty()) {
            errors.add(model.Error(
                    message = "Required top level field 'team' missing",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://half-pipe-landing.apps.public.gcp.springernature.io/docs/linter/#top-level-missing-field-team"
            ))
        }

        if (manifest.tasks.isEmpty()) {
            errors.add(model.Error(
                    message = "Tasks is empty...",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://half-pipe-landing.apps.public.gcp.springernature.io/docs/manifest/#tasks"
            ))
        }

        if (manifest.repo == Repo()) {
            errors.add(model.Error(
                    message = "Required top level field 'repo' missing",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://half-pipe-landing.apps.public.gcp.springernature.io/docs/manifest/#repo"
            ))
        }


        return Result(linter = this.name(), errors = errors)
    }
}