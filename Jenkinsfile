pipeline {
    agent none
    environment {
        VERSION = "${env.BUILD_ID}"
    }
    stages {
        stage("Build") {

            agent {
                docker {
                    image 'openjdk:8-jdk-slim'
                    args '-v $HOME/.gradle:/home/jenkins/.gradle'
                }
            }

            steps {
                sh './gradlew clean compileJava'
                sh './gradlew clean test'
                junit '**/test-results/test/*.xml'
                sh './gradlew sonarqube -Dsonar.host.url=http://51.158.120.222:8085 -Dsonar.login=da47239c76c1b9747a86f8a648883946950e6327 -Dsonar.java.coveragePlugin=jacoco'
                sh './gradlew clean customFatJar'
                dir('.ci') {
                    stash name: 'dockerfile', includes: 'Dockerfile'
                    stash name: 'docker-compose-stack', includes: 'docker-compose-stack.yml'
                }
                dir('build/libs') {
                    stash name: 'jar', includes: 'cd_demo_movie_web-all-1.0-SNAPSHOT.jar'
                }
            }
        }

        stage("Package") {

            agent {
                docker {
                    image 'ugurgungor/dind-agent:1.0'
                    args '-u root:root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }

            steps {
                unstash 'dockerfile'
                unstash 'jar'
                sh "docker build . -t 51.15.240.50:8082/movie-web:$VERSION"
                sh "sudo docker login -u admin -p admin123 51.15.240.50:8082"
                sh "sudo docker push 51.15.240.50:8082/movie-web:$VERSION"
            }

            post {
                success {
                    sh "sudo docker image rm 51.15.240.50:8082/movie-web:$VERSION"
                }
            }
        }

        stage("Deploy2QA") {

            agent any

            steps {
                echo 'deploy to qa same as deploy2prod'
            }
        }

        stage("E2E Test") {

            agent any

            steps {
                echo 'run e2e test on qa env'
            }
        }

        stage('Deploy2PROD') {
            agent any
            steps {
                unstash 'docker-compose-stack'
                sshPublisher(
                        continueOnError: false, failOnError: true,
                        publishers: [
                                sshPublisherDesc(
                                        configName: "root@swarm-prod",
                                        verbose: true,
                                        transfers: [
                                                sshTransfer(
                                                        sourceFiles: "docker-compose-stack.yml",
                                                        remoteDirectory: "/deployment/movie-web/$VERSION/",
                                                ),
                                                sshTransfer(
                                                        execCommand: "sudo docker login -u admin -p admin123 51.15.240.50:8082"
                                                ),
                                                sshTransfer(
                                                        execCommand: "sudo VERSION=$VERSION ENV=production docker stack deploy -c /home/ubuntu/deployment/movie-web/$VERSION/docker-compose-stack.yml --prune --with-registry-auth movie-web"
                                                )
                                        ])
                        ])
            }
        }
    }
}

