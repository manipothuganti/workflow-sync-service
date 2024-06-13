String basepath = 'NLCD/workflow-sync-service'

String giturl = 'git@github.com:discoveryinc-cs/workflow-sync-service.git'

pipelineJob("$basepath/1.Build-Dev-Qa") {
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
                scriptPath('pipeline/pipeline_dev_qa.groovy')
            }
        }
    }
}

pipelineJob("$basepath/2.Deploy-UAT-Prod") {
    logRotator {
        numToKeep(5)
        artifactNumToKeep(5)
    }
    throttleConcurrentBuilds {
        maxTotal(1)
    }

    parameters {
        stringParam('APPLICATION_VERSION', '')
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote { url(giturl) }
                    branches('master')
                    extensions {}
                }
                scriptPath('pipeline/pipeline_uat_prod.groovy')
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
                    branches('master')
                    extensions {}
                }
                scriptPath('pipeline/pipeline_env_test.groovy')
            }
        }
    }
}

