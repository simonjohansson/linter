package secrets

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.junit.Test

class SecretsTest {


    @Test
    fun `Should return false if no credentials used to create object`() {
        assertThat(Secrets(vaultToken = "").haveToken()).isFalse()
    }

    @Test
    fun `Should throw if empty credentials`() {
        try {
            Secrets(vaultToken = "").exists("pe", "test-repo", "simon")
            fail("Should not get here")
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("You must create object with real vault token!")
        }
    }

    @Test
    fun `asd`() {
        fail("We need more tests")
    }
}