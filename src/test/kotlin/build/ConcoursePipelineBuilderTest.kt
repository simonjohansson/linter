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
                tasks = listOf(Run(script = "test.sh", image = "yolo"))
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
                        |  - task: ${(manifest.tasks[0] as Run).name()}
                        |    privileged: true
                        |    config:
                        |      platform: linux
                        |      image_resource:
                        |        type: docker-image
                        |        source:
                        |          repository: ${(manifest.tasks[0] as Run).image}
                        |          tag: latest
                        |      run:
                        |        path: /bin/sh
                        |        args:
                        |        - -exc
                        |        - ./${(manifest.tasks[0] as Run).name()}
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
                        Run(script = "test.sh", image = "yolo"),
                        Run(script = "ci/build.sh", image = "kehe"),
                        Run(script = "./other-path/build.sh", image = "kehe", vars = mapOf("KEY" to "value"))
                )
        )

        val wanted = """|---
            |resources:
            |- name: my-cool-repo
            |  type: git
            |  source:
            |    uri: https://github.com/org/my-cool-repo.git
            |jobs:
            |- name: test.sh
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |  - task: test.sh
            |    privileged: true
            |    config:
            |      platform: linux
            |      image_resource:
            |        type: docker-image
            |        source:
            |          repository: yolo
            |          tag: latest
            |      run:
            |        path: /bin/sh
            |        args:
            |        - -exc
            |        - ./test.sh
            |        dir: my-cool-repo
            |      inputs:
            |      - name: my-cool-repo
            |- name: ci.build.sh
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |    passed:
            |    - test.sh
            |  - task: ci.build.sh
            |    privileged: true
            |    config:
            |      platform: linux
            |      image_resource:
            |        type: docker-image
            |        source:
            |          repository: kehe
            |          tag: latest
            |      run:
            |        path: /bin/sh
            |        args:
            |        - -exc
            |        - ./ci/build.sh
            |        dir: my-cool-repo
            |      inputs:
            |      - name: my-cool-repo
            |- name: ..other-path.build.sh
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |    passed:
            |    - ci.build.sh
            |  - task: ..other-path.build.sh
            |    privileged: true
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
            |        path: /bin/sh
            |        args:
            |        - -exc
            |        - ./other-path/build.sh
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
                team = "myOrg",
                tasks = listOf(
                        Run("./test.sh", "busybox:yolo"),
                        Deploy(
                                api = "api",
                                space = "space",
                                manifest = "ci/manifest.yml"
                        )
                )
        )

        val wanted = """|---
            |resources:
            |- name: my-cool-repo
            |  type: git
            |  source:
            |    uri: https://github.com/org/my-cool-repo.git
            |jobs:
            |- name: ..test.sh
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |  - task: ..test.sh
            |    privileged: true
            |    config:
            |      platform: linux
            |      image_resource:
            |        type: docker-image
            |        source:
            |          repository: busybox
            |          tag: yolo
            |      run:
            |        path: /bin/sh
            |        args:
            |        - -exc
            |        - ./test.sh
            |        dir: my-cool-repo
            |      inputs:
            |      - name: my-cool-repo
            |- name: deploy-space
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |    passed:
            |    - ..test.sh
            |  - task: 'Deploy to myOrg: space'
            |    privileged: true
            |    config:
            |      platform: linux
            |      image_resource:
            |        type: docker-image
            |        source:
            |          repository: simonjohansson/cf-push
            |          tag: latest
            |      params:
            |        API: api
            |        USERNAME: ((cf-credentials.username))
            |        PASSWORD: ((cf-credentials.password))
            |        ORG: myOrg
            |        SPACE: space
            |        MANIFEST_PATH: ci/manifest.yml
            |      run:
            |        path: /bin/sh
            |        args:
            |        - -exc
            |        - cf-push
            |        dir: my-cool-repo
            |      inputs:
            |      - name: my-cool-repo
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
                space = "space1"
        )

        val deploy2 = Deploy(
                api = "api2",
                space = "space2",
                vars = mapOf("SIMON" to "Johansson")

        )

        val docker = Docker(
                username = "asd",
                password = "asd",
                repository = "asd/asd"
        )

        val manifest = Manifest(
                repo = Repo(uri),
                team = "myOrg",
                tasks = listOf(
                        Run("./test.sh", "busybox"),
                        deploy1,
                        Run("./integration-tests.sh", "busybox"),
                        deploy2,
                        docker

                )
        )

        val wanted = """
            |---
            |resources:
            |- name: my-cool-repo
            |  type: git
            |  source:
            |    uri: https://github.com/org/my-cool-repo.git
            |- name: docker-push
            |  type: docker-image
            |  source:
            |    username: asd
            |    password: asd
            |    repository: asd/asd
            |jobs:
            |- name: ..test.sh
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |  - task: ..test.sh
            |    privileged: true
            |    config:
            |      platform: linux
            |      image_resource:
            |        type: docker-image
            |        source:
            |          repository: busybox
            |          tag: latest
            |      run:
            |        path: /bin/sh
            |        args:
            |        - -exc
            |        - ./test.sh
            |        dir: my-cool-repo
            |      inputs:
            |      - name: my-cool-repo
            |- name: deploy-space1
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |    passed:
            |    - ..test.sh
            |  - task: 'Deploy to myOrg: space1'
            |    privileged: true
            |    config:
            |      platform: linux
            |      image_resource:
            |        type: docker-image
            |        source:
            |          repository: simonjohansson/cf-push
            |          tag: latest
            |      params:
            |        API: api1
            |        USERNAME: ((cf-credentials.username))
            |        PASSWORD: ((cf-credentials.password))
            |        ORG: myOrg
            |        SPACE: space1
            |        MANIFEST_PATH: manifest.yml
            |      run:
            |        path: /bin/sh
            |        args:
            |        - -exc
            |        - cf-push
            |        dir: my-cool-repo
            |      inputs:
            |      - name: my-cool-repo
            |- name: ..integration-tests.sh
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |    passed:
            |    - deploy-space1
            |  - task: ..integration-tests.sh
            |    privileged: true
            |    config:
            |      platform: linux
            |      image_resource:
            |        type: docker-image
            |        source:
            |          repository: busybox
            |          tag: latest
            |      run:
            |        path: /bin/sh
            |        args:
            |        - -exc
            |        - ./integration-tests.sh
            |        dir: my-cool-repo
            |      inputs:
            |      - name: my-cool-repo
            |- name: deploy-space2
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |    passed:
            |    - ..integration-tests.sh
            |  - task: 'Deploy to myOrg: space2'
            |    privileged: true
            |    config:
            |      platform: linux
            |      image_resource:
            |        type: docker-image
            |        source:
            |          repository: simonjohansson/cf-push
            |          tag: latest
            |      params:
            |        SIMON: Johansson
            |        API: api2
            |        USERNAME: ((cf-credentials.username))
            |        PASSWORD: ((cf-credentials.password))
            |        ORG: myOrg
            |        SPACE: space2
            |        MANIFEST_PATH: manifest.yml
            |      run:
            |        path: /bin/sh
            |        args:
            |        - -exc
            |        - cf-push
            |        dir: my-cool-repo
            |      inputs:
            |      - name: my-cool-repo
            |- name: docker-push
            |  serial: true
            |  plan:
            |  - get: my-cool-repo
            |    trigger: true
            |    passed:
            |    - deploy-space2
            |  - put: docker-push
            |    params:
            |      build: my-cool-repo
            |
                        """.trimMargin()

        assertThat(subject.build(manifest)).isEqualTo(wanted)
    }

}