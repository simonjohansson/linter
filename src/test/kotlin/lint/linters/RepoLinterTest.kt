package lint.linters

import com.google.common.truth.Truth.assertThat
import model.Error
import model.Manifest
import org.junit.Test

class RepoLinterTest {

    val repoLinter = RepoLinter()

    @Test
    fun `it has the correct name`() {
        assertThat(repoLinter.name()).isEqualTo("Repo")
    }

    @Test
    fun `it fails if repo does not look a real repo`() {
        val repo = "asddasds"
        val manifest = Manifest(repo = repo)
        val result = repoLinter.lint(manifest)

        assertThat(
                model.Error("'$repo' does not look like a real repo!", Error.Type.BAD_VALUE)
        ).isIn(result.errors)
    }

    @Test
    fun `it works if repo is a real git repo`() {
        val repo = "https://github.com/springernature/yolo.git"
        val manifest = Manifest(repo = repo)
        val result = repoLinter.lint(manifest)

        assertThat(result.errors).isEmpty()
    }
}