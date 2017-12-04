package parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.gson.Gson
import model.manifest.Deploy
import model.manifest.ITask
import model.manifest.Manifest
import model.manifest.Run
import reader.IReader
import java.util.*
import kotlin.collections.HashMap

interface IParser {
    fun parseManifest(): Optional<Manifest>
}

class Parser(val reader: IReader) : IParser {
    override fun parseManifest() = when (reader.fileExists(".ci.yml")) {
        true -> {
            val content = reader.readFile(".ci.yml")
            if(content.isEmpty()) {
                Optional.of(Manifest())
            } else {
                val mapper = ObjectMapper(YAMLFactory())
                val data: java.util.HashMap<*, *> = mapper.readValue(content, HashMap::class.java)
                Optional.of(mapToManifest(data))
            }
        }
        false -> Optional.empty()
    }

    private fun mapToManifest(data: HashMap<*, *>): Manifest {
        var org: String = ""
        var repo: String = ""

        if("org" in data) {
            org = data["org"] as String
        }

        if("repo" in data) {
            repo = data["repo"] as String
        }

        var tasks: List<ITask> = emptyList()
        if("tasks" in data) {
            val mapTasks = data["tasks"] as List<*>
            tasks = mapTasks.map({
                mapTaskToRealTask(it as HashMap<String, *>)
            })
        }
        return Manifest(org = org, tasks = tasks, repo = repo)
    }

    private fun mapTaskToRealTask(task: HashMap<String, *>): ITask = when (task["task"]) {
        "run" -> toITask(task, Run::class.java)
        "deploy" -> toITask(task, Deploy::class.java)

        else -> {
            throw NotImplementedError("I don't know how to deal with task '${task["task"]}'")
        }
    }

    private fun toITask(task: HashMap<String, *>, type: Class<*>): ITask {
        val gson = Gson()
        val json = gson.toJson(task)
        return gson.fromJson(json, type) as ITask
    }
}