package model.pipeline.plan

import com.fasterxml.jackson.annotation.JsonInclude

interface IPutParams

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CFParams(
        val path: String = "",
        val manifest: String = "manifest.yml",
        val environment_variables: Map<String, String> = emptyMap()
) : IPutParams

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class DockerParams(
        val build: String = ""
) : IPutParams

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Put(
        val put: String = "",
        val params: IPutParams
) : PlanItem
