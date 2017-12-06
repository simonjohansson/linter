package model

import com.google.common.truth.Truth.assertThat
import model.manifest.Manifest
import model.manifest.Repo
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
            Manifest(repo = Repo("asds")).getRepoName()
            fail("Should not get here")
        } catch (e: RuntimeException) {

        }
    }

    @Test
    fun `getRepoName should correctly parse out repo name`() {
        val html = Manifest(repo = Repo("https://github.com/myorg/my-cool_repo.git")).getRepoName()
        val ssh = Manifest(repo = Repo("git@github.com:myorg/my-cool_repo.git")).getRepoName()

        assertThat(html).isEqualTo("my-cool_repo")
        assertThat(ssh).isEqualTo("my-cool_repo")
    }
}