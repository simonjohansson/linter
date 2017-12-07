package parser

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import model.manifest.*
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import reader.IReader


class ParserTest {
    lateinit var parser: Parser
    lateinit var reader: IReader

    @Before
    fun setup() {
        reader = mock(IReader::class.java)
        parser = Parser(reader)
    }

    @Test
    fun `when manifest doesnt exist it should return Empty`() {
        val path = ".ci.yml"
        given(reader.fileExists(path)).willReturn(false)
        val manifest = parser.parseManifest()
        assertThat(manifest.isPresent).isFalse()
    }

    @Test
    fun `when manifest exist it should return it`() {
        val path = ".ci.yml"
        given(reader.fileExists(path)).willReturn(true)
        given(reader.readFile(path)).willReturn("""
                        org: yolo
                        repo:
                          uri: asd
                          private_key: |
                            I AM
                            SO PRIVATE
                        tasks:
                            - task: run
                              command: test.sh
                            - task: run
                              command: build.sh
                            - task: deploy
                              env: live
                              vars:
                                name: value
                                secret: ((kehe))
                            - task: docker
                              email: asd
                              username: asd
                              password: asd
                              repository: asd/asd
                        """)
        val manifest = parser.parseManifest()
        assertThat(manifest.isPresent).isTrue()
        assertThat(manifest.get()).isEqualTo(
                Manifest(
                        org = "yolo",
                        repo = Repo("asd", "I AM\nSO PRIVATE\n"),
                        tasks = listOf(
                                Run(command = "test.sh"),
                                Run(command = "build.sh"),
                                Deploy(
                                        vars = mapOf(
                                                "name" to "value",
                                                "secret" to "((kehe))"
                                        )
                                ),
                                Docker(
                                        email = "asd",
                                        username = "asd",
                                        password = "asd",
                                        repository = "asd/asd"
                                )
                        )
                )
        )
    }

    @Test
    fun `when manifest have a bad task it should throw`() {
        val path = ".ci.yml"
        given(reader.fileExists(path)).willReturn(true)
        given(reader.readFile(path)).willReturn("""
                        org: yolo
                        tasks:
                            - task: run
                              command: build.sh
                            - task: ThEfUcK
                              image: python:3.6
                        """)

        try {
            parser.parseManifest()
            fail("method should throw")
        } catch (e: NotImplementedError) {
            assertThat(e).hasMessageThat().isEqualTo("I don't know how to deal with task 'ThEfUcK'")
        }
    }

}