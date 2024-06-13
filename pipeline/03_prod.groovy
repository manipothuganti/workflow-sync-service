@Library('jpl@1.4.4')
import com.discovery.jpl.PipelineSupport

String giturl = 'git@github.com:discoveryinc-cs/workflow-sync-service.git'
String application = 'workflow-sync-service'
String owner = 'dl-turbo@discovery.com'
String chartname = 'chart-workflow-sync-service'
String namespace = 'distribute'
String liquibaseVaultDBName = 'planning'

boolean deploy_to_prod = true

def common = new PipelineSupport()

currentBuild.displayName = '#' + env.BUILD_NUMBER + ' (v' + params.APPLICATION_VERSION + ')'

stage('Confirmation') {
    when(params.DEPLOY) {
        try {
            timeout(time: 30, unit: 'MINUTES') {
                input message: "Deploy to v${params.APPLICATION_VERSION} to production?"
            }
        } catch (err) {
            println "Skipping remaining stages."
            deploy_to_prod = false
        }
    }
}

stage('Deploy-prod') {
    env.JPL_DOCKER_ENV = "prod"
    env.JPL_KUBECONFIG_ACCOUNT = 'cs-prod'

    when(params.DEPLOY && deploy_to_prod) {
        node('eks-cluster') {
            container('eks-cluster') {
                try {
                    sh "rm -rf *"
                    println "Deploying Prod: ${APPLICATION_VERSION}"
                    env.APPLICATION_VERSION = APPLICATION_VERSION

                    common.gitCheckout(giturl)

                    env.JAVA_HOME = "${tool name: 'amazon-corretto-21', type: 'hudson.model.JDK'}"
                    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

                    env.GIT_REVISION = common.lookupGitRevision(application, env.APPLICATION_VERSION)
                    env.CHART_VERSION = common.lookupChartVersion(application, env.APPLICATION_VERSION)

                    sh "git checkout ${env.GIT_REVISION}"

                    // Database migration
//                    common.withLiquibaseCommandSecrets("rds/${liquibaseVaultDBName}/prod/master") {
//                        sh "./gradlew -Pliquibase.migration.environment=prod update"
//                    }

                    common.deployChart('prod', chartname, env.APPLICATION_VERSION, env.CHART_VERSION,
                            namespace, [domain: 'dcitech.cloud'])

                    String ACCEPTANCE_HOST = "${application}.prod.dcitech.cloud"
                    sh "./gradlew -Dapplication.base.api.url=http://${ACCEPTANCE_HOST}/" +
                            " -Ptest.includes=smoke -Dtest.environment=prod functionaltest"
                } catch (e) {
                    common.notifyOnFailure(e, owner, application)
                    throw e
                } finally {
                    publishHTML(target: [allowMissing: true, alwaysLinkToLastBuild: true,
                                         keepAll     : true, reportDir: 'workflow-sync-service-test/build/reports/functionaltest/',
                                         reportFiles : 'index.html', reportName: 'Functional Test Report (Prod)'])
                }
            }
        }
    }
}
