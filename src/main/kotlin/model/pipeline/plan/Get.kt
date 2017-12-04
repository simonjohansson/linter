package model.pipeline.plan

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Get(
        val get: String = "",
        val trigger: Boolean = true,

        @JsonIgnore
        val passed: List<String> = emptyList()
): PlanItem
