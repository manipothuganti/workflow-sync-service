@Library('jpl@1.4.4')
import com.discovery.jpl.PipelineSupport

String deploy_project = 'workflow-sync-service'
String giturl = 'git@github.com:discoveryinc-cs/workflow-sync-service.git'
String notify = 'dl-turbo@discovery.com'

def common = new PipelineSupport()

node('eks-cluster') {
    container('eks-cluster') {
        stage('Build') {
            sh 'rm -rf *'

            env.GIT_BRANCH = USE_GIT_BRANCH
            env.JPL_DOCKER_ENV = "prod"

            common.gitCheckout(giturl, env.GIT_BRANCH)

            try {
                env.GIT_COMMIT = common.gitCommit()
                env.GIT_URL = common.gitUrl()

                env.JAVA_HOME = "${tool name: 'amazon-corretto-21', type: 'hudson.model.JDK'}"
                env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

                sh './gradlew clean test integrationtest bootjar'

                withSonarQubeEnv('msc-prod') {
                    sh './gradlew sonar -Dsonar.branch.name=${GIT_BRANCH}'
                }
                timeout(time: 2, unit: 'MINUTES') {
                    def qg = waitForQualityGate()
                    if (qg.status != 'OK') {
                        error "Pipeline aborted due to SonarQube quality gate failure: ${qg.status}"
                    }
                }

                sh './gradlew publish dockerprep'

                env.APPLICATION_VERSION = common.getApplicationVersion("${deploy_project}/build/resources/main/META-INF/build-info.properties")
                env.CHART_VERSION = common.buildChart('deploy/chart-workflow-sync-service')

                common.buildImage('prod', "${deploy_project}/build/docker", 'deploy/docker-params.yml', env.APPLICATION_VERSION)

                common.tagArtifactory(["snapshotless": true, "git_commit": env.GIT_COMMIT])

                build job: '02.Deploy', wait: false,
                        parameters: [
                                [$class: 'StringParameterValue', name: 'APPLICATION_VERSION', value: env.APPLICATION_VERSION],
                                [$class: 'StringParameterValue', name: 'CHART_VERSION', value: env.CHART_VERSION],
                                [$class: 'StringParameterValue', name: 'USE_GIT_BRANCH', value: env.GIT_BRANCH]
                        ]

                echo("Version = " + env.APPLICATION_VERSION)
                echo("Git branch = " + env.GIT_BRANCH)
                echo("Git revision = " + env.GIT_COMMIT)
                echo("Chart version = " + env.CHART_VERSION)
            } catch (e) {
                common.notifyOnFailure(e, notify, deploy_project)
                sleep 180
                throw e
            } finally {
                step([$class: 'JUnitResultArchiver', testResults: '**/build/**/TEST-*.xml'])
            }
        }
    }
}
