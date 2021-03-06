package lint.linters

import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import reader.IReader

class RequiredFilesLinterTest {
    lateinit var reader: IReader

    @Before
    fun setup() {
        reader = Mockito.mock(IReader::class.java)
    }

    @Test
    fun `it has the correct name`() {
        val linter = RequiredFilesLinter(reader)

        Truth.assertThat(linter.name()).isEqualTo("Required Files")
    }

    @Test
    fun `it fails if halfpipe file is missing`() {
        given(reader.fileExists(".halfpipe.io")).willReturn(false)

        val linter = RequiredFilesLinter(reader)
        val result = linter.lint()
        assertErrorMessage(result, "'.halfpipe.io' file is missing")
    }

    @Test
    fun `it does nit fails if halfpipe file is missing`() {
        given(reader.fileExists(".halfpipe.io")).willReturn(false)

        val linter = RequiredFilesLinter(reader)
        val result = linter.lint()
        assertErrorMessage(result, "'.halfpipe.io' file is missing")
    }
}