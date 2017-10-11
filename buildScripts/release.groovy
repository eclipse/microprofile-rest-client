pipeline {
    agent any
    tools {
        maven 'apache-maven-latest'
        jdk 'jdk1.8.0-latest'
    }
    parameters {
        string(description: 'The next snapshot version', name: 'snapshotVersion')
        string(description: 'The release version', name: 'releaseVersion')
        string(description: 'The SCM tag to apply', name: 'tag')
    }

    stages {
        stage("Execute Release") {
            steps {
                sh "mvn -s /opt/public/hipp/homes/genie.microprofile/.m2/settings-deploy-ossrh.xml release:prepare release:perform -B -Dtag=${params.tag} -DdevelopmentVersion=${params.snapshotVersion} -DreleaseVersion=${params.releaseVersion}"
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}
