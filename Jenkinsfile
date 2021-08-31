pipeline {
    agent { label 'main' }
    options { timestamps() }

   environment {
        discord_webhook1 = credentials('discord_webhook')
    }
    
    stages {
        stage('Cleanup') {
            tools {
                jdk "JDK 16"
            }
            steps {
                scmSkip(deleteBuild: true, skipPattern:'.*\[ci skip\].*')
                sh 'git config --global gc.auto 0'
                sh 'rm -rf ./target'
            }
        }
        stage('Decompile & apply patches') {
            tools {
                jdk "JDK 16"
            }
            steps {
                    sh '''
                    git checkout ${BRANCH_NAME}
                    git reset --hard
                    git fetch 
                    git pull
                    git config user.email "jenkins@sugarcanemc.org"
                    git config user.name "Jenkins"
                    rm -rf Sugarcane-Server
                    rm -rf Sugarcane-API
                    ./gradlew printMinecraftVersionAP applyPatches
                    '''
                }
            }
        stage('Build') {
            tools {
                jdk "JDK 16"
            }
            steps {
                        sh'''
                        ./gradlew printMinecraftVersionBD build paperclipJar :Sugarcane-API:publishMavenPublicationToMavenRepository publishToMavenLocal
                        mkdir -p "./target"
                        cp -v "sugarcane-paperclip.jar" "./target/sugarcane-paperclip-b$BUILD_NUMBER.jar"
                        '''
            }
        }

        stage('Archive Jars') {
            steps {
                archiveArtifacts(artifacts: 'target/*.jar', fingerprint: true)
            }
        }
        
     
        stage('Discord Webhook') {
            steps {
                script {
                    env.GIT_COMMIT_MSG = sh (script: 'git log -1 --pretty=%B HEAD', returnStdout: true).trim()
                    env.GIT_COMMIT_CUT = sh (script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.GIT_COMMIT_AUTHOR = sh (script: 'git log -1 --pretty=%an HEAD', returnStdout: true).trim()
                    env.ARTIFACT_URL = "${BUILD_URL}artifact/target/sugarcane-paperclip-b${BUILD_NUMBER}.jar"
                    discordSend description: """
**Build:** [${BUILD_DISPLAY_NAME}](${BUILD_URL})
**Status:** [${currentBuild.currentResult.toLowerCase()}](${BUILD_URL})

**Changes:** 
- `${GIT_COMMIT_CUT}` ${GIT_COMMIT_MSG} *- ${GIT_COMMIT_AUTHOR}*

**Artifacts:** 
- [sugarcane-paperclip-b${BUILD_NUMBER}.jar](${ARTIFACT_URL})""", footer: "Build: ${BUILD_DISPLAY_NAME}", link: BUILD_URL, result: currentBuild.currentResult, title: "**Sugarcane** - ${BRANCH_NAME} ${BUILD_DISPLAY_NAME}", webhookURL: discord_webhook1
                }
            }   
        }
    }
}
