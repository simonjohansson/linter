package model.pipeline

import com.fasterxml.jackson.annotation.JsonInclude

interface ISource

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class GitSource(
        val uri: String = "",
        val private_key: String = ""
) : ISource

data class CFSource(
        val api: String = "",
        val username: String = "",
        val password: String = "",
        val organization: String = "",
        val space: String = "",
        val skip_cert_check: Boolean = false

) : ISource

data class DockerSource(
        val email: String = "",
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