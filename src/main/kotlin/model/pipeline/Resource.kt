package model.pipeline

import com.fasterxml.jackson.annotation.JsonInclude

interface ISource

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class GitSource(
        val uri: String = "",
        val private_key: String = ""
) : ISource

data class DockerSource(
        val username: String = "",
        val password: String = "",
        val repository: String = ""
) : ISource

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Resource(
        val name: String = "",
        val type: String = "",
        val source: ISource
)