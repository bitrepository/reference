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
        ARGOCD_SERVER = 'argocd-server.argocd.svc.cluster.local' // The argoCD server
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
    }
    post {
        success {
            script {
                echo 'Build succeeded, syncing application in ArgoCD.'
                withCredentials([string(credentialsId: 'argocd-deployment-token', variable: 'ARGOCD_TOKEN')]) {
                    sh "argocd login ${env.ARGOCD_SERVER} --insecure --grpc-web --auth-token ${ARGOCD_TOKEN}"
                    sh "argocd app sync ${env.APP_NAME}"
                }
            }
        }
        failure {
            echo 'Build failed, investigate errors in the console output.'
        }
    }
}
