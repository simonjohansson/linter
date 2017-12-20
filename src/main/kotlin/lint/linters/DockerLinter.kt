package lint.linters

import model.Error
import model.Result
import model.manifest.Docker
import model.manifest.ITask
import model.manifest.Manifest

open class DockerLinter : ILinter {
    override fun name() = "Docker"

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(task: ITask): Result {
        return Result(
                linter = this.name(),
                errors = requiredFieldsLinter(task as Docker)
        )
    }


    private fun requiredFieldsLinter(docker: Docker): List<Error> {
        val missingRequiredFields: ArrayList<String> = arrayListOf()

        if (docker.password.isEmpty())
            missingRequiredFields.add("password")
        if (docker.username.isEmpty())
            missingRequiredFields.add("username")
        if (docker.repository.isEmpty())
            missingRequiredFields.add("repository")

        val missingFields = missingRequiredFields
                .sorted()
                .joinToString(", ")

        val errors: ArrayList<Error> = arrayListOf()
        if (missingFields.isNotEmpty()) {
            errors.add(model.Error(
                    message = "Required fields '${missingRequiredFields.joinToString(", ")}' are missing",
                    type = Error.Type.MISSING_FIELD,
                    documentation = "https://github.com/simonjohansson/linter/wiki/Deploy#missing_field"
            ))
        }

        return errors
    }
}