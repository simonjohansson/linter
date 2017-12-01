package lint

import com.google.common.truth.Truth.assertThat
import lint.linters.*
import model.Build
import model.Manifest
import model.Result
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
    lateinit var testLinter: TestLinter
    lateinit var buildLinter: BuildLinter
    lateinit var repoLinter: RepoLinter
    lateinit var parser: IParser
    lateinit var linter: Linter

    @Before
    fun setup() {
        requiredFilesLinter = mock(RequiredFilesLinter::class.java)
        requiredFieldsLinter = mock(RequiredFieldsLinter::class.java)
        testLinter = mock(TestLinter::class.java)
        buildLinter = mock(BuildLinter::class.java)
        repoLinter = mock(RepoLinter::class.java)
        parser = mock(IParser::class.java)

        linter = Linter(
                requiredFilesLinter,
                requiredFieldsLinter,
                testLinter,
                buildLinter,
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
        Mockito.verifyZeroInteractions(testLinter)
        Mockito.verifyZeroInteractions(buildLinter)
    }

    @Test
    fun `When manifest has one test task and one build task`() {
        val manifest = Manifest(tasks = listOf(model.Test(), Build()))
        val resulta = Result(linter = "files")
        val resultb = Result(linter = "required")
        val resultc = Result(linter = "repo")
        val resultd = Result(linter = "test")
        val resulte = Result(linter = "build")
        given(parser.parseManifest()).willReturn(Optional.of(manifest))

        given(requiredFilesLinter.lint()).willReturn(resulta)
        given(requiredFieldsLinter.lint(manifest)).willReturn(resultb)
        given(repoLinter.lint(manifest)).willReturn(resultc)
        given(testLinter.lint(manifest.tasks[0])).willReturn(resultd)
        given(buildLinter.lint(manifest.tasks[1])).willReturn(resulte)

        linter.lint()

        assertThat(linter.lint()).isEqualTo(listOf(resulta, resultb, resultc, resultd, resulte))
    }

    @Test
    fun `When manifest has two test task and one build task`() {
        val manifest = Manifest(tasks = listOf(model.Test(), Build(), model.Test()))
        val resulta = Result(linter = "files")
        val resultb = Result(linter = "required")
        val resultc = Result(linter = "repo")
        val resultd = Result(linter = "test")
        val resulte = Result(linter = "build")
        val resultf = Result(linter = "test")

        given(parser.parseManifest()).willReturn(Optional.of(manifest))
        given(requiredFilesLinter.lint()).willReturn(resulta)
        given(requiredFieldsLinter.lint(manifest)).willReturn(resultb)
        given(repoLinter.lint(manifest)).willReturn(resultc)
        given(testLinter.lint(manifest.tasks[0]))
                .willReturn(resultd)
                .willReturn(resultf)
        given(buildLinter.lint(manifest.tasks[1])).willReturn(resulte)

        linter.lint()

        assertThat(linter.lint()).isEqualTo(listOf(resulta, resultb, resultc, resultd, resulte, resultf))

    }
}