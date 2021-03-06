package lint.linters

import com.google.common.truth.Truth.assertThat
import model.manifest.Docker
import org.junit.Before
import org.junit.Test

class DockerLinterTest {

    lateinit var subject: DockerLinter

    @Before
    fun setup() {
        subject = DockerLinter()
    }

    @Test
    fun `Adds errors for missing fields`() {
        val docker0 = Docker()
        assertThat(subject.lint(docker0).errors).hasSize(1)

        val docker2 = Docker("b")
        assertThat(subject.lint(docker2).errors).hasSize(1)

        val docker4 = Docker("b", "c", "d")
        assertThat(subject.lint(docker4).errors).hasSize(0)

    }
}