package lint.linters

import com.google.common.truth.Truth
import model.Build
import model.Error
import model.Result
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import reader.IReader

class BuildLinterTest {
    lateinit var reader: IReader

    @Before
    fun setup() {
        reader = Mockito.mock(IReader::class.java)
    }

    @Test
    fun `it has the correct name`() {
        val linter = BuildLinter(reader)

        Truth.assertThat(linter.name()).isEqualTo("Build Task")
    }

    @Test
    fun `it fails if test is specified but no file found`() {
        BDDMockito.given(reader.fileExists("build.sh")).willReturn(false)

        val task = Build()
        val linter = BuildLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(
                Result(
                        linter = linter.name(),
                        errors = listOf(Error("File 'build.sh' is not found", Error.Type.MISSING_FILE))
                )
        )
    }

    @Test
    fun `it fails if test is specified, file found but not executable`() {
        BDDMockito.given(reader.fileExists("build.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("build.sh")).willReturn(false)

        val task = Build()
        val linter = BuildLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(
                Result(
                        linter = linter.name(),
                        errors = listOf(Error("File 'build.sh' is not executable", Error.Type.NOT_EXECUTABLE))
                )
        )
    }

    @Test
    fun `it fails if test is specified and different path but no file found`() {
        BDDMockito.given(reader.fileExists("ci/build.sh")).willReturn(false)

        val task = Build(command = "ci/build.sh")
        val linter = BuildLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(
                Result(
                        linter = linter.name(),
                        errors = listOf(Error("File 'ci/build.sh' is not found", Error.Type.MISSING_FILE))
                )
        )
    }

    @Test
    fun `it fails if test is specified and different path, file found but not executable`() {
        BDDMockito.given(reader.fileExists("ci/build.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("ci/build.sh")).willReturn(false)

        val task = Build(command = "ci/build.sh")
        val linter = BuildLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(
                Result(
                        linter = linter.name(),
                        errors = listOf(Error("File 'ci/build.sh' is not executable", Error.Type.NOT_EXECUTABLE))
                )
        )
    }

    @Test
    fun `No errors when everything is in order`() {
        BDDMockito.given(reader.fileExists("build.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("build.sh")).willReturn(true)

        val task = Build()
        val linter = BuildLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(Result(linter = linter.name()))
    }

    @Test
    fun `No errors when everything is in with custom path`() {
        BDDMockito.given(reader.fileExists("ci/build.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("ci/build.sh")).willReturn(true)

        val task = Build(command = "ci/build.sh")
        val linter = BuildLinter(reader)
        val result = linter.lint(task)

        Truth.assertThat(result).isEqualTo(Result(linter = linter.name()))
    }
}