package build

import model.manifest.Deploy
import model.manifest.ITask
import model.manifest.Manifest
import model.manifest.Run
import model.pipeline.*
import model.pipeline.plan.*


interface IBuild {
    fun build(manifest: Manifest): String
}

class ConcoursePipelineBuilder : IBuild {
    override fun build(manifest: Manifest): String {
        var pipeline = Pipeline()
        if (manifest.repo.uri.isNotEmpty()) {
            pipeline = pipeline.copy(resources = resources(manifest))

            if (manifest.tasks.isNotEmpty())
                pipeline = pipeline.copy(jobs = jobs(manifest.tasks, manifest.getRepoName()))

        }
        return pipeline.toYAML()
    }

    private fun gitResource(manifest: Manifest): Resource {
        return Resource(
                name = manifest.getRepoName(),
                type = "git",
                source = GitSource(
                        uri = manifest.repo.uri,
                        private_key = manifest.repo.private_key
                )
        )
    }

    private fun deployResources(manifest: Manifest) =
            manifest.tasks.filter { it is Deploy }
                    .map {
                        val deploy = it as Deploy
                        Resource(deploy.name(), "cf", CFSource(
                                api = deploy.api,
                                username = deploy.username,
                                password = deploy.password,
                                organization = deploy.organization,
                                space = deploy.space,
                                skip_cert_check = deploy.skip_cert_check

                        ))
                    }

    private fun resources(manifest: Manifest): List<Resource> {
        return emptyList<Resource>() + gitResource(manifest) + deployResources(manifest)
    }

    private fun jobs(tasks: List<ITask>, repoName: String): List<Job> {
        val taskList = arrayListOf<ITask>(Run())
        taskList.addAll(tasks)

        return taskList.zip(taskList.drop(1)).map { (lastTask, currentTask) ->
            when (currentTask) {
                is Run -> {
                    Job(
                            name = currentTask.name(),
                            plan = listOf(
                                    getPlan(lastTask, repoName),
                                    taskPlan(currentTask, repoName)
                            )
                    )
                }
                is Deploy -> {
                    Job(
                            name = currentTask.name(),
                            plan = listOf(
                                    getPlan(lastTask, repoName),
                                    deployPlan(currentTask, repoName)
                            )
                    )
                }
                else -> {
                    throw Exception()
                }
            }
        }
    }

    private fun deployPlan(task: Deploy, repoName: String): Put {
        return Put(
                put = task.name(),
                params = Params(
                        path = repoName,
                        manifest = "$repoName/${task.manifest}",
                        environment_variables = task.vars
                )
        )
    }

    private fun isFirstPlan(lastTask: ITask): Boolean {
        return when (lastTask) {
            is Deploy -> false
            is Run -> lastTask == Run()

            else -> {
                throw RuntimeException()
            }
        }
    }

    private fun getPlan(lastTask: ITask, repoName: String): Get {
        if (isFirstPlan(lastTask)) {
            return Get(repoName)
        }

        return Get(
                repoName,
                passed = listOf(lastTask.name()))
    }

    private fun taskPlan(task: ITask, repoName: String): Task {
        val run = (task as Run)
        return Task(
                task = run.command,
                config = Config(
                        image_resource = ImageResource(
                                source = Source(run.image)
                        ),
                        params = run.vars,
                        run = model.pipeline.plan.Run(
                                path = makeCorrectPath(run.command),
                                dir = repoName
                        ),
                        inputs = listOf(Input(repoName))
                )
        )
    }

    private fun makeCorrectPath(path: String): String {
        if (path.startsWith("./"))
            return path
        return "./$path"
    }
}