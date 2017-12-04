package lint.linters

import com.google.common.truth.Truth
import model.manifest.Deploy
import model.manifest.Manifest
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
    fun `it fails if no env`() {
        val deploy = Deploy()
        val result = subject.lint(deploy)

        assertErrorMessage(result, "Required field 'env' is missing")

    }

    @Test
    fun `it fails if no manifest yml for cf`() {
        val deploy = Deploy(env = "live")
        val path = "manifest.yml"
        given(reader.fileExists(path)).willReturn(false)

        val result = subject.lint(deploy)

        assertErrorMessage(result, "Cannot find Cloud Foundry manifest at path '$path'")
    }

    @Test
    fun `it fails if no manifest yml for cf when given custom path`() {
        val path = "ci/manifest.yml"
        val deploy = Deploy(env = "live", manifest = path)
        given(reader.fileExists(path)).willReturn(false)

        val result = subject.lint(deploy)

        assertErrorMessage(result, "Cannot find Cloud Foundry manifest at path '$path'")
    }
}