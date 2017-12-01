package model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule


//resources:
//- name: tutorial-release
//  type: git
//  source:
//    uri: https://github.com/docker/labs.git

data class Pipeline(
        val resources: List<Resource> = listOf(Resource())
)

data class Resource(
        val name: String = "",
        val type: String = "git",
        val source: Map<String, String> = mapOf()
)

fun Pipeline.toYAML(): String {
    val mapper = ObjectMapper(YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES))
    mapper.registerModule(KotlinModule())
    return mapper.writeValueAsString(this)
}