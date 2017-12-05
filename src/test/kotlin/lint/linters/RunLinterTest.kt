package lint.linters

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import model.Result
import model.manifest.Manifest
import model.manifest.Run
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito.mock
import reader.IReader
import secrets.ISecrets

class RunLinterTest {

    lateinit var reader: IReader
    lateinit var secrets: ISecrets
    lateinit var subject: RunLinter


    @Before
    fun setup() {
        reader = mock(IReader::class.java)
        secrets = mock(ISecrets::class.java)
        subject = RunLinter(reader, secrets)
    }

    @Test
    fun `it has the correct name`() {
        val linter = RunLinter(reader, secrets)

        Truth.assertThat(linter.name()).isEqualTo("Run")
    }

    @Test
    fun `it fails if nothing is specified`() {
        val task = Run()
        val result = subject.lint(task, Manifest())

        assertThat(result.errors).hasSize(2)
        assertErrorMessage(result, "You must specify a command")
        assertErrorMessage(result, "You must specify a image")
    }

    @Test
    fun `it fails if no command is specified`() {
        val task = Run(image = "asdf")
        val result = subject.lint(task, Manifest())

        assertThat(result.errors).hasSize(1)
        assertErrorMessage(result, "You must specify a command")
    }

    @Test
    fun `it fails if command is specified, but file is not found`() {
        val file = "test.sh"

        BDDMockito.given(reader.fileExists(file)).willReturn(false)

        val task = Run(command = file, image = "jklsafdjkl")
        val result = subject.lint(task, Manifest())

        assertThat(result.errors).hasSize(1)
        assertErrorMessage(result, "File '$file' is not found")
    }

    @Test
    fun `it fails if command is specified, file found, but its not executable`() {
        val file = "test.sh"

        BDDMockito.given(reader.fileExists(file)).willReturn(true)
        BDDMockito.given(reader.fileExecutable(file)).willReturn(false)

        val task = Run(command = file, image = "asdf")
        val result = subject.lint(task, Manifest())

        assertThat(result.errors).hasSize(1)
        assertErrorMessage(result, "File '$file' is not executable")
    }

    @Test
    fun `it fails if test is specified and different path but no file found`() {
        BDDMockito.given(reader.fileExists("ci/test.sh")).willReturn(false)

        val task = Run(command = "ci/test.sh")
        val result = subject.lint(task, Manifest())

        assertErrorMessage(result, "File 'ci/test.sh' is not found")
    }

    @Test
    fun `it fails if test is specified and different path, file found but not executable`() {
        BDDMockito.given(reader.fileExists("ci/test.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("ci/test.sh")).willReturn(false)

        val task = Run(command = "ci/test.sh")
        val result = subject.lint(task, Manifest())

        assertErrorMessage(result, "File 'ci/test.sh' is not executable")
    }

    @Test
    fun `it fails if no image is specified`() {
        BDDMockito.given(reader.fileExists("test.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("test.sh")).willReturn(true)

        val task = Run(command = "test.sh")
        val result = subject.lint(task, Manifest())

        assertThat(result.errors).hasSize(1)
        assertErrorMessage(result, "You must specify a image")
    }

    @Test
    fun `No errors when everything is in order`() {
        BDDMockito.given(reader.fileExists("test.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("test.sh")).willReturn(true)

        val task = Run(command = "test.sh", image = "lkjasdf")
        val result = subject.lint(task, Manifest())

        Truth.assertThat(result).isEqualTo(Result(linter = subject.name()))
    }

    @Test
    fun `It lints environment variables for upper case`() {
        val task = Run(vars = mapOf(
                "VAR1" to "value",
                "VaR2" to "value",
                "VAR3" to "value",
                "var4" to "value"
        ))
        val result = subject.lint(task, Manifest())

        assertErrorMessage(result, "Environment variable 'VaR2' must be upper case, its a env var yo!")
        assertErrorMessage(result, "Environment variable 'var4' must be upper case, its a env var yo!")
    }

    @Test
    fun `It checks that secrets are defined in vault`() {

        val run = Run(
                command = "test.sh",
                image = "asdf",
                vars = mapOf(
                "VAR1" to "((secret))",
                "VAR2" to "((secret.found))",
                "VAR3" to "((secret.not_found))"
        ))

        val manifest = Manifest(org = "yolo", repo = "https://github.sadlfksdf.com/org/repo-name.git")

        BDDMockito.given(reader.fileExists("test.sh")).willReturn(true)
        BDDMockito.given(reader.fileExecutable("test.sh")).willReturn(true)
        BDDMockito.given(secrets.haveToken()).willReturn(true)
        BDDMockito.given(secrets.exists(manifest.org, manifest.getRepoName(), run.vars.get("VAR2")!!)).willReturn(true)
        BDDMockito.given(secrets.exists(manifest.org, manifest.getRepoName(), run.vars.get("VAR3")!!)).willReturn(false)

        val result = subject.lint(run, manifest)

        assertThat(result.errors).hasSize(2)
        assertErrorMessage(result, "Your secret keys must be in the format of '((map-name.key-name))' got '((secret))'")
        assertErrorMessage(result, "Cannot resolve 'not_found' in '/concourse/yolo/repo-name/secret'")

    }
}