package lint.linters

import com.google.common.truth.Truth
import model.Error
import model.Result
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito.mock
import reader.IReader

class TestLinterTest {

    lateinit var reader: IReader

    @Before
    fun setup() {
        reader = mock(IReader::class.java)
    }

    @Test
    fun `it has the correct name`() {
        val linter = TestLinter(reader)

        Truth.assertThat(linter.name()).isEqualTo("Test Task")
    }

    @Test
    fun `it fails if test is specified but no file found`() {
        BDDMockito.given(reader.fileExists("test.sh")).willReturn(false)

        val task = model.Test()
        val linter = TestLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(
                Result(
                        linter = linter.name(),
                        errors = listOf(Error("File 'test.sh' is not found", Error.Type.MISSING_FILE))
                )
        )
    }

    @Test
    fun `it fails if test is specified, file found but not executable`() {
        BDDMockito.given(reader.fileExists("test.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("test.sh")).willReturn(false)

        val task = model.Test()
        val linter = TestLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(
                Result(
                        linter = linter.name(),
                        errors = listOf(Error("File 'test.sh' is not executable", Error.Type.NOT_EXECUTABLE))
                )
        )
    }

    @Test
    fun `it fails if test is specified and different path but no file found`() {
        BDDMockito.given(reader.fileExists("ci/test.sh")).willReturn(false)

        val task = model.Test(command = "ci/test.sh")
        val linter = TestLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(
                Result(
                        linter = linter.name(),
                        errors = listOf(Error("File 'ci/test.sh' is not found", Error.Type.MISSING_FILE))
                )
        )
    }

    @Test
    fun `it fails if test is specified and different path, file found but not executable`() {
        BDDMockito.given(reader.fileExists("ci/test.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("ci/test.sh")).willReturn(false)

        val task = model.Test(command = "ci/test.sh")
        val linter = TestLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(
                Result(
                        linter = linter.name(),
                        errors = listOf(Error("File 'ci/test.sh' is not executable", Error.Type.NOT_EXECUTABLE))
                )
        )
    }

    @Test
    fun `No errors when everything is in order`() {
        BDDMockito.given(reader.fileExists("test.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("test.sh")).willReturn(true)

        val task = model.Test()
        val linter = TestLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(Result(linter = linter.name()))
    }

    @Test
    fun `No errors when everything is in with custom path`() {
        BDDMockito.given(reader.fileExists("ci/test.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("ci/test.sh")).willReturn(true)

        val task = model.Test(command = "ci/test.sh")
        val linter = TestLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(Result(linter = linter.name()))
    }

}