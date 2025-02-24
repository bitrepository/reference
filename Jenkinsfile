#!/usr/bin/env groovy

pipeline {
    agent any
    // TODO: agent {
    //   label 'maven-agent' // Use a Jenkins node with this label
    //}
    tools {
        maven 'Maven' // Use the maven automatic installation configured in Jenkins
    }
    environment {
        // TODO: Need to use settings: -s /etc/m2/settings.xml
        MVN_CMD = 'mvn -s /etc/m2/settings.xml --batch-mode' // Define the base Maven command
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
                sh "${env.MVN_CMD} -PallTests clean package"
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
                    echo "Deploying '${env.BRANCH_NAME}' branch to Nexus"
                    if (env.BRANCH_NAME == 'master') {
                        sh "${env.MVN_CMD} clean deploy -DskipTests=true"
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
