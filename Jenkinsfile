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
        APP_NAME = 'bitrepository-reference' // Application Name (Must match ArgoCD)
        ARGOCD_SERVER = 'localhost:8080' // The argoCD server
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
    }
    post {
        success {
            script {
                echo 'Build succeeded, syncing application in ArgoCD.'
                withCredentials([string(credentialsId: 'argocd-password', variable: 'ARGOCD_PASSWORD')]) {
                    sh "argocd login ${env.ARGOCD_SERVER} --insecure --grpc-web --username jenkins --password $ARGOCD_PASSWORD"
                    sh "argocd app sync ${env.APP_NAME}"
                }
            }
        }
        failure {
            echo 'Build failed, investigate errors in the console output.'
        }
    }
}
