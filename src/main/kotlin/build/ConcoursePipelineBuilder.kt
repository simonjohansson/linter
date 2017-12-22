package build

import model.manifest.*
import model.manifest.Run
import model.pipeline.*


interface IBuild {
    fun build(manifest: Manifest): String
}

class ConcoursePipelineBuilder : IBuild {
    override fun build(manifest: Manifest): String {
        var pipeline = Pipeline()
        if (manifest.repo.uri.isNotEmpty()) {
            pipeline = pipeline.copy(resources = resources(manifest))

            if (manifest.tasks.isNotEmpty())
                pipeline = pipeline.copy(jobs = jobs(manifest))

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

    private fun dockerResource(manifest: Manifest) =
            manifest.tasks.filter { it is Docker }
                    .map {
                        val docker = it as Docker
                        Resource(docker.name(), "docker-image", DockerSource(
                                username = it.username,
                                password = it.password,
                                repository = it.repository
                        ))
                    }

    private fun resources(manifest: Manifest): List<Resource> {
        return emptyList<Resource>() +
                gitResource(manifest) +
                dockerResource(manifest)
    }

    private fun jobs(manifest: Manifest): List<Job> {
        val taskList = arrayListOf<ITask>(Run())
        taskList.addAll(manifest.tasks)

        val repoName = manifest.getRepoName()
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
                    val org = if (currentTask.org.isNotEmpty()) currentTask.org else manifest.team
                    Job(
                            name = currentTask.name(),
                            plan = listOf(
                                    getPlan(lastTask, repoName),
                                    deployPlan(currentTask, manifest)
                            )
                    )
                }
                is Docker -> {
                    Job(
                            name = currentTask.name(),
                            plan = listOf(
                                    getPlan(lastTask, repoName),
                                    dockerPlan(currentTask, repoName)
                            )
                    )
                }
            }
        }
    }

    private fun dockerPlan(task: Docker, repoName: String): Put {
        return Put(
                put = task.name(),
                params = DockerParams(
                        build = repoName
                )
        )
    }

    private fun deployPlan(task: Deploy, manifest: Manifest): Task {
        val repoName = manifest.getRepoName()
        val org = if (task.org.isNotEmpty()) task.org else manifest.team
        val params = task.vars +
                ("API" to task.api) +
                ("USERNAME" to task.username) +
                ("PASSWORD" to task.password) +
                ("ORG" to org) +
                ("SPACE" to task.space) +
                ("MANIFEST_PATH" to task.manifest)

        return Task(
                task = "Deploy to $org: ${task.space}",
                config = Config(
                        image_resource = ImageResource(
                                source = Source(
                                        repository = "simonjohansson/cf-push",
                                        tag = "latest")
                        ),
                        params = params,
                        run = model.pipeline.Run(
                                path = "/bin/sh",
                                args = listOf(
                                        "-exc",
                                        "/bin/cf-push"
                                ),
                                dir = repoName
                        ),
                        inputs = listOf(Input(repoName))
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

    private fun dockerImage(image: String): Pair<String, String> {
        if (image.contains(":")) {
            val split = image.split(":")
            return (split[0] to split[1])
        }

        return (image to "latest")
    }

    private fun taskPlan(task: ITask, repoName: String): Task {
        val run = (task as Run)
        val (image, tag) = dockerImage(run.image)
        return Task(
                task = run.name(),
                config = Config(
                        image_resource = ImageResource(
                                source = Source(
                                        repository = image,
                                        tag = tag)
                        ),
                        params = run.vars,
                        run = model.pipeline.Run(
                                path = "/bin/sh",
                                args = listOf(
                                        "-exc",
                                        makeCorrectPath(run.script)
                                ),
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