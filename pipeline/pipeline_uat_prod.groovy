@Library('jpl@1.4.4')
import com.discovery.jpl.PipelineSupport

String giturl = 'git@github.com:discoveryinc-cs/workflow-sync-service.git'
String application = 'workflow-sync-service'
String owner = 'dl-turbo@discovery.com'
String liquibaseVaultDBName = ''

def common = new PipelineSupport()

node {
    stage('Deploy-uat') {

        sh "rm -rf *"
        println "Deploying UAT: ${APPLICATION_VERSION}"
        env.APPLICATION_VERSION = APPLICATION_VERSION
        common.gitCheckout(giturl)

        try {
            env.JAVA_HOME = "${tool name: 'amazon-corretto-21', type: 'hudson.model.JDK'}"
            env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

            common.getDeploymentTools()

            env.GIT_REVISION = common.getGitRevision()
            sh "git checkout ${env.GIT_REVISION}"

            // Database migration
//            common.withLiquibaseCommandSecrets("rds/${liquibaseVaultDBName}/uat/master") {
//                sh "./gradlew -Pliquibase.migration.environment=uat migrate"
//            }

            common.amazonDeployment('uat', 'content-systems')
            common.amazonElbCheck('uat')

            String ACCEPTANCE_HOST = common.getAcceptanceHost()
            sh "./gradlew -Dapplication.base.api.url=http://${ACCEPTANCE_HOST}/ -Dtest.environment=uat functionaltest"

            common.amazonPromotion('uat')
            common.tagGitVersion()

            common.tagArtifactory(["release-candidate": true])
        } catch (e) {
            common.notifyOnFailure(e, owner, application)
            throw e
        } finally {
            publishHTML(target: [allowMissing: true, alwaysLinkToLastBuild: true,
                                 keepAll     : true, reportDir: 'workflow-sync-service-test/build/reports/functionaltest/',
                                 reportFiles : 'index.html', reportName: 'Functional Test Report (Uat)'])
        }

    }
}


stage('Promote-Decision') {
    input message: "Are you ready to deploy to production?"
}

node {
    stage('Deploy-prod') {
        try {
            sh "rm -rf *"

            env.JAVA_HOME = "${tool name: 'amazon-corretto-21', type: 'hudson.model.JDK'}"
            env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

            println "Deploying Prod: ${APPLICATION_VERSION}"
            env.APPLICATION_VERSION = APPLICATION_VERSION
            common.gitCheckout(giturl)

            common.getDeploymentTools()

            env.GIT_REVISION = common.getGitRevision()
            sh "git checkout ${env.GIT_REVISION}"

            // Database migration
//            common.withLiquibaseCommandSecrets("rds/${liquibaseVaultDBName}/prod/master") {
//                sh "./gradlew -Pliquibase.migration.environment=prod migrate"
//            }

            common.amazonDeployment('prod', 'content-systems')
            common.amazonElbCheck('prod')

            String ACCEPTANCE_HOST = common.getAcceptanceHost()
            sh "./gradlew -Dapplication.base.api.url=http://${ACCEPTANCE_HOST}/" +
                    " -Ptest.includes=smoke -Dtest.environment=prod functionaltest"

            common.amazonPromotion('prod')
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
