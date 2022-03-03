#!/usr/bin/env groovy

openshift.withCluster() { 

    podTemplate(
            inheritFrom: 'maven',
            cloud: 'openshift', //cloud must be openshift
            envVars: [ //This fixes the error with en_US.utf8 not being found
                    envVar(key:"LC_ALL", value:"C.utf8")
            ],
            volumes: [ //mount the settings.xml
                       secretVolume(mountPath: '/etc/m2', secretName: 'maven-settings')
            ]) {

        try {
            //GO to a node with maven and settings.xml
            node(POD_LABEL) {
                //Do not use concurrent builds
                properties([disableConcurrentBuilds()])

                def mvnCmd = "mvn -s /etc/m2/settings.xml --batch-mode"

                stage('checkout') {
                    checkout scm
                }

                stage('Mvn clean package') {
                    sh "${mvnCmd} -PallTests clean package"
                }

                stage('Analyze build results') {
                    recordIssues aggregatingResults: true,
                        tools: [java(),
                                javaDoc(),
                                mavenConsole(),
                                taskScanner(highTags:'FIXME', normalTags:'TODO', includePattern: '**/*.java', excludePattern: 'target/**/*')]
                }

                stage('Push to Nexus (if Master)') {
                    echo "Branch name ${env.BRANCH_NAME}"
                    if (env.BRANCH_NAME == 'master') {
	                    sh "${mvnCmd} clean deploy -DskipTests=true"
                    } else {
	                    echo "Branch ${env.BRANCH_NAME} is not master, so no mvn deploy"
                    }
                }
            }
        } catch (e) {
            currentBuild.result = 'FAILURE'
            throw e
        } finally {
            configFileProvider([configFile(fileId: "notifier", variable: 'notifier')]) {
                def notifier = load notifier
                notifier.notifyInCaseOfFailureOrImprovement(true, "#playground")
            }
        }
    }
}

