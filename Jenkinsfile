#!/usr/bin/env groovy

pipeline {
    agent {
        label 'maven-agent' // Use Jenkins node with this label
    }
    tools {
        maven 'Maven' // Use the maven automatic installation configured in Jenkins
    }
    environment {
        MVN_CMD = 'mvn -s /etc/m2/settings.xml --batch-mode' // Define the base Maven command
        APP_NAME = 'bitrepository' // Application Name (Must match ArgoCD)
        AUTH_TOKEN = 'TOKEN' // Authentication token for ArgoCD
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
                    if (env.BRANCH_NAME == 'master') {
                        echo "Deploying '${env.BRANCH_NAME}' branch to Nexus"
                        sh "${env.MVN_CMD} clean deploy -DskipTests=true"
                    }
                }
            }
        }
    }
    post {
        success {
            script {
                echo 'Build succeeded, syncing application in ArgoCD.'
            }
        }
        failure {
            echo 'Build failed, investigate errors in the console output.'
        }
    }
}
