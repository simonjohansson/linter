package lint.linters

import com.google.common.truth.Truth.assertThat
import model.Build
import model.Error
import model.Manifest
import model.Result
import org.junit.Test

class RequiredFieldsLinterTest {

    @Test
    fun `it has the corrent name`() {
        val linter = RequiredFieldsLinter()

        assertThat(linter.name()).isEqualTo("Required Fields")
    }

    @Test
    fun `error if no org`() {
        val manifest = Manifest()
        val linter = RequiredFieldsLinter()
        val result = linter.lint(manifest)

        assertErrorMessageInResults(result, "Required top level field 'org' missing")
    }

    @Test
    fun `error if no tasks`() {
        val manifest = Manifest(org = "yolo")
        val linter = RequiredFieldsLinter()
        val result = linter.lint(manifest)
        assertErrorMessageInResults(result, "Tasks is empty...")
    }

    @Test
    fun `error if no repo`() {
        val manifest = Manifest()
        val linter = RequiredFieldsLinter()
        val result = linter.lint(manifest)

        assertErrorMessageInResults(result, "Required top level field 'repo' missing")
    }

    @Test
    fun `no errors if all is ok`() {
        val manifest = Manifest(org = "yolo", tasks = listOf(Build()), repo = "git@...")
        val linter = RequiredFieldsLinter()
        val result = linter.lint(manifest)

        assertThat(result.errors).isEmpty()
    }

}