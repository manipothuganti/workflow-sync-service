String basepath = 'NLCD/workflow-sync-service'

String giturl = 'git@github.com:discoveryinc-cs/workflow-sync-service.git'

pipelineJob("$basepath/01.Build") {
    properties {
        pipelineTriggers {
            triggers {
                pollSCM { scmpoll_spec('H/10 * * * *') }
            }
        }
    }

    logRotator {
        numToKeep(10)
        artifactNumToKeep(10)
    }
    parameters {
        stringParam('USE_GIT_BRANCH', 'master')
    }
    throttleConcurrentBuilds {
        maxTotal(1)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(giturl) }
                    branches('master')
                    extensions {}
                }
                scriptPath('pipeline/01_commit.groovy')
            }
        }
    }
}

pipelineJob("$basepath/02.Deploy") {
    logRotator {
        numToKeep(20)
        artifactNumToKeep(20)
    }
    parameters {
        stringParam('USE_GIT_BRANCH', 'master')
        stringParam('APPLICATION_VERSION', '')
        stringParam('CHART_VERSION', '')
    }
    throttleConcurrentBuilds {
        maxTotal(1)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(giturl) }
                    branches('master')
                    extensions {}
                }
                scriptPath('pipeline/02_nonprod.groovy')
            }
        }
    }
}

pipelineJob("$basepath/03.Production") {
    logRotator {
        numToKeep(10)
        artifactNumToKeep(10)
    }
    parameters {
        stringParam('APPLICATION_VERSION', '')
        stringParam('CHART_VERSION', '')
        booleanParam('DEPLOY', false)
    }
    throttleConcurrentBuilds {
        maxTotal(1)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(giturl) }
                    branches('master')
                    extensions {}
                }
                scriptPath('pipeline/03_prod.groovy')
            }
        }
    }
}

pipelineJob("$basepath/Environment-Test") {
    logRotator {
        numToKeep(10)
        artifactNumToKeep(10)
    }
    parameters {
        stringParam('TARGET_ENV', '', 'The environment name to provide to the tests')
        stringParam('TARGET_BASEURL', '', 'The base URL to provide to the tests')
        stringParam('TEST_CATEGORIES', 'SmokeTests')
        stringParam('USE_GIT_BRANCH', 'master', 'The branch name to run the tests from')
    }
    throttleConcurrentBuilds {
        maxTotal(1)
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote { url(giturl) }
                    branch('master')
                }
                scriptPath('pipeline/pipeline_env_test.groovy')
            }
        }
    }
}
