@Library('jpl@1.4.4')
import com.discovery.jpl.PipelineSupport

String giturl = 'git@github.com:discoveryinc-cs/workflow-sync-service.git'
String application = 'workflow-sync-service'
String owner = 'dl-turbo@discovery.com'
String chartname = 'chart-workflow-sync-service'
String namespace = 'distribute'
String liquibaseVaultDBName = 'planning'

def common = new PipelineSupport()

node('eks-cluster') {
    container('eks-cluster') {

        stage('Deploy-dev') {
            sh 'rm -rf *'

            env.GIT_BRANCH = USE_GIT_BRANCH
            env.APPLICATION_VERSION = APPLICATION_VERSION
            env.CHART_VERSION = CHART_VERSION
            env.JPL_DOCKER_ENV = "prod"
            env.JPL_KUBECONFIG_ACCOUNT = 'cs-nonprod'

            if (!env.APPLICATION_VERSION) {
                error 'No version specified'
            }

            common.gitCheckout(giturl, env.GIT_BRANCH)

            try {
                env.GIT_COMMIT = common.gitCommit()
                env.GIT_URL = common.gitUrl()

                env.JAVA_HOME = "${tool name: 'amazon-corretto-21', type: 'hudson.model.JDK'}"
                env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

                // Database migration
//                common.withLiquibaseCommandSecrets("rds/${liquibaseVaultDBName}/dev/master") {
//                    sh "./gradlew -Pliquibase.migration.environment=dev update"
//                }

                common.deployChart('dev', chartname, env.APPLICATION_VERSION, env.CHART_VERSION, namespace, [domain: 'dcitech.cloud'])
                String ACCEPTANCE_HOST = "${application}.dev.dcitech.cloud"
                sh "./gradlew -Dapplication.base.api.url=http://${ACCEPTANCE_HOST}/ -Dtest.environment=dev functionaltest"

                stash includes: '**', name: 'source'
            } catch (e) {
                common.notifyOnFailure(e, owner, application)
                throw e
            } finally {
                publishHTML(target: [allowMissing: true, alwaysLinkToLastBuild: true,
                                     keepAll     : true, reportDir: 'workflow-sync-service-test/build/reports/functionaltest/',
                                     reportFiles : 'index.html', reportName: 'Functional Test Report (Dev)'])
            }
        }
        stage('Deploy-QA') {
            unstash 'source'

            try {
                // Database migration
//                common.withLiquibaseCommandSecrets("rds/${liquibaseVaultDBName}/qa/master") {
//                    sh "./gradlew -Pliquibase.migration.environment=qa update"
//                }

                common.deployChart('qa', chartname, env.APPLICATION_VERSION, env.CHART_VERSION, namespace, [domain: 'dcitech.cloud'])
                String ACCEPTANCE_HOST = "${application}.qa.dcitech.cloud"
                sh "./gradlew -Dapplication.base.api.url=http://${ACCEPTANCE_HOST}/ -Dtest.environment=qa functionaltest"

                echo("Successfully Deployed. Version = " + env.APPLICATION_VERSION)
                echo("Git revision = " + env.GIT_COMMIT)
                echo("Helm chart version = " + env.CHART_VERSION)

                common.tagArtifactory(["latest": true], true)
                common.set_artifactory_tags(["latest": true], env.CHART_VERSION, true, 'deploy/params-helm.json')
            } catch (e) {
                common.notifyOnFailure(e, owner, application)
                throw e
            } finally {
                publishHTML(target: [allowMissing: true, alwaysLinkToLastBuild: true,
                                     keepAll     : true, reportDir: 'workflow-sync-service-test/build/reports/functionaltest/',
                                     reportFiles : 'index.html', reportName: 'Functional Test Report (QA)'])
            }
        }
    }
}

def promoted = false

stage('Promote-Decision') {
    try {
        input message: 'Deploy to UAT?'
        promoted = true
    } catch (err) {
        echo 'Aborted'
    }
}

node('eks-cluster') {
    container('eks-cluster') {
        if (promoted) {
            stage('Deploy-UAT') {

                sh "rm -rf *"
                common.gitCheckout(giturl)

                try {
                    env.GIT_REVISION = common.lookupGitRevision(application, env.APPLICATION_VERSION)
                    env.CHART_VERSION = common.lookupChartVersion(application, env.APPLICATION_VERSION)

                    env.JAVA_HOME = "${tool name: 'amazon-corretto-21', type: 'hudson.model.JDK'}"
                    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

                    sh "git checkout ${env.GIT_REVISION}"

                    // Database migration
//                    common.withLiquibaseCommandSecrets("rds/${liquibaseVaultDBName}/uat/master") {
//                        sh "./gradlew -Pliquibase.migration.environment=uat update"
//                    }

                    common.deployChart('uat', chartname, env.APPLICATION_VERSION, env.CHART_VERSION, namespace, [domain: 'dcitech.cloud'])

                    String ACCEPTANCE_HOST = "${application}.uat.dcitech.cloud"
                    sh "./gradlew -Dapplication.base.api.url=http://${ACCEPTANCE_HOST}/ -Dtest.environment=uat functionaltest"
                    common.tagGitVersion()
                    common.retagImage('deploy/docker-params.yml', env.APPLICATION_VERSION)

                    common.tagArtifactory(["release-candidate": true])
                } catch (e) {
                    common.notifyOnFailure(e, owner, application)
                    throw e
                } finally {
                    publishHTML(target: [allowMissing: true, alwaysLinkToLastBuild: true,
                                         keepAll     : true, reportDir: 'workflow-sync-service-test/build/reports/functionaltest/',
                                         reportFiles : 'index.html', reportName: 'Functional Test Report (UAT)'])
                }
            }
            stage('Setup Prod') {
                build job: '03.Production', wait: false, parameters: [
                        [
                                $class: 'StringParameterValue',
                                name  : 'APPLICATION_VERSION',
                                value : env.APPLICATION_VERSION
                        ],
                        [
                                $class: 'StringParameterValue',
                                name  : 'CHART_VERSION',
                                value : env.CHART_VERSION
                        ],
                        [
                                $class: 'BooleanParameterValue',
                                name  : 'DEPLOY',
                                value : false
                        ]
                ]
            }
        } else {
            echo "Promotion was declined for version $APPLICATION_VERSION"
            currentBuild.result = 'SUCCESS'
        }
    }
}
