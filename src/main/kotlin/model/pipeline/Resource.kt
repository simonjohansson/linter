package model.pipeline

import com.fasterxml.jackson.annotation.JsonInclude

data class GitSource(
        val uri: String = ""
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Resource(
        val name: String = "",
        val type: String = "git",
        val source: GitSource = GitSource()
)