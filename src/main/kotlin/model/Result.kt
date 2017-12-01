package model

data class Error(
        val message: String,
        val type: Error.Type
) {
    enum class Type {
        MISSING_FIELD,
        MISSING_FILE,
        NOT_EXECUTABLE,
        BAD_VALUE
    }
}

data class Warning(
        val message: String,
        val type: Warning.Type
) {
    enum class Type {
    }
}

data class Information(
        val message: String,
        val type: Information.Type
) {
    enum class Type {
    }
}

data class Result(
        val linter: String,
        val errors: List<Error> = listOf(),
        val warning: List<Warning> = listOf()
) {
    fun hasErrors() = errors.isNotEmpty()
}

fun List<Result>.hasErrors() = this.any { it.hasErrors() }