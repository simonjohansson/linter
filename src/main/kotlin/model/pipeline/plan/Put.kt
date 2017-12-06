package model.pipeline.plan

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Params(
        val path: String = "",
        val manifest: String = "manifest.yml",
        val environment_variables: Map<String, String> = emptyMap()
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Put(
        val put: String = "",
        val params: Params = Params()
): PlanItem
