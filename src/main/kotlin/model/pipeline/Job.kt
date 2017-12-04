package model.pipeline

import com.fasterxml.jackson.annotation.JsonInclude
import model.pipeline.plan.PlanItem

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Job(
        val name: String = "",
        val serial: Boolean = true,
        val plan: List<PlanItem> = emptyList()
)