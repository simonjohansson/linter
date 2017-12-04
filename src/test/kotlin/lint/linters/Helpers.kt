package lint.linters

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import model.Error
import model.Result

fun assertErrorMessage(result: Result, error_message: String) =
        assertThat(result.errors.map { it.message }).contains(error_message)