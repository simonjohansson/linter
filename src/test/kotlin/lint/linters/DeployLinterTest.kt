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
    fun `Should fail if a secret is not found lowercase`() {
        val path = "manifest.yml"
        val secret_value_found = "((secret.found))"
        val secret_value_not_found = "((secret.not_found))"
        val deploy = Deploy( manifest = path, vars = mapOf(
                "VAR1" to "value1",
                "VAR2" to secret_value_found,
                "VAR3" to "value4",
                "VAR4" to secret_value_not_found
        ))
        val manifest = Manifest(org = "yolo", repo = Repo("https://github.sadlfksdf.com/org/repo-name.git"))
        given(reader.fileExists(path)).willReturn(true)
        given(secrets.haveToken()).willReturn(true)
        given(secrets.exists(manifest.org, manifest.getRepoName(), secret_value_found)).willReturn(true)
        given(secrets.exists(manifest.org, manifest.getRepoName(), secret_value_not_found)).willReturn(false)
        given(secrets.prefix()).willReturn("springernature")

        val result = subject.lint(deploy, manifest)

        assertErrorMessage(result, "Cannot resolve 'not_found' in '/springernature/${manifest.org}/${manifest.getRepoName()}/secret' or '/springernature/${manifest.org}/secret'")
    }

    @Test
    fun `Should fail if there are secrets but no credentials supplied`() {
        val path = "manifest.yml"
        val deploy = Deploy( manifest = path, vars = mapOf(
                "VAR2" to "((secret.value))"
        ))
        val manifest = Manifest(org = "yolo", repo = Repo("https://github.sadlfksdf.com/org/repo-name.git"))
        given(reader.fileExists(path)).willReturn(true)
        given(secrets.haveToken()).willReturn(false)

        val result = subject.lint(deploy, manifest)

        assertErrorMessage(result, "You have secrets in your env map, cannot lint unless you pass a vault token with " +
                "`-v vaultToken` to linter!")
    }

    @Test
    fun `Fails if secret is not in the right format`() {
        val path = "manifest.yml"
        val deploy = Deploy( manifest = path, vars = mapOf(
                "VAR2" to "((secret-value))"
        ))
        val manifest = Manifest(org = "yolo", repo = Repo("https://github.sadlfksdf.com/org/repo-name.git"))

        given(reader.fileExists(path)).willReturn(true)
        given(secrets.haveToken()).willReturn(true)

        val result = subject.lint(deploy, manifest)

        assertErrorMessage(result, "Your secret keys must be in the format of '((map-name.key-name))' got '((secret-value))'")

    }

    @Test
    fun `Fails if password is not a secret`() {
        val deploy = Deploy(
                password = "ImASecret"
        )

        val result = subject.lint(deploy, Manifest())
        assertErrorMessage(result, "'password' must be a secret")
    }

    @Test
    fun `Fails if password is not in vault`() {
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

        given(secrets.prefix()).willReturn("springernature")

        val result = subject.lint(deploy, manifest)
        assertErrorMessage(result, "Cannot resolve 'secret' in '/springernature/myOrg/linter/super' or '/springernature/myOrg/super'")
    }


}