package parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.gson.Gson
import model.manifest.*
import org.ini4j.Wini
import reader.IReader
import java.io.InputStream
import java.io.Reader
import java.io.StringReader
import java.util.*
import kotlin.collections.HashMap

interface IParser {
    fun parseManifest(): Optional<Manifest>
}

class Parser(val reader: IReader) : IParser {
    override fun parseManifest() = when (reader.fileExists(".ci.yml")) {
        true -> {
            val content = reader.readFile(".ci.yml")
            if (content.isEmpty()) {
                Optional.of(Manifest())
            } else {
                val mapper = ObjectMapper(YAMLFactory())
                val data = mapper.readValue(content, HashMap::class.java)
                val manifest = mapToManifest(data)

                manifest.let { manifest ->
                    when(manifest.repo.uri.isEmpty()) {
                        true -> Optional.of(addRepoFromGitConfigToManifest(manifest))
                        false -> Optional.of(manifest)
                    }
                }
            }
        }
        false -> Optional.empty()
    }

    private fun addRepoFromGitConfigToManifest(manifest: Manifest): Manifest {
        if(reader.fileExists(".git/config")) {
            val gitConfig = reader.readFile(".git/config")
            val wini = Wini(StringReader(gitConfig))
            wini["remote \"origin\""].let { remote ->
                remote?.get("url").let { url ->
                    if(!url.isNullOrEmpty()) {
                        return manifest.copy(
                                repo = manifest.repo.copy(uri = url!!)
                        )
                    }
                }
            }
        }

        return manifest
    }

    private fun mapToManifest(data: HashMap<*, *>) = Manifest(
            org = getOrg(data),
            repo = getRepo(data),
            tasks = getTasks(data))

    private fun getOrg(data: HashMap<*, *>) = if ("org" in data) {
        data["org"] as String
    } else ""

    private fun getRepo(data: HashMap<*, *>) = if ("repo" in data) {
        parseRepo(data["repo"] as HashMap<String, String>)
    } else Repo()

    private fun parseRepo(repo: HashMap<String, String>): Repo {
        return Gson().fromJson(Gson().toJson(repo), Repo::class.java)
    }

    private fun getTasks(data: HashMap<*, *>) =
            if ("tasks" in data) {
                val mapTasks = data["tasks"] as List<*>
                mapTasks.map({
                    mapTaskToRealTask(it as HashMap<String, *>)
                })
            } else listOf()


    private fun mapTaskToRealTask(task: HashMap<String, *>): ITask = when (task["task"]) {
        "run" -> toITask(task, Run::class.java)
        "deploy" -> toITask(task, Deploy::class.java)
        "docker" -> toITask(task, Docker::class.java)

        else -> {
            throw NotImplementedError("I don't know how to deal with task '${task["task"]}'")
        }
    }

    private fun toITask(task: HashMap<String, *>, type: Class<*>): ITask {
        return Gson().fromJson(Gson().toJson(task), type) as ITask
    }
}