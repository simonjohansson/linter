package model

data class Error(
        val message: String,
        val type: Error.Type,
        val documentation: String = ""
) {
    enum class Type {
        MISSING_FIELD,
        MISSING_FILE,
        NOT_EXECUTABLE,
        BAD_VALUE,
        LINTER_ERROR
    }
}

data class Result(
        val linter: String,
        val errors: List<Error> = listOf()
) {
    fun hasErrors() = errors.isNotEmpty()
}

fun List<Result>.hasErrors() = this.any { it.hasErrors() }