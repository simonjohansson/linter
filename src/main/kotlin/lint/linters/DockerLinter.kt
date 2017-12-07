package lint.linters

import model.Result
import model.manifest.ITask
import model.manifest.Manifest
import reader.Reader
import secrets.Secrets

open class DockerLinter(val reader: Reader, val secrets: Secrets) : ILinter {
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

    override fun lint(task: ITask, manifest: Manifest): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}