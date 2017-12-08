package lint.linters

import model.Result
import model.manifest.ITask
import model.manifest.Manifest
import reader.Reader
import secrets.Secrets

open class DockerLinter(val reader: Reader) : ILinter {
    override fun name() = "Docker"

    override fun lint(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lint(task: ITask): Result {
        return Result(this.name())
    }

}