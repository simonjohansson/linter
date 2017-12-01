package lint.linters

import com.google.common.truth.Truth
import model.Error
import model.Result

fun assertErrorMessage(result: Result, error_message: String) =
    Truth.assertThat(result.errors.first().message).isEqualTo(error_message)

fun assertErrorMessageInResults(result: Result, error_message: String) =
    Truth.assertThat(error_message).isIn(result.errors.map { it.message })
