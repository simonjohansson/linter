package model.pipeline.plan

import com.fasterxml.jackson.annotation.JsonInclude

data class Params(val environment_variables: Map<String, String> = emptyMap())

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Put(
        val put: String = "",
        val params: Params = Params()
): PlanItem
