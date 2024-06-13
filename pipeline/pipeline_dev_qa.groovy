@Library('jpl@1.4.4')
import com.discovery.jpl.PipelineSupport

String giturl = 'git@github.com:discoveryinc-cs/workflow-sync-service.git'
String application = 'workflow-sync-service'
String deploy_project = 'workflow-sync-service'
String owner = 'dl-turbo@discovery.com'
String liquibaseVaultDBName = ''

def common = new PipelineSupport()

node {
    stage('Build') {
        sh 'rm -rf *'

        String gitbranch = USE_GIT_BRANCH
        println "Building " + giturl + ", Branch " + gitbranch
        common.gitCheckout(giturl, gitbranch)

        try {
            env.JAVA_HOME = "${tool name: 'amazon-corretto-21', type: 'hudson.model.JDK'}"
            env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

            env.GIT_COMMIT = common.gitCommit()
            sh 'echo $GIT_COMMIT'

            env.GIT_URL = common.gitUrl()
            env.GIT_BRANCH = gitbranch
            sh 'echo $GIT_URL, $GIT_BRANCH'

            sh 'java -version'
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

            sh './gradlew publish'

            env.APPLICATION_VERSION = common.getApplicationVersion("${deploy_project}/build/resources/main/META-INF/build-info.properties")

            sh 'echo $APPLICATION_VERSION'

            common.getDeploymentTools()
            common.tagArtifactory(["snapshotless": true, "git_commit": env.GIT_COMMIT])

            stash includes: '**', name: 'source'
        } catch (e) {
            common.notifyOnFailure(e, owner, application)
            throw e
        } finally {
            step([$class: 'JUnitResultArchiver', testResults: '**/build/**/TEST-*.xml'])
        }
    }

    stage('Deploy-dev') {
        try {
            unstash 'source'
            common.getDeploymentTools()

            // Database migration
//            common.withLiquibaseCommandSecrets("rds/${liquibaseVaultDBName}/dev/master") {
//                sh "./gradlew -Pliquibase.migration.environment=dev migrate"
//            }

            common.amazonDeployment('dev', 'content-systems')
            common.amazonElbCheck('dev')

            String ACCEPTANCE_HOST = common.getAcceptanceHost()
            sh "./gradlew -Dapplication.base.api.url=http://${ACCEPTANCE_HOST}/ -Dtest.environment=dev functionaltest"

            common.amazonPromotion('dev')
        } catch (e) {
            common.notifyOnFailure(e, owner, application)
            throw e
        } finally {
            publishHTML(target: [allowMissing: true, alwaysLinkToLastBuild: true,
                                 keepAll     : true, reportDir: 'workflow-sync-service-test/build/reports/functionaltest/',
                                 reportFiles : 'index.html', reportName: 'Functional Test Report (Dev)'])
        }
    }

    stage('Deploy-qa') {
        try {
            unstash 'source'
            common.getDeploymentTools()

            // Database migration
//            common.withLiquibaseCommandSecrets("rds/${liquibaseVaultDBName}/qa/master") {
//                sh "./gradlew -Pliquibase.migration.environment=qa migrate"
//            }

            common.amazonDeployment('qa', 'content-systems')
            common.amazonElbCheck('qa')

            String ACCEPTANCE_HOST = common.getAcceptanceHost()
            sh "./gradlew -Dapplication.base.api.url=http://${ACCEPTANCE_HOST}/ -Dtest.environment=qa functionaltest"

            common.amazonPromotion('qa')

            common.tagArtifactory(["latest": true], true)

            echo("Successfully Deployed. Version = " + env.APPLICATION_VERSION)
            echo("Git revision = " + env.GIT_COMMIT)
        } catch (e) {
            common.notifyOnFailure(e, owner, application)
            throw e
        } finally {
            publishHTML(target: [allowMissing: true, alwaysLinkToLastBuild: true,
                                 keepAll     : true, reportDir: 'workflow-sync-service-test/build/reports/functionaltest/',
                                 reportFiles : 'index.html', reportName: 'Functional Test Report (Qa)'])
        }
    }
}

