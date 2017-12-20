package lint.linters

import com.google.common.truth.Truth.assertThat
import model.manifest.Deploy
import model.manifest.Docker
import model.manifest.Manifest
import model.manifest.Repo
import org.junit.Before
import org.junit.Test

class RequiredAsSecretLinterTest {

    lateinit var subject: RequiredAsSecretLinter

    @Before
    fun setup() {
        subject = RequiredAsSecretLinter()
    }

    @Test
    fun `It has the correct name`() {
        assertThat(subject.name()).isEqualTo("Required Secrets")
    }

    @Test
    fun `Empty manifest should not fail`() {
        val lint = subject.lint(Manifest())
        assertThat(lint.errors).hasSize(0)
    }

    @Test
    fun `When there are issues it errors out`() {
        val manifest = Manifest(
                repo = Repo(
                        private_key = "private key"
                ),
                tasks = listOf(
                        Docker(
                                password = "docker password"
                        ),
                        Deploy(
                                password = "cf password"
                        )
                )
        )

        val result = subject.lint(manifest)

        assertThat(result.errors).hasSize(3)


    }

    @Test
    fun `When all is ok errors is empty`() {
        val manifest = Manifest(
                repo = Repo(
                        private_key = "((private.key))"
                ),
                tasks = listOf(
                        Docker(
                                password = "((docker.docker-password))"
                        ),
                        Deploy(
                                password = "((cf-credentials.password))"
                        )
                )
        )

        val result = subject.lint(manifest)

        assertThat(result.errors).hasSize(0)


    }

}