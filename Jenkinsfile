pipeline {
    agent any

    environment {
        APP_NAME = "nodeapp"
        MONGO_NAME = "mongodb"
        APP_PORT = "4000"
        DOCKER_COMPOSE_FILE = "docker-compose.yml"
        DOCKER_TEST_IMAGE = "markhobson/maven-chrome:jdk-11"
        RECIPIENT_EMAIL = "huzaifaabid41@gmail.com"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo '=========================================='
                    echo 'STAGE 1: Checkout Code from GitHub'
                    echo '=========================================='
                }
                git branch: 'main', url: 'https://github.com/Iqrrakhan/jenkins-ci-cd.git', credentialsId: 'github-pat'
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
                            # Get the Docker bridge IP
                            DOCKER_HOST_IP=\$(ip -4 addr show docker0 | grep -Po 'inet \\K[\\d.]+')
                            echo "Docker host IP: \$DOCKER_HOST_IP"
                            
                            # Run tests
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

        stage('Publish Test Results') {
            steps {
                junit '**/selenium-tests/target/surefire-reports/*.xml'
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
                echo 'Processing Test Results for Email'
                echo '=========================================='
                
                // Parse test results from XML files
                def raw = sh(
                    script: "grep -h \"<testcase\" selenium-tests/target/surefire-reports/*.xml || echo ''",
                    returnStdout: true
                ).trim()

                int total = 0
                int passed = 0
                int failed = 0
                int skipped = 0
                def details = ""

                if (raw) {
                    raw.split('\n').each { line ->
                        total++

                        def nameMatcher = (line =~ /name=\"([^\"]+)\"/)
                        def name = nameMatcher ? nameMatcher[0][1] : "Unknown Test"

                        if (line.contains("<failure")) {
                            failed++
                            details += "❌ ${name} — FAILED\n"
                        } else if (line.contains("<skipped") || line.contains("</skipped>")) {
                            skipped++
                            details += "⏭️  ${name} — SKIPPED\n"
                        } else {
                            passed++
                            details += "✅ ${name} — PASSED\n"
                        }
                    }
                }

                def buildStatus = currentBuild.currentResult
                def statusEmoji = buildStatus == 'SUCCESS' ? '✅' : '❌'

                def emailBody = """
${statusEmoji} Jenkins Build ${buildStatus} - Build #${env.BUILD_NUMBER}
==========================================================================

PROJECT: ${env.JOB_NAME}
BUILD NUMBER: ${env.BUILD_NUMBER}
BUILD STATUS: ${buildStatus}
BUILD DURATION: ${currentBuild.durationString.replace(' and counting', '')}
BUILD URL: ${env.BUILD_URL}

==========================================================================
SELENIUM TEST SUMMARY
==========================================================================

Total Tests:   ${total}
Passed:        ${passed} ✅
Failed:        ${failed} ❌
Skipped:       ${skipped} ⏭️

Pass Rate:     ${total > 0 ? String.format("%.1f", (passed * 100.0 / total)) : '0'}%

==========================================================================
DETAILED TEST RESULTS
==========================================================================

${details ?: 'No test details available'}

==========================================================================
PIPELINE STAGES
==========================================================================

✅ Checkout Code from GitHub
✅ Stop Existing Containers
✅ Build & Start Application
✅ Wait for Application
${failed == 0 ? '✅' : '❌'} Run Selenium Tests
✅ Verify Deployment

==========================================================================
VIEW FULL REPORT
==========================================================================

Test Report: ${env.BUILD_URL}testReport/
Console Output: ${env.BUILD_URL}console
Artifacts: ${env.BUILD_URL}artifact/

==========================================================================

This is an automated email from Jenkins CI/CD Pipeline.
Generated at: ${new Date()}

"""

                emailext(
                    to: "${RECIPIENT_EMAIL}",
                    subject: "${statusEmoji} Build #${env.BUILD_NUMBER}: ${env.JOB_NAME} - ${buildStatus}",
                    body: emailBody,
                    attachmentsPattern: '**/selenium-tests/target/surefire-reports/*.xml'
                )
                
                echo "Email sent to: ${RECIPIENT_EMAIL}"
            }
            
            // Archive test reports
            archiveArtifacts artifacts: '**/selenium-tests/target/surefire-reports/**/*', allowEmptyArchive: true
        }
        
        success {
            echo '=========================================='
            echo '✅ PIPELINE SUCCESSFUL!'
            echo 'Application deployed and all tests passed'
            echo 'Email notification sent successfully'
            echo '=========================================='
        }
        
        failure {
            echo '=========================================='
            echo '❌ PIPELINE FAILED!'
            echo 'Check logs for details'
            echo 'Email notification sent with failure details'
            echo '=========================================='
            
            sh """
                echo "Application Logs:"
                docker logs $APP_NAME || true
                echo "MongoDB Logs:"
                docker logs $MONGO_NAME || true
            """
        }
    }
}
