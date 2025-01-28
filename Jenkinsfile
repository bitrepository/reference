#!/usr/bin/env groovy

pipeline {
    agent any
    // TODO: agent {
    //   label 'maven-agent' // Use a Jenkins node with this label
    //}
    environment {
        // TODO: Need to use settings: -s /etc/m2/settings.xml
        MVN_CMD = 'mvn --batch-mode' // Define the base Maven command
    }
    options {
        disableConcurrentBuilds() // Prevent concurrent builds
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Mvn clean package') {
            steps {
                withMaven {
                    sh "${env.MVN_CMD} -PallTests clean package"
                }
            }
        }
        stage('Analyze build results') {
            steps {
                recordIssues aggregatingResults: true, tools: [
                    java(),
                    javaDoc(),
                    mavenConsole(),
                    taskScanner(
                        highTags: 'FIXME',
                        normalTags: 'TODO',
                        includePattern: '**/*.java',
                        excludePattern: 'target/**/*'
                    )
                ]
            }
        }
        stage('Push to Nexus (if Master)') {
            steps {
                script {
                    echo "Branch name '${env.BRANCH_NAME}'"
                    if (env.BRANCH_NAME == 'master') {
                        withMaven {
                            sh "${env.MVN_CMD} clean deploy -DskipTests=true"
                        }
                    } else {
                        echo "Branch '${env.BRANCH_NAME}' is not master, so no deployment to Nexus."
                    }
                }
            }
        }
    }
    post {
        success {
            echo 'Build succeeded.'
        }
        failure {
            // TODO: Notify on email (possibly just use plugin)
            echo 'Build failed, investigate errors in the console output.'
        }
    }
}
