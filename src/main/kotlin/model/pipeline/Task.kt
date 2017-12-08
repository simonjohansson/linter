package model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

sealed class PlanItem

data class Source(
        val repository: String = "",
        val tag: String = ""
)
data class ImageResource(val type: String = "docker-image", val source: Source = Source())
data class Run(val path: String = "", val dir: String = "")
data class Input(val name: String = "")
data class Config(
        val platform: String = "linux",
        val image_resource: ImageResource = ImageResource(),
        val params: Map<String, String> = emptyMap(),
        val run: Run = Run(),
        val inputs: List<Input> = listOf(Input())
)

data class Task(val task: String = "", val config: Config = Config()): PlanItem()

sealed class IPutParams

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CFParams(
        val path: String = "",
        val manifest: String = "manifest.yml",
        val environment_variables: Map<String, String> = emptyMap()
) : IPutParams()

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class DockerParams(
        val build: String = ""
) : IPutParams()

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Put(
        val put: String = "",
        val params: IPutParams
) : PlanItem()

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Get(
        val get: String = "",
        val trigger: Boolean = true,

        @JsonIgnore
        val passed: List<String> = emptyList()
): PlanItem()