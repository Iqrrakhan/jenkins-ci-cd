
pipeline {
    agent any

    environment {
        APP_NAME = "nodeapp"
        MONGO_NAME = "mongodb"
        APP_PORT = "4000"
        DOCKER_COMPOSE_FILE = "docker-compose.yml"
    }

    stages {
        stage('Checkout') {
            steps {
                // Pull code from GitHub
                git branch: 'main', url: 'https://github.com/HuzefaAbid/Airbnb-clone-ci.git', credentialsId: 'github-pat'
            }
        }

        stage('Stop Existing Containers') {
            steps {
                script {
                    // Stop and remove running containers if they exist
                    sh """
                        docker-compose -f $DOCKER_COMPOSE_FILE down || true
                        docker rm -f $APP_NAME || true
                        docker rm -f $MONGO_NAME || true
                    """
                }
            }
        }

        stage('Build & Start Containers') {
            steps {
                script {
                    // Start containers using docker-compose
                    sh """
                        docker-compose -f $DOCKER_COMPOSE_FILE up -d
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    // Check if the app is running
                    sh """
                        sleep 10
                        curl -f http://localhost:$APP_PORT || exit 1
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment Successful!'
        }
        failure {
            echo 'Deployment Failed!'
        }
    }
}

