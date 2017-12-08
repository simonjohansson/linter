package lint.linters

import com.google.common.truth.Truth.assertThat
import model.Error
import model.manifest.*
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import secrets.ISecrets

class SecretsLinterTest {

    lateinit var secrets: ISecrets
    lateinit var subject: SecretsLinter

    @Before
    fun setup() {
        secrets = mock(ISecrets::class.java)
        subject = SecretsLinter(secrets)
    }

    @Test
    fun `When there are no secrets, there are no errors`() {
        val manifest = Manifest(
                tasks = listOf(
                        Run(),
                        Deploy(),
                        Docker()
                )

        )
        val result = subject.lint(manifest)
        assertThat(result.errors).isEmpty()
    }

    @Test
    fun `When there are malformatted secrets there are errors`() {
        val badValue1 = "((heh))"
        val badValue2 = "((c))"
        val badValue3 = "((wryy))"
        val badValue4 = "((username))"
        val badValue5 = "((space))"


        val goodValue1 = "((a.b))"
        val goodValue2 = "((cf.password))"

        val missingValue1 = "((wryy.yolo))"
        val missingValue2 = "((good.value))"

        val manifest = Manifest(
                org = "simon",
                repo = Repo(
                        uri = "git@github.com:simonjohansson/subject.git",
                        private_key = badValue1
                ),
                tasks = listOf(
                        Run(
                                command = "command",
                                image = "image",
                                vars = mapOf(
                                        "A" to "B",
                                        "C" to goodValue1,
                                        "E" to badValue2
                                )

                        ),
                        Deploy(
                                password = goodValue2,
                                api = "api",
                                username = "username",
                                space = badValue5,
                                organization = "org",
                                manifest = "manifest.yml",
                                skip_cert_check = false,
                                vars = mapOf(
                                        "A" to "B",
                                        "C" to badValue3,
                                        "D" to missingValue1
                                )
                        ),
                        Docker(
                                email = "Email",
                                username = badValue4,
                                password = missingValue2,
                                repository = "repo"
                        )
                )
        )

        given(secrets.haveToken()).willReturn(true)
        given(secrets.exists(manifest.org, manifest.getRepoName(), goodValue1)).willReturn(true)
        given(secrets.exists(manifest.org, manifest.getRepoName(), goodValue2)).willReturn(true)
        given(secrets.exists(manifest.org, manifest.getRepoName(), missingValue1)).willReturn(false)
        given(secrets.exists(manifest.org, manifest.getRepoName(), missingValue2)).willReturn(false)

        val result = subject.lint(manifest)

        assertThat(result.errors).hasSize(7)
        errorContainsMalformatedSecret(badValue1, result.errors)
        errorContainsMalformatedSecret(badValue2, result.errors)
        errorContainsMalformatedSecret(badValue3, result.errors)
        errorContainsMalformatedSecret(badValue4, result.errors)
        errorContainsMalformatedSecret(badValue5, result.errors)

        errorContainsMissingVaultValue(missingValue1, result.errors)
        errorContainsMissingVaultValue(missingValue2, result.errors)
    }

    @Test
    fun `Error if there are secrets but no token for the linter`() {
        val manifest = Manifest(
                org = "((its.secret))",
                repo = Repo(
                        uri = "git@github.com:simonjohansson/subject.git"
                )
        )
        given(secrets.haveToken()).willReturn(false)

        val lint = subject.lint(manifest)

        assertThat(lint.errors).hasSize(1)
        assertThat(lint.errors.first().message).isEqualTo(
                "You have secrets in your env map, cannot lint unless you pass a vault token with `-v vaultToken` to linter!"
        )
    }


    private fun errorContainsMalformatedSecret(value: String, errors: List<Error>) {
        assertThat(errors.filter { it.message.contains(value) }).hasSize(1)
    }

    private fun errorContainsMissingVaultValue(value: String, errors: List<Error>) {
        val (map, secret) = value.removePrefix("((").removeSuffix("))").split(".")
        assertThat(errors
                .filter { it.message.contains(map) && it.message.contains(secret) })
                .hasSize(1)

    }

}