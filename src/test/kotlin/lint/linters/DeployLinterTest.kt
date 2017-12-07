package lint.linters

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import model.manifest.Deploy
import model.manifest.Manifest
import model.manifest.Repo
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import reader.IReader
import secrets.ISecrets

class DeployLinterTest {

    lateinit var subject: DeployLinter
    lateinit var reader: IReader
    lateinit var secrets: ISecrets

    @Before
    fun setup() {
        reader = mock(IReader::class.java)
        secrets = mock(ISecrets::class.java)
        subject = DeployLinter(reader, secrets)
    }

    @Test
    fun `it has the correct name`() {
        Truth.assertThat(subject.name()).isEqualTo("Deploy")
    }

    @Test
    fun `it fails if missing required fields`() {
        val deploy = Deploy()
        val result = subject.lint(deploy, Manifest())
        assertErrorMessage(result, "Required fields 'api, organization, password, space, username' are missing")

        val deploy2 = Deploy(organization = "yolo", api = "420")
        val result2 = subject.lint(deploy2, Manifest())
        assertErrorMessage(result2, "Required fields 'password, space, username' are missing")
    }

    @Test
    fun `it fails if no manifest yml for cf`() {
        val deploy = Deploy()
        val path = "manifest.yml"
        given(reader.fileExists(path)).willReturn(false)

        val result = subject.lint(deploy, Manifest())

        assertErrorMessage(result, "Cannot find Cloud Foundry manifest at path '$path'")
    }

    @Test
    fun `it fails if no manifest yml for cf when given custom path`() {
        val path = "ci/manifest.yml"
        val deploy = Deploy( manifest = path)
        given(reader.fileExists(path)).willReturn(false)

        val result = subject.lint(deploy, Manifest())

        assertErrorMessage(result, "Cannot find Cloud Foundry manifest at path '$path'")
    }

    @Test
    fun `it succeeds when everything is ok`() {
        val path = "manifest.yml"
        val deploy = Deploy(
                api = "api",
                username = "username",
                password = "((super.secret))",
                organization = "organization",
                space = "space"
        )
        val manifest = Manifest(
                org = "myOrg",
                repo = Repo(uri = "https://github.com/simonjohansson/linter.git")
        )

        given(reader.fileExists(path)).willReturn(true)
        given(secrets.exists(manifest.org, manifest.getRepoName(), deploy.password)).willReturn(true)


        val result = subject.lint(deploy, manifest)
        assertThat(result.errors).isEmpty()
    }

    @Test
    fun `Should fail if a var is lowercase`() {
        val path = "manifest.yml"
        val deploy = Deploy( manifest = path, vars = mapOf(
                "VAR1" to "value1",
                "VaR2" to "value2",
                "VAR3" to "value3",
                "val3" to "value4"
        ))
        given(reader.fileExists(path)).willReturn(true)

        val result = subject.lint(deploy, Manifest())
        assertErrorMessage(result, "Environment variable 'VaR2' must be upper case, its a env var yo!")
        assertErrorMessage(result, "Environment variable 'val3' must be upper case, its a env var yo!")
    }

    @Test
    fun `Fails if password is not a secret`() {
        val deploy = Deploy(
                password = "ImASecret"
        )

        val result = subject.lint(deploy, Manifest())
        assertErrorMessage(result, "'password' must be a secret")
    }

}