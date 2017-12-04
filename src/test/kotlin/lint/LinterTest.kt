package lint

import com.google.common.truth.Truth.assertThat
import lint.linters.*
import model.manifest.Manifest
import model.Result
import model.manifest.Deploy
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

    lateinit var parser: IParser
    lateinit var linter: Linter

    @Before
    fun setup() {
        requiredFilesLinter = mock(RequiredFilesLinter::class.java)
        requiredFieldsLinter = mock(RequiredFieldsLinter::class.java)
        repoLinter = mock(RepoLinter::class.java)
        runLinter = mock(RunLinter::class.java)
        deployLinter = mock(DeployLinter::class.java)
        parser = mock(IParser::class.java)

        linter = Linter(
                requiredFilesLinter,
                requiredFieldsLinter,
                runLinter,
                deployLinter,
                repoLinter,
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
        Mockito.verifyZeroInteractions(repoLinter)
        Mockito.verifyZeroInteractions(runLinter)
        Mockito.verifyZeroInteractions(deployLinter)
    }

    @Test
    fun `When manifest has one run task`() {
        val manifest = Manifest(tasks = listOf(Run()))
        val resulta = Result(linter = "files")
        val resultb = Result(linter = "required")
        val resultc = Result(linter = "repo")
        val resultd = Result(linter = "run1")
        given(parser.parseManifest()).willReturn(Optional.of(manifest))

        given(requiredFilesLinter.lint()).willReturn(resulta)
        given(requiredFieldsLinter.lint(manifest)).willReturn(resultb)
        given(repoLinter.lint(manifest)).willReturn(resultc)
        given(runLinter.lint(manifest.tasks[0])).willReturn(resultd)

        linter.lint()

        assertThat(linter.lint()).isEqualTo(listOf(resulta, resultb, resultc, resultd))
        Mockito.verifyZeroInteractions(deployLinter)
    }

    @Test
    fun `When manifest has two test task and one build task`() {
        val manifest = Manifest(tasks = listOf(
                Run("test1"),
                Run("build"),
                Run("test2"),
                Deploy("asdf")
        ))
        val resulta = Result(linter = "files")
        val resultb = Result(linter = "required")
        val resultc = Result(linter = "repo")
        val resultd = Result(linter = "test1")
        val resulte = Result(linter = "build")
        val resultf = Result(linter = "test2")
        val resultg = Result(linter = "deploy")

        given(parser.parseManifest()).willReturn(Optional.of(manifest))
        given(requiredFilesLinter.lint()).willReturn(resulta)
        given(requiredFieldsLinter.lint(manifest)).willReturn(resultb)
        given(repoLinter.lint(manifest)).willReturn(resultc)
        given(runLinter.lint(manifest.tasks[0])).willReturn(resultd)
        given(runLinter.lint(manifest.tasks[1])).willReturn(resulte)
        given(runLinter.lint(manifest.tasks[2])).willReturn(resultf)
        given(deployLinter.lint(manifest.tasks[3])).willReturn(resultg)

        linter.lint()

        assertThat(linter.lint()).isEqualTo(listOf(resulta, resultb, resultc, resultd, resulte, resultf, resultg))
    }
}