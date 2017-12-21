package lint.linters

import com.google.common.truth.Truth.assertThat
import model.manifest.Manifest
import model.manifest.Repo
import model.manifest.Run
import org.junit.Test

class RequiredFieldsLinterTest {

    @Test
    fun `it has the corrent name`() {
        val linter = RequiredFieldsLinter()

        assertThat(linter.name()).isEqualTo("Required Fields")
    }

    @Test
    fun `error if no team`() {
        val manifest = Manifest()
        val linter = RequiredFieldsLinter()
        val result = linter.lint(manifest)

        assertErrorMessage(result, "Required top level field 'team' missing")
    }

    @Test
    fun `error if no tasks`() {
        val manifest = Manifest(team = "yolo")
        val linter = RequiredFieldsLinter()
        val result = linter.lint(manifest)

        assertErrorMessage(result, "Tasks is empty...")
    }

    @Test
    fun `error if no repo`() {
        val manifest = Manifest()
        val linter = RequiredFieldsLinter()
        val result = linter.lint(manifest)

        assertErrorMessage(result, "Required top level field 'repo' missing")
    }

    @Test
    fun `no errors if all is ok`() {
        val manifest = Manifest(
                team = "yolo",
                repo = Repo("git@..."),
                tasks = listOf(Run()))

        val linter = RequiredFieldsLinter()
        val result = linter.lint(manifest)

        assertThat(result.errors).isEmpty()
    }

}