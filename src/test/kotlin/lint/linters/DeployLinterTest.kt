package lint.linters

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import model.manifest.Deploy
import model.manifest.Manifest
import org.junit.Before
import org.junit.Test
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
    fun `it fails if no env`() {
        val deploy = Deploy()
        val result = subject.lint(deploy, Manifest())

        assertErrorMessage(result, "Required field 'env' is missing")

    }

    @Test
    fun `it fails if no manifest yml for cf`() {
        val deploy = Deploy(env = "live")
        val path = "manifest.yml"
        given(reader.fileExists(path)).willReturn(false)

        val result = subject.lint(deploy, Manifest())

        assertErrorMessage(result, "Cannot find Cloud Foundry manifest at path '$path'")
    }

    @Test
    fun `it fails if no manifest yml for cf when given custom path`() {
        val path = "ci/manifest.yml"
        val deploy = Deploy(env = "live", manifest = path)
        given(reader.fileExists(path)).willReturn(false)

        val result = subject.lint(deploy, Manifest())

        assertErrorMessage(result, "Cannot find Cloud Foundry manifest at path '$path'")
    }

    @Test
    fun `it succeeds when everything is ok`() {
        val path = "manifest.yml"
        val deploy = Deploy(env = "live", manifest = path)
        given(reader.fileExists(path)).willReturn(true)

        val result = subject.lint(deploy, Manifest())
        assertThat(result.errors).isEmpty()
    }

    @Test
    fun `Should fail if a var is lowercase`() {
        val path = "manifest.yml"
        val deploy = Deploy(env = "live", manifest = path, vars = mapOf(
                "VAR1" to "value1",
                "VaR2" to "value2",
                "VAR3" to "value3",
                "val3" to "value4"
        ))
        given(reader.fileExists(path)).willReturn(true)

        val result = subject.lint(deploy, Manifest())
        assertThat(result.errors).hasSize(2)
        assertErrorMessage(result, "Environment variable 'VaR2' must be upper case, its a env var yo!")
        assertErrorMessage(result, "Environment variable 'val3' must be upper case, its a env var yo!")
    }

    @Test
    fun `Should fail if a secret is not found lowercase`() {
        val path = "manifest.yml"
        val secret_value_found = "((super-secret))"
        val secret_value_not_found = "((super-secret-not-found))"
        val deploy = Deploy(env = "live", manifest = path, vars = mapOf(
                "VAR1" to "value1",
                "VAR2" to secret_value_found,
                "VAR3" to "value4",
                "VAR4" to secret_value_not_found
        ))
        val manifest = Manifest(org = "yolo", repo = "https://github.sadlfksdf.com/org/repo-name.git")
        given(reader.fileExists(path)).willReturn(true)
        given(secrets.haveToken()).willReturn(true)
        given(secrets.exists(manifest.org, manifest.getRepoName(), secret_value_found)).willReturn(true)
        given(secrets.exists(manifest.org, manifest.getRepoName(), secret_value_not_found)).willReturn(false)

        val result = subject.lint(deploy, manifest)

        assertThat(result.errors).hasSize(1)
        assertErrorMessage(result, "Cannot resolve '/concourse/${manifest.org}/${manifest.getRepoName()}/super-secret-not-found'")
    }

    @Test
    fun `Should fail if there are secrets but no credentials supplied`() {
        val path = "manifest.yml"
        val deploy = Deploy(env = "live", manifest = path, vars = mapOf(
                "VAR2" to "((secret-value))"
        ))
        val manifest = Manifest(org = "yolo", repo = "https://github.sadlfksdf.com/org/repo-name.git")
        given(reader.fileExists(path)).willReturn(true)
        given(secrets.haveToken()).willReturn(false)

        val result = subject.lint(deploy, manifest)

        assertThat(result.errors).hasSize(1)
        assertErrorMessage(result, "You have secrets in your env map, cannot lint unless you pass a vault token with " +
                "`-v vaultToken` to linter!")
    }

    @Test
    fun `Write test`() {
        // secret keys MUST be ((top-level.sub-key)) not ((top-level)) as its a dict
        fail("Write test.")
    }
}