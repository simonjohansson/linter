package lint.linters

import com.google.common.truth.Truth.assertThat
import model.manifest.Docker
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import reader.IReader

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

        val docker2 = Docker("a", "b")
        assertThat(subject.lint(docker2).errors).hasSize(1)

        val docker4 = Docker("a", "b", "c", "d")
        assertThat(subject.lint(docker4).errors).hasSize(0)

    }
}