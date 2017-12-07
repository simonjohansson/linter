package lint.linters

import com.google.common.truth.Truth.assertThat
import model.manifest.Manifest
import model.manifest.Repo
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import secrets.ISecrets

class RepoLinterTest {

    lateinit var secrets: ISecrets
    lateinit var subject: RepoLinter

    @Before
    fun setup() {
        secrets = mock(ISecrets::class.java)
        subject = RepoLinter(secrets)
    }

    @Test
    fun `it has the correct name`() {
        assertThat(subject.name()).isEqualTo("Repo")
    }

    @Test
    fun `it fails if repo does not look a real repo`() {
        val repo = "asd.git"
        val manifest = Manifest(repo = Repo(repo))
        val result = subject.lint(manifest)

        assertThat(result.errors).hasSize(1)
        assertErrorMessage(result, "'$repo' does not look like a real repo!")
    }

    @Test
    fun `it works if repo is a real git repo`() {
        val repo = "https://github.com/springernature/yolo.git"
        val manifest = Manifest(repo = Repo(repo))
        val result = subject.lint(manifest)

        assertThat(result.errors).isEmpty()
    }

    @Test
    fun `it fails if repo ssh but no private key provided`() {
        val repo = "git@github.com:simonjohansson/test-repo.git"
        val manifest = Manifest(repo = Repo(repo))
        val result = subject.lint(manifest)

        assertThat(result.errors).hasSize(1)
        assertErrorMessage(result, "It looks like you are using SSH, but no private key provided in `repo.deploy_key`")
    }

    @Test
    fun `it fails if private key is not a var`() {
        val manifest = Manifest(
                repo = Repo("git@github.com:simonjohansson/test-repo.git",
                        private_key = "Im not a secret!")
        )
        val result = subject.lint(manifest)

        assertThat(result.errors).hasSize(1)
        assertErrorMessage(result, "Key provided in 'repo.deploy_key' must be a var, not a key in clear text.")
    }

    @Test
    fun `No errors if private key is in Vault`() {
        val manifest = Manifest(
                org = "yolo",
                repo = Repo(
                        "git@github.com:simonjohansson/test-repo.git",
                        private_key = "((deploy-key.private))")
        )
        given(secrets.exists(manifest.org, manifest.getRepoName(), manifest.repo.private_key))
                .willReturn(true)

        val result = subject.lint(manifest)

        assertThat(result.errors).hasSize(0)
    }

}