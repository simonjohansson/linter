package lint.linters

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import model.manifest.Deploy
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import reader.IReader

class DeployLinterTest {

    lateinit var subject: DeployLinter
    lateinit var reader: IReader

    @Before
    fun setup() {
        reader = mock(IReader::class.java)
        subject = DeployLinter(reader)
    }

    @Test
    fun `it has the correct name`() {
        Truth.assertThat(subject.name()).isEqualTo("Deploy")
    }

    @Test
    fun `it fails if missing required fields`() {
        val deploy = Deploy()
        val result = subject.lint(deploy)
        assertErrorMessage(result, "Required fields 'api, space' are missing")

        val deploy2 = Deploy(api = "420")
        val result2 = subject.lint(deploy2)
        assertErrorMessage(result2, "Required fields 'space' are missing")

        val deploy3 = Deploy(api = "420", org = "", username = "", password = "")
        val result3 = subject.lint(deploy3)
        assertErrorMessage(result3, "Required fields 'password, space, username' are missing")

    }

    @Test
    fun `it fails if no manifest yml for cf`() {
        val deploy = Deploy()
        val path = "manifest.yml"
        given(reader.fileExists(path)).willReturn(false)

        val result = subject.lint(deploy)

        assertErrorMessage(result, "Cannot find Cloud Foundry manifest at path '$path'")
    }

    @Test
    fun `it fails if no manifest yml for cf when given custom path`() {
        val path = "ci/manifest.yml"
        val deploy = Deploy(manifest = path)
        given(reader.fileExists(path)).willReturn(false)

        val result = subject.lint(deploy)

        assertErrorMessage(result, "Cannot find Cloud Foundry manifest at path '$path'")
    }

    @Test
    fun `it succeeds when everything is ok`() {
        val path = "manifest.yml"
        val deploy = Deploy(
                api = "api",
                space = "space"
        )

        given(reader.fileExists(path)).willReturn(true)

        val result = subject.lint(deploy)
        assertThat(result.errors).isEmpty()
    }

    @Test
    fun `Should fail if a var is lowercase`() {
        val path = "manifest.yml"
        val deploy = Deploy(manifest = path, vars = mapOf(
                "VAR1" to "value1",
                "VaR2" to "value2",
                "VAR3" to "value3",
                "val3" to "value4"
        ))
        given(reader.fileExists(path)).willReturn(true)

        val result = subject.lint(deploy)
        assertErrorMessage(result, "Environment variable 'VaR2' must be upper case, its a env var yo!")
        assertErrorMessage(result, "Environment variable 'val3' must be upper case, its a env var yo!")
    }
}