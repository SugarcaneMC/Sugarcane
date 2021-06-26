pipeline {
    agent { label 'slave' }
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
                scmSkip(deleteBuild: true, skipPattern:'.*\\[CI-SKIP\\].*')
                // sh 'git config --global gc.auto 0'
                // sh 'rm -rf ./target'
                // sh 'rm -rf ./Sugarcane-API ./Sugarcane-Server'
                // sh 'rm -rf .gradle'
                sh 'chmod +x ./gradlew'
                // sh './gradlew clean -DXms1G -DXmx2G'
            }
        }
        stage('Decompile & apply patches') {
            tools {
                jdk "JDK 16"
            }
            steps {
                    sh '''
                    git config user.email "jenkins@sugarcanemc.org"
                    git config user.name "Jenkins"
                    ./gradlew applyPatches
                    '''
                }
            }
        stage('Build') {
            tools {
                jdk "JDK 16"
            }
            steps {
                        sh'''
                        ./gradlew build paperclip publish
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
                    discordSend description: "Sugarcane Jenkins Build", footer: "Sugarcane", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: discord_webhook1
                }
            }
           post {
                always {
                    // cleanWs()
                }
            }
        }
    }
}
