package lint

import com.google.common.truth.Truth.assertThat
import lint.linters.*
import model.Result
import model.manifest.Deploy
import model.manifest.Docker
import model.manifest.Manifest
import model.manifest.Run
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import parser.IParser
import java.util.*


class LinterTest {

    lateinit var requiredFilesLinter: RequiredFilesLinter
    lateinit var requiredFieldsLinter: RequiredFieldsLinter
    lateinit var repoLinter: RepoLinter
    lateinit var runLinter: RunLinter
    lateinit var deployLinter: DeployLinter
    lateinit var dockerLinter: DockerLinter
    lateinit var secretsLinter: SecretsLinter

    lateinit var parser: IParser
    lateinit var linter: Linter

    @Before
    fun setup() {
        requiredFilesLinter = mock(RequiredFilesLinter::class.java)
        requiredFieldsLinter = mock(RequiredFieldsLinter::class.java)
        repoLinter = mock(RepoLinter::class.java)
        runLinter = mock(RunLinter::class.java)
        deployLinter = mock(DeployLinter::class.java)
        dockerLinter = mock(DockerLinter::class.java)
        secretsLinter = mock(SecretsLinter::class.java)
        parser = mock(IParser::class.java)

        linter = Linter(
                requiredFilesLinter,
                requiredFieldsLinter,
                runLinter,
                deployLinter,
                repoLinter,
                dockerLinter,
                secretsLinter,
                parser
        )
    }

    @Test
    fun `When manifest is empty it only calls out to fields linter`() {
        val result = Result(linter = "files")
        given(requiredFilesLinter.lint()).willReturn(result)
        given(parser.parseManifest()).willReturn(Optional.empty())

        assertThat(linter.lint()).isEqualTo(listOf(result))
        Mockito.verifyZeroInteractions(requiredFieldsLinter)
        Mockito.verifyZeroInteractions(secretsLinter)
        Mockito.verifyZeroInteractions(repoLinter)
        Mockito.verifyZeroInteractions(runLinter)
        Mockito.verifyZeroInteractions(deployLinter)
    }

    @Test
    fun `When manifest has one run task`() {
        val manifest = Manifest(tasks = listOf(Run()))
        val files = Result(linter = "files")
        val required = Result(linter = "required")
        val repo = Result(linter = "repo")
        val secrets = Result(linter = "secrets")
        val run = Result(linter = "run1")

        given(parser.parseManifest()).willReturn(Optional.of(manifest))

        given(requiredFilesLinter.lint()).willReturn(files)
        given(requiredFieldsLinter.lint(manifest)).willReturn(required)
        given(repoLinter.lint(manifest)).willReturn(repo)
        given(runLinter.lint(manifest.tasks[0], manifest)).willReturn(run)
        given(secretsLinter.lint(manifest)).willReturn(secrets)

        linter.lint()

        assertThat(linter.lint()).isEqualTo(listOf(files, required, repo, secrets, run))
        Mockito.verifyZeroInteractions(deployLinter)
    }

    @Test
    fun `When manifest has many tasks`() {
        val manifest = Manifest(tasks = listOf(
                Run("test1"),
                Run("build"),
                Run("test2"),
                Deploy("deploy"),
                Docker("docker")
        ))

        val files = Result(linter = "files")
        val required = Result(linter = "required")
        val repo = Result(linter = "repo")
        val secrets = Result(linter = "repo")
        val test1 = Result(linter = "test1")
        val build1 = Result(linter = "build")
        val test2 = Result(linter = "test2")
        val deploy = Result(linter = "deploy")
        val docker = Result(linter = "docker")

        given(parser.parseManifest()).willReturn(Optional.of(manifest))
        given(requiredFilesLinter.lint()).willReturn(files)
        given(requiredFieldsLinter.lint(manifest)).willReturn(required)
        given(repoLinter.lint(manifest)).willReturn(repo)
        given(runLinter.lint(manifest.tasks[0], manifest)).willReturn(test1)
        given(runLinter.lint(manifest.tasks[1], manifest)).willReturn(build1)
        given(runLinter.lint(manifest.tasks[2], manifest)).willReturn(test2)
        given(deployLinter.lint(manifest.tasks[3], manifest)).willReturn(deploy)
        given(dockerLinter.lint(manifest.tasks[4])).willReturn(docker)
        given(dockerLinter.lint(manifest.tasks[4])).willReturn(docker)
        given(secretsLinter.lint(manifest)).willReturn(secrets)

        linter.lint()

        assertThat(linter.lint()).isEqualTo(listOf(files, required, repo, secrets, test1, build1, test2, deploy, docker))
    }
}