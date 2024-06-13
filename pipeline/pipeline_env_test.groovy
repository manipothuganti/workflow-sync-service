@Library('jpl@1.4.4')
import com.discovery.jpl.PipelineSupport

String giturl = 'git@github.com:discoveryinc-cs/workflow-sync-service.git'
String application = 'workflow-sync-service'
String owner = 'dl-turbo@discovery.com'

def common = new PipelineSupport()

node('eks-cluster') {
    container('eks-cluster') {

        stage('Environment Test') {
            sh 'rm -rf *'

            env.JAVA_HOME = "${tool name: 'amazon-corretto-21', type: 'hudson.model.JDK'}"
            env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

            String gitbranch = USE_GIT_BRANCH
            println "Testing from " + giturl + ", Branch " + gitbranch
            common.gitCheckout(giturl)
            sh "git checkout ${gitbranch}"

            try {
                String cmd = "./gradlew" +
                        (TARGET_BASEURL ? " -Dapplication.base.api.url=${TARGET_BASEURL}" : "") +
                        (TARGET_ENV ? " -Dtest.environment=${TARGET_ENV}" : "") +
                        (TEST_CATEGORIES ? " -Ptest.includes=${TEST_CATEGORIES}" : "") +
                        " functionaltest"

                sh cmd
            } catch (e) {
                common.notifyOnFailure(e, owner, application)
                throw e
            } finally {
                publishHTML(target: [allowMissing: true, alwaysLinkToLastBuild: true,
                                     keepAll     : true, reportDir: 'workflow-sync-service-test/build/reports/functionaltest/',
                                     reportFiles : 'index.html', reportName: 'Test Report'])
            }
        }
    }
}