package model.pipeline

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule


@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Pipeline(
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        val resources: List<Resource> = listOf(Resource()),
        val jobs: List<Job> = listOf(Job())
)

fun Pipeline.toYAML(): String {
    val disableQuotesAroundValues = YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
    val mapper = ObjectMapper(disableQuotesAroundValues)
    mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)

    mapper.registerModule(KotlinModule())
    return mapper.writeValueAsString(this)
}