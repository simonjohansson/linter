package lint

import lint.linters.*
import model.manifest.Deploy
import model.manifest.Docker
import model.manifest.Manifest
import model.manifest.Run
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import parser.IParser
import java.util.*


class LinterTest {

    lateinit var requiredFilesLinter: RequiredFilesLinter
    lateinit var requiredFieldsLinter: RequiredFieldsLinter
    lateinit var requiredAsSecretLinter: RequiredAsSecretLinter
    lateinit var repoLinter: RepoLinter
    lateinit var runLinter: RunLinter
    lateinit var deployLinter: DeployLinter
    lateinit var dockerLinter: DockerLinter
    lateinit var secretsLinter: SecretsLinter


    lateinit var parser: IParser
    lateinit var subject: Linter

    @Before
    fun setup() {
        requiredFilesLinter = mock(RequiredFilesLinter::class.java)
        requiredFieldsLinter = mock(RequiredFieldsLinter::class.java)
        requiredAsSecretLinter = mock(RequiredAsSecretLinter::class.java)
        repoLinter = mock(RepoLinter::class.java)
        runLinter = mock(RunLinter::class.java)
        deployLinter = mock(DeployLinter::class.java)
        dockerLinter = mock(DockerLinter::class.java)
        secretsLinter = mock(SecretsLinter::class.java)
        parser = mock(IParser::class.java)

        subject = Linter(
                requiredFilesLinter,
                requiredFieldsLinter,
                requiredAsSecretLinter,
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
        given(parser.parseManifest()).willReturn(Optional.empty())

        subject.lint()

        Mockito.verify(requiredFilesLinter, times(1)).lint()
        Mockito.verifyZeroInteractions(requiredFieldsLinter)
        Mockito.verifyZeroInteractions(requiredAsSecretLinter)
        Mockito.verifyZeroInteractions(repoLinter)
        Mockito.verifyZeroInteractions(runLinter)
        Mockito.verifyZeroInteractions(deployLinter)
        Mockito.verifyZeroInteractions(dockerLinter)
        Mockito.verifyZeroInteractions(secretsLinter)
    }

    @Test
    fun `When manifest has one run task`() {
        val manifest = Manifest(tasks = listOf(Run()))
        given(parser.parseManifest()).willReturn(Optional.of(manifest))

        subject.lint()


        Mockito.verifyZeroInteractions(deployLinter)
        Mockito.verifyZeroInteractions(dockerLinter)

        Mockito.verify(secretsLinter, times(1)).lint(manifest)
        Mockito.verify(requiredAsSecretLinter, times(1)).lint(manifest)
        Mockito.verify(runLinter, times(1)).lint(manifest.tasks.first())
        Mockito.verify(requiredFilesLinter, times(1)).lint()
        Mockito.verify(requiredFieldsLinter, times(1)).lint(manifest)
        Mockito.verify(repoLinter, times(1)).lint(manifest)
        Mockito.verify(secretsLinter, times(1)).lint(manifest)
    }

    @Test
    fun `When manifest has many tasks`() {
        val manifest = Manifest(tasks = listOf(
                Run("test1"),
                Run("build"),
                Deploy("deploy"),
                Run("test2"),
                Docker()
        ))
        given(parser.parseManifest()).willReturn(Optional.of(manifest))

        subject.lint()

        Mockito.verify(requiredFilesLinter).lint()
        Mockito.verify(requiredFieldsLinter).lint(manifest)
        Mockito.verify(requiredAsSecretLinter).lint(manifest)
        Mockito.verify(repoLinter).lint(manifest)
        Mockito.verify(secretsLinter).lint(manifest)

        Mockito.verify(runLinter).lint(manifest.tasks[0])
        Mockito.verify(runLinter).lint(manifest.tasks[1])
        Mockito.verify(runLinter).lint(manifest.tasks[3])
        Mockito.verify(deployLinter).lint(manifest.tasks[2])
        Mockito.verify(dockerLinter).lint(manifest.tasks[4])

    }
}