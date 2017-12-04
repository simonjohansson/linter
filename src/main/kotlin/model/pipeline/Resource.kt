package model.pipeline

import com.fasterxml.jackson.annotation.JsonInclude

interface ISource

data class GitSource(
        val uri: String = ""
) : ISource

data class CFSource(
        val api: String = "",
        val username: String = "",
        val password: String = "",
        val organization: String = "",
        val space: String = ""
) : ISource

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Resource(
        val name: String = "",
        val type: String = "",
        val source: ISource
)