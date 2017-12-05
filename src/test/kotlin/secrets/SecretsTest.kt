package secrets

import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultException
import com.bettercloud.vault.api.Logical
import com.bettercloud.vault.response.LogicalResponse
import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock

class SecretsTest {


    lateinit var vault: Vault
    lateinit var subject: Secrets

    @Before
    fun setup() {
        vault = mock(Vault::class.java)
        subject = Secrets(vaultToken = "asdf", vault = vault)
    }


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
    fun `Should return false when map not found`() {
        val org = "org"
        val repoName = "repoName"
        val secret_key = "((secret.key))"
        val vaultPath = "/concourse/$org/$repoName/secret"

        val logical = mock(Logical::class.java)
        given(vault.logical()).willReturn(logical)
        given(logical.read(vaultPath)).willThrow(VaultException::class.java)

        assertThat(subject.exists(org, repoName, secret_key)).isFalse()
    }

    @Test
    fun `Should return false when secret in map not found`() {
        val org = "org"
        val repoName = "repoName"
        val secret_key = "((secret.key))"
        val vaultPath = "/concourse/$org/$repoName/secret"

        val logical = mock(Logical::class.java)
        given(vault.logical()).willReturn(logical)

        val logicalResponse = mock(LogicalResponse::class.java)
        given(logical.read(vaultPath)).willReturn(logicalResponse)
        given(logicalResponse.data).willReturn(mapOf("yolo" to "kolo"))

        assertThat(subject.exists(org, repoName, secret_key)).isFalse()
    }

    @Test
    fun `Should return true when secret in map found`() {
        val org = "org"
        val repoName = "repoName"
        val secret_key = "((secret.key))"
        val vaultPath = "/concourse/$org/$repoName/secret"

        val logical = mock(Logical::class.java)
        given(vault.logical()).willReturn(logical)

        val logicalResponse = mock(LogicalResponse::class.java)
        given(logical.read(vaultPath)).willReturn(logicalResponse)
        given(logicalResponse.data).willReturn(mapOf("key" to "I exist!"))

        assertThat(subject.exists(org, repoName, secret_key)).isTrue()
    }
}