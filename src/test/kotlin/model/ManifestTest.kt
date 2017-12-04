package model

import com.google.common.truth.Truth.assertThat
import model.manifest.Manifest
import org.junit.Assert.fail
import org.junit.Test

class ManifestTest {

    @Test
    fun `getRepoName should throw for empty repo`() {
        try {
            Manifest().getRepoName()
            fail("Should not get here")
        } catch (e: RuntimeException) {

        }
    }

    @Test
    fun `getRepoName should throw when it doesnt look like a get repo`() {
        try {
            Manifest(repo = "asds").getRepoName()
            fail("Should not get here")
        } catch (e: RuntimeException) {

        }
    }

    @Test
    fun `getRepoName should correctly parse out repo name`() {
        val html = Manifest(repo = "https://github.com/myorg/my-cool_repo.git").getRepoName()
        val ssh = Manifest(repo = "git@github.com:myorg/my-cool_repo.git").getRepoName()

        assertThat(html).isEqualTo("my-cool_repo")
        assertThat(ssh).isEqualTo("my-cool_repo")
    }
}