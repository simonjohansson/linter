package parser

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import model.manifest.*
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import reader.IReader


class ParserTest {
    lateinit var subject: Parser
    lateinit var reader: IReader

    val path = ".halfpipe.io"

    @Before
    fun setup() {
        reader = mock(IReader::class.java)
        subject = Parser(reader)
    }

    @Test
    fun `when manifest doesnt exist it should return Empty`() {
        given(reader.fileExists(path)).willReturn(false)
        val manifest = subject.parseManifest()
        assertThat(manifest.isPresent).isFalse()
    }

    @Test
    fun `when manifest exist it should return it`() {
        given(reader.fileExists(path)).willReturn(true)
        given(reader.readFile(path)).willReturn("""
                        team: yolo
                        repo:
                          uri: asd
                          private_key: |
                            I AM
                            SO PRIVATE
                        tasks:
                            - task: run
                              script: test.sh
                            - task: run
                              script: build.sh
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
        val manifest = subject.parseManifest()
        assertThat(manifest.isPresent).isTrue()
        assertThat(manifest.get()).isEqualTo(
                Manifest(
                        team = "yolo",
                        repo = Repo("asd", "I AM\nSO PRIVATE\n"),
                        tasks = listOf(
                                Run(script = "test.sh"),
                                Run(script = "build.sh"),
                                Deploy(
                                        vars = mapOf(
                                                "name" to "value",
                                                "secret" to "((kehe))"
                                        )
                                ),
                                Docker(
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
        given(reader.fileExists(path)).willReturn(true)
        given(reader.readFile(path)).willReturn("""
                        team: yolo
                        tasks:
                            - task: run
                              command: build.sh
                            - task: ThEfUcK
                              image: python:3.6
                        """)

        try {
            subject.parseManifest()
            fail("method should throw")
        } catch (e: NotImplementedError) {
            assertThat(e).hasMessageThat().isEqualTo("I don't know how to deal with task 'ThEfUcK'")
        }
    }

    @Test
    fun `when repo uri is set it doesnt call out to filesystem to check for URI`() {
        given(reader.fileExists(path)).willReturn(true)
        given(reader.readFile(path)).willReturn("""
                            team: myOrg
                            repo:
                                uri: uri
                        """)

        val manifest = subject.parseManifest()
        assertThat(manifest.get()).isEqualTo(Manifest(team = "myOrg", repo = Repo(uri = "uri")))
        verify(reader, Mockito.times(0)).fileExists(".git/config")
        verify(reader, Mockito.times(0)).readFile(".git/config")
    }

    @Test
    fun `when no repo uri is set it calls out to filesystem to check for URI`() {
        given(reader.fileExists(path)).willReturn(true)
        given(reader.fileExists(".git/config")).willReturn(true)
        given(reader.readFile(path)).willReturn("""
                            team: myOrg
                        """)
        given(reader.readFile(".git/config")).willReturn("""
            [core]
                repositoryformatversion = 0
                filemode = true
                bare = false
                logallrefupdates = true
                ignorecase = true
                precomposeunicode = true
            [remote "origin"]
                url = git@github.com:simonjohansson/linter.git
                fetch = +refs/heads/*:refs/remotes/origin/*
            [branch "master"]
                remote = origin
                merge = refs/heads/master
            """
        )

        val manifest = subject.parseManifest()
        assertThat(manifest.get()).isEqualTo(Manifest(team = "myOrg", repo = Repo(uri = "git@github.com:simonjohansson/linter.git")))
    }

    @Test
    fun `when no repo uri but private_key is set it calls out to filesystem to check for URI and keeps private_key`() {
        given(reader.fileExists(path)).willReturn(true)
        given(reader.fileExists(".git/config")).willReturn(true)
        given(reader.readFile(path)).willReturn("""
                            team: myOrg
                            repo:
                              private_key: yolo
                        """)
        given(reader.readFile(".git/config")).willReturn("""
            [core]
                repositoryformatversion = 0
                filemode = true
                bare = false
                logallrefupdates = true
                ignorecase = true
                precomposeunicode = true
            [remote "origin"]
                url = git@github.com:simonjohansson/linter.git
                fetch = +refs/heads/*:refs/remotes/origin/*
            [branch "master"]
                remote = origin
                merge = refs/heads/master
            """
        )

        val manifest = subject.parseManifest()
        assertThat(manifest.get()).isEqualTo(Manifest(team = "myOrg", repo = Repo(
                uri = "git@github.com:simonjohansson/linter.git",
                private_key = "yolo"
        )))
    }

    @Test
    fun `when no repo uri is set and git config doesnt have a url it does nothing`() {
        given(reader.fileExists(path)).willReturn(true)
        given(reader.fileExists(".git/config")).willReturn(true)
        given(reader.readFile(path)).willReturn("""
                            team: myOrg
                        """)
        given(reader.readFile(".git/config")).willReturn("""
            [core]
                repositoryformatversion = 0
                filemode = true
                bare = false
                logallrefupdates = true
                ignorecase = true
                precomposeunicode = true
            [remote "origin"]
                fetch = +refs/heads/*:refs/remotes/origin/*
            [branch "master"]
                remote = origin
                merge = refs/heads/master
            """
        )

        val manifest = subject.parseManifest()
        assertThat(manifest.get()).isEqualTo(Manifest(team = "myOrg"))
    }

}