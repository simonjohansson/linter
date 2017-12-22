team: engineering-enablement

repo:
  uri: https://github.com/simonjohansson/linter.git

tasks:
- task: run
  script: ./test.sh
  image: openjdk:8-slim
- task: docker
  username: ((docker.username))
  password: ((docker.password))
  repository: simonjohansson/half-pipe-linter
