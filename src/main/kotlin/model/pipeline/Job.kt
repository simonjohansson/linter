package model.pipeline

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Job(
        val name: String = "",
        val serial: Boolean = true,
        val plan: List<PlanItem> = emptyList()
)