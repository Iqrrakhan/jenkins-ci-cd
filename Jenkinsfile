
pipeline {
    agent any

    environment {
        APP_NAME = "nodeapp"
        MONGO_NAME = "mongodb"
        APP_PORT = "4000"
        DOCKER_COMPOSE_FILE = "docker-compose.yml"
        DOCKER_TEST_IMAGE = "markhobson/maven-chrome:jdk-11"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo '=========================================='
                    echo 'STAGE 1: Checkout Code from GitHub'
                    echo '=========================================='
                }
                git branch: 'main', url: 'https://github.com/HuzefaAbid/Airbnb-clone-ci.git', credentialsId: 'github-pat'
            }
        }

        stage('Stop Existing Containers') {
            steps {
                script {
                    echo '=========================================='
                    echo 'STAGE 2: Stop Existing Containers'
                    echo '=========================================='
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
                    echo '=========================================='
                    echo 'STAGE 3: Build & Start Application'
                    echo '=========================================='
                    sh """
                        docker-compose -f $DOCKER_COMPOSE_FILE up -d --build
                    """
                }
            }
        }

        stage('Wait for Application') {
            steps {
                script {
                    echo '=========================================='
                    echo 'STAGE 4: Wait for Application to Start'
                    echo '=========================================='
                    sh """
                        echo "Waiting for application to be ready..."
                        sleep 15
                        
                        # Check if app is responding
                        for i in {1..10}; do
                            if curl -f http://localhost:$APP_PORT > /dev/null 2>&1; then
                                echo "✓ Application is ready!"
                                exit 0
                            fi
                            echo "Waiting... attempt \$i/10"
                            sleep 3
                        done
                        
                        echo "✗ Application failed to start"
                        docker logs $APP_NAME
                        exit 1
                    """
                }
            }
        }

        stage('Run Selenium Tests') {
            steps {
                script {
                    echo '=========================================='
                    echo 'STAGE 5: Running Selenium Tests in Docker'
                    echo '=========================================='
                    
                    dir('selenium-tests') {
                        sh """
                            # Get the Docker bridge IP for host.docker.internal equivalent
                            DOCKER_HOST_IP=\$(ip -4 addr show docker0 | grep -Po 'inet \\K[\\d.]+')
                            echo "Docker host IP: \$DOCKER_HOST_IP"
                            
                            # Run tests in Docker container
                            docker run --rm \
                                --network host \
                                -v \$(pwd):/tests \
                                -w /tests \
                                -e MAVEN_OPTS='-Xmx1024m' \
                                ${DOCKER_TEST_IMAGE} \
                                mvn clean test -Dapp.url=http://\$DOCKER_HOST_IP:${APP_PORT}
                        """
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    echo '=========================================='
                    echo 'STAGE 6: Final Deployment Verification'
                    echo '=========================================='
                    sh """
                        curl -f http://localhost:$APP_PORT || exit 1
                        echo "✓ Application is running and accessible"
                    """
                }
            }
        }
    }

    post {
        always {
            script {
                echo '=========================================='
                echo 'Post-Build Actions'
                echo '=========================================='
            }
            
            // Publish test results
            junit allowEmptyResults: true, testResults: '**/selenium-tests/target/surefire-reports/*.xml'
            
            // Archive test reports
            archiveArtifacts artifacts: '**/selenium-tests/target/surefire-reports/**/*', allowEmptyArchive: true
        }
        
        success {
            echo '=========================================='
            echo '✓ PIPELINE SUCCESSFUL!'
            echo 'Application deployed and all tests passed'
            echo '=========================================='
        }
        
        failure {
            echo '=========================================='
            echo '✗ PIPELINE FAILED!'
            echo 'Check logs for details'
            echo '=========================================='
            
            // Show container logs on failure
            sh """
                echo "Application Logs:"
                docker logs $APP_NAME || true
                echo "MongoDB Logs:"
                docker logs $MONGO_NAME || true
            """
        }
    }
}
