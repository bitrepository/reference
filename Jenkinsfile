#!/usr/bin/env groovy

pipeline {
    agent {
        label 'maven-agent' // Use Jenkins node with this label
    }
    tools {
        maven 'Maven' // Use the maven automatic installation configured in Jenkins
    }
    environment {
        APP_NAME = 'bitrepository-reference' // Application Name (Must match ArgoCD)
        ARGOCD_SERVER = 'localhost:8080' // The argoCD server
        ARGOCD_PASSWORD = credentials('argocd-password') // Pulling password from Jenkins secrets
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
    // TODO: Re-add the removed stages, and fix ARGOCD_SERVER to match a real argoCD server.
    post {
        success {
            script {
                echo 'Build succeeded, syncing application in ArgoCD.'
                sh '''
                    argocd login $ARGOCD_SERVER --insecure --grpc-web --username jenkins --password $ARGOCD_PASSWORD
                    argocd app sync $APP_NAME
                '''
            }
        }
        failure {
            echo 'Build failed, investigate errors in the console output.'
        }
    }
}
