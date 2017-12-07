package build

import com.google.common.truth.Truth.assertThat
import model.manifest.*
import org.junit.Before
import org.junit.Test

class ConcoursePipelineBuilderTest {

    lateinit var subject: ConcoursePipelineBuilder

    @Before
    fun setup() {
        subject = ConcoursePipelineBuilder()
    }

    @Test
    fun `Should render correct resources for a manifest`() {
        val repoName = "my-cool-repo"
        val uri = "https://github.com/org/$repoName.git"
        val manifest = Manifest(repo = Repo(uri))
        val wanted = """|---
                        |resources:
                        |- name: $repoName
                        |  type: git
                        |  source:
                        |    uri: $uri
                        |jobs:
                        |- serial: true
                        |
                        """.trimMargin()

        assertThat(subject.build(manifest)).isEqualTo(wanted)
    }

    @Test
    fun `should render one job correctly`() {
        val repoName = "my-cool-repo"
        val uri = "https://github.com/org/$repoName.git"
        val manifest = Manifest(
                repo = Repo(uri),
                tasks = listOf(Run(command = "test.sh", image = "yolo"))
        )

        val wanted = """|---
                        |resources:
                        |- name: ${manifest.getRepoName()}
                        |  type: git
                        |  source:
                        |    uri: ${manifest.repo.uri}
                        |jobs:
                        |- name: ${(manifest.tasks[0] as Run).name()}
                        |  serial: true
                        |  plan:
                        |  - get: ${manifest.getRepoName()}
                        |    trigger: true
                        |  - task: ${(manifest.tasks[0] as Run).command}
                        |    config:
                        |      platform: linux
                        |      image_resource:
                        |        type: docker-image
                        |        source:
                        |          repository: ${(manifest.tasks[0] as Run).image}
                        |          tag: latest
                        |      run:
                        |        path: ./${(manifest.tasks[0] as Run).command}
                        |        dir: ${manifest.getRepoName()}
                        |      inputs:
                        |      - name: ${manifest.getRepoName()}
                        |
                        """.trimMargin()



        assertThat(subject.build(manifest)).isEqualTo(wanted)
    }

    @Test
    fun `should render two jobs correctly`() {
        val repoName = "my-cool-repo"
        val uri = "https://github.com/org/$repoName.git"

        val manifest = Manifest(
                repo = Repo(uri),
                tasks = listOf(
                        Run(command = "test.sh", image = "yolo"),
                        Run(command = "ci/build.sh", image = "kehe"),
                        Run(command = "./other-path/build.sh", image = "kehe", vars = mapOf("KEY" to "value"))
                )
        )

        val wanted = """|---
                        |resources:
                        |- name: $repoName
                        |  type: git
                        |  source:
                        |    uri: $uri
                        |jobs:
                        |- name: ${(manifest.tasks[0] as Run).name()}
                        |  serial: true
                        |  plan:
                        |  - get: ${manifest.getRepoName()}
                        |    trigger: true
                        |  - task: ${(manifest.tasks[0] as Run).command}
                        |    config:
                        |      platform: linux
                        |      image_resource:
                        |        type: docker-image
                        |        source:
                        |          repository: ${(manifest.tasks[0] as Run).image}
                        |          tag: latest
                        |      run:
                        |        path: ./${(manifest.tasks[0] as Run).command}
                        |        dir: ${manifest.getRepoName()}
                        |      inputs:
                        |      - name: ${manifest.getRepoName()}
                        |- name: ${(manifest.tasks[1] as Run).name()}
                        |  serial: true
                        |  plan:
                        |  - get: ${manifest.getRepoName()}
                        |    trigger: true
                        |    passed:
                        |    - ${(manifest.tasks[0] as Run).command}
                        |  - task: ${(manifest.tasks[1] as Run).name()}
                        |    config:
                        |      platform: linux
                        |      image_resource:
                        |        type: docker-image
                        |        source:
                        |          repository: ${(manifest.tasks[1] as Run).image}
                        |          tag: latest
                        |      run:
                        |        path: ./${(manifest.tasks[1] as Run).command}
                        |        dir: ${manifest.getRepoName()}
                        |      inputs:
                        |      - name: ${manifest.getRepoName()}
                        |- name: ./other-path/build.sh
                        |  serial: true
                        |  plan:
                        |  - get: my-cool-repo
                        |    trigger: true
                        |    passed:
                        |    - ci/build.sh
                        |  - task: ./other-path/build.sh
                        |    config:
                        |      platform: linux
                        |      image_resource:
                        |        type: docker-image
                        |        source:
                        |          repository: kehe
                        |          tag: latest
                        |      params:
                        |        KEY: value
                        |      run:
                        |        path: ./other-path/build.sh
                        |        dir: my-cool-repo
                        |      inputs:
                        |      - name: my-cool-repo
                        |
                        """.trimMargin()

        assertThat(subject.build(manifest)).isEqualTo(wanted)
    }

    @Test
    fun `should render a deploy job properly`() {
        val repoName = "my-cool-repo"
        val uri = "https://github.com/org/$repoName.git"


        val manifest = Manifest(
                repo = Repo(uri),
                org = "myOrg",
                tasks = listOf(
                        Run("./test.sh", "busybox:yolo"),
                        Deploy(
                                manifest = "ci/manifest.yml",
                                api = "api",
                                username = "username",
                                password = "password",
                                organization = "organization",
                                space = "space"
                        )
                )
        )

        val wanted = """|---
                        |resources:
                        |- name: $repoName
                        |  type: git
                        |  source:
                        |    uri: $uri
                        |- name: ${(manifest.tasks[1] as Deploy).name()}
                        |  type: cf
                        |  source:
                        |    api: ${(manifest.tasks.last() as Deploy).api}
                        |    username: ${(manifest.tasks.last() as Deploy).username}
                        |    password: ${(manifest.tasks.last() as Deploy).password}
                        |    organization: ${(manifest.tasks.last() as Deploy).organization}
                        |    space: ${(manifest.tasks.last() as Deploy).space}
                        |    skip_cert_check: false
                        |jobs:
                        |- name: ${(manifest.tasks[0] as Run).name()}
                        |  serial: true
                        |  plan:
                        |  - get: ${manifest.getRepoName()}
                        |    trigger: true
                        |  - task: ${(manifest.tasks[0] as Run).name()}
                        |    config:
                        |      platform: linux
                        |      image_resource:
                        |        type: docker-image
                        |        source:
                        |          repository: busybox
                        |          tag: yolo
                        |      run:
                        |        path: ${(manifest.tasks[0] as Run).command}
                        |        dir: ${manifest.getRepoName()}
                        |      inputs:
                        |      - name: ${manifest.getRepoName()}
                        |- name: ${(manifest.tasks[1] as Deploy).name()}
                        |  serial: true
                        |  plan:
                        |  - get: $repoName
                        |    trigger: true
                        |    passed:
                        |    - ./test.sh
                        |  - put: ${(manifest.tasks[1] as Deploy).name()}
                        |    params:
                        |      path: my-cool-repo
                        |      manifest: my-cool-repo/ci/manifest.yml
                        |
                        """.trimMargin()

        assertThat(subject.build(manifest)).isEqualTo(wanted)
    }

    @Test
    fun `should render two deploy jobs and a docker push properly`() {
        val repoName = "my-cool-repo"
        val uri = "https://github.com/org/$repoName.git"

        val deploy1 = Deploy(
                api = "api1",
                username = "username1",
                password = "password1",
                organization = "organization1",
                space = "space1"
        )

        val deploy2 = Deploy(
                api = "api2",
                username = "username2",
                password = "password2",
                organization = "organization2",
                space = "space2",
                vars = mapOf("SIMON" to "Johansson")

        )

        val docker = Docker(
                email = "asd",
                username = "asd",
                password = "asd",
                repository = "asd/asd"
        )

        val manifest = Manifest(
                repo = Repo(uri),
                org = "myOrg",
                tasks = listOf(
                        Run("./test.sh", "busybox"),
                        deploy1,
                        Run("./integration-tests.sh", "busybox"),
                        deploy2,
                        docker

                )
        )

        val wanted = """|---
                        |resources:
                        |- name: $repoName
                        |  type: git
                        |  source:
                        |    uri: $uri
                        |- name: ${(manifest.tasks[1] as Deploy).name()}
                        |  type: cf
                        |  source:
                        |    api: ${(manifest.tasks[1] as Deploy).api}
                        |    username: ${(manifest.tasks[1] as Deploy).username}
                        |    password: ${(manifest.tasks[1] as Deploy).password}
                        |    organization: ${(manifest.tasks[1] as Deploy).organization}
                        |    space: ${(manifest.tasks[1] as Deploy).space}
                        |    skip_cert_check: false
                        |- name: ${(manifest.tasks[3] as Deploy).name()}
                        |  type: cf
                        |  source:
                        |    api: ${(manifest.tasks[3] as Deploy).api}
                        |    username: ${(manifest.tasks[3] as Deploy).username}
                        |    password: ${(manifest.tasks[3] as Deploy).password}
                        |    organization: ${(manifest.tasks[3] as Deploy).organization}
                        |    space: ${(manifest.tasks[3] as Deploy).space}
                        |    skip_cert_check: false
                        |- name: docker-push
                        |  type: docker-image
                        |  source:
                        |    email: asd
                        |    username: asd
                        |    password: asd
                        |    repository: asd/asd
                        |jobs:
                        |- name: ${(manifest.tasks[0] as Run).name()}
                        |  serial: true
                        |  plan:
                        |  - get: ${manifest.getRepoName()}
                        |    trigger: true
                        |  - task: ${(manifest.tasks[0] as Run).name()}
                        |    config:
                        |      platform: linux
                        |      image_resource:
                        |        type: docker-image
                        |        source:
                        |          repository: ${(manifest.tasks[0] as Run).image}
                        |          tag: latest
                        |      run:
                        |        path: ${(manifest.tasks[0] as Run).command}
                        |        dir: ${manifest.getRepoName()}
                        |      inputs:
                        |      - name: ${manifest.getRepoName()}
                        |- name: ${(manifest.tasks[1] as Deploy).name()}
                        |  serial: true
                        |  plan:
                        |  - get: $repoName
                        |    trigger: true
                        |    passed:
                        |    - ./test.sh
                        |  - put: ${(manifest.tasks[1] as Deploy).name()}
                        |    params:
                        |      path: my-cool-repo
                        |      manifest: my-cool-repo/manifest.yml
                        |- name: ${(manifest.tasks[2] as Run).name()}
                        |  serial: true
                        |  plan:
                        |  - get: ${manifest.getRepoName()}
                        |    trigger: true
                        |    passed:
                        |    - ${(manifest.tasks[1] as Deploy).name()}
                        |  - task: ${(manifest.tasks[2] as Run).name()}
                        |    config:
                        |      platform: linux
                        |      image_resource:
                        |        type: docker-image
                        |        source:
                        |          repository: ${(manifest.tasks[0] as Run).image}
                        |          tag: latest
                        |      run:
                        |        path: ${(manifest.tasks[2] as Run).command}
                        |        dir: ${manifest.getRepoName()}
                        |      inputs:
                        |      - name: ${manifest.getRepoName()}
                        |- name: ${(manifest.tasks[3] as Deploy).name()}
                        |  serial: true
                        |  plan:
                        |  - get: ${manifest.getRepoName()}
                        |    trigger: true
                        |    passed:
                        |    - ${(manifest.tasks[2] as Run).name()}
                        |  - put: ${(manifest.tasks[3] as Deploy).name()}
                        |    params:
                        |      path: ${manifest.getRepoName()}
                        |      manifest: ${manifest.getRepoName()}/manifest.yml
                        |      environment_variables:
                        |        SIMON: Johansson
                        |- name: docker-push
                        |  serial: true
                        |  plan:
                        |  - get: my-cool-repo
                        |    trigger: true
                        |    passed:
                        |    - deploy-organization2-space2
                        |  - put: docker-push
                        |    params:
                        |      build: ${manifest.getRepoName()}

                        """.trimMargin()

        assertThat(subject.build(manifest)).isEqualTo(wanted)
    }

}