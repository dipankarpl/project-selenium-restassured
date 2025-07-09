pipeline {
    agent any
    
    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['qa', 'uat', 'prod'],
            description: 'Environment to run tests against'
        )
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox', 'edge'],
            description: 'Browser to run tests on'
        )
        choice(
            name: 'TEST_SUITE',
            choices: ['sanity', 'regression', 'all'],
            description: 'Test suite to execute'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run tests in headless mode'
        )
        booleanParam(
            name: 'PARALLEL_EXECUTION',
            defaultValue: true,
            description: 'Enable parallel test execution'
        )
        string(
            name: 'REMOTE_URL',
            defaultValue: '',
            description: 'Remote WebDriver URL (optional)'
        )
        string(
            name: 'THREAD_COUNT',
            defaultValue: '3',
            description: 'Number of parallel threads'
        )
    }
    
    environment {
        MAVEN_OPTS = '-Xmx2g -XX:+UseG1GC'
        JAVA_HOME = '/usr/lib/jvm/java-11-openjdk'
        PATH = "${JAVA_HOME}/bin:${PATH}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo "Checking out source code..."
                checkout scm
            }
        }
        
        stage('Setup Environment') {
            steps {
                script {
                    echo "Setting up environment: ${params.ENVIRONMENT}"
                    
                    // Set environment variables
                    env.ENVIRONMENT = params.ENVIRONMENT
                    env.BROWSER = params.BROWSER
                    env.HEADLESS = params.HEADLESS
                    env.REMOTE_URL = params.REMOTE_URL
                    
                    // Create directories
                    sh '''
                        mkdir -p reports/allure-results
                        mkdir -p reports/extent-report
                        mkdir -p reports/screenshots
                        mkdir -p logs
                    '''
                }
            }
        }
        
        stage('Install Dependencies') {
            steps {
                echo "Installing Maven dependencies..."
                sh 'mvn clean compile test-compile'
            }
        }
        
        stage('Run Tests') {
            steps {
                script {
                    def testSuite = params.TEST_SUITE
                    def threadCount = params.THREAD_COUNT
                    
                    echo "Running ${testSuite} tests with ${threadCount} threads..."
                    
                    try {
                        if (testSuite == 'sanity') {
                            sh "mvn test -Psanity -Dthread.count=${threadCount}"
                        } else if (testSuite == 'regression') {
                            sh "mvn test -Pregression -Dthread.count=${threadCount}"
                        } else {
                            sh "mvn test -Psanity -Dthread.count=${threadCount}"
                            sh "mvn test -Pregression -Dthread.count=${threadCount}"
                        }
                    } catch (Exception e) {
                        echo "Test execution failed: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Generate Reports') {
            steps {
                script {
                    echo "Generating test reports..."
                    
                    // Generate Allure Report
                    try {
                        sh 'mvn allure:report'
                    } catch (Exception e) {
                        echo "Allure report generation failed: ${e.getMessage()}"
                    }
                    
                    // Archive test results
                    publishTestResults testResultsPattern: '**/target/surefire-reports/TEST-*.xml'
                    
                    // Archive reports
                    archiveArtifacts artifacts: 'reports/**/*', allowEmptyArchive: true
                    archiveArtifacts artifacts: 'logs/**/*', allowEmptyArchive: true
                }
            }
        }
        
        stage('Publish Results') {
            steps {
                script {
                    echo "Publishing test results..."
                    
                    // Publish Allure Report
                    try {
                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: 'reports/allure-results']]
                        ])
                    } catch (Exception e) {
                        echo "Allure report publishing failed: ${e.getMessage()}"
                    }
                    
                    // Publish HTML Reports
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'reports/extent-report',
                        reportFiles: '*.html',
                        reportName: 'Extent Report',
                        reportTitles: ''
                    ])
                }
            }
        }
    }
    
    post {
        always {
            echo "Pipeline completed"
            
            // Clean up workspace
            script {
                if (params.CLEAN_WORKSPACE) {
                    cleanWs()
                }
            }
        }
        
        success {
            echo "Pipeline completed successfully"
            
            // Send success notification
            script {
                if (env.SEND_NOTIFICATIONS == 'true') {
                    emailext (
                        subject: "Test Execution Successful - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
                            Test execution completed successfully!
                            
                            Environment: ${params.ENVIRONMENT}
                            Browser: ${params.BROWSER}
                            Test Suite: ${params.TEST_SUITE}
                            
                            Build URL: ${env.BUILD_URL}
                            Reports: ${env.BUILD_URL}allure/
                        """,
                        to: "${env.NOTIFICATION_EMAIL}"
                    )
                }
            }
        }
        
        failure {
            echo "Pipeline failed"
            
            // Send failure notification
            script {
                if (env.SEND_NOTIFICATIONS == 'true') {
                    emailext (
                        subject: "Test Execution Failed - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
                            Test execution failed!
                            
                            Environment: ${params.ENVIRONMENT}
                            Browser: ${params.BROWSER}
                            Test Suite: ${params.TEST_SUITE}
                            
                            Build URL: ${env.BUILD_URL}
                            Console Output: ${env.BUILD_URL}console
                        """,
                        to: "${env.NOTIFICATION_EMAIL}"
                    )
                }
            }
        }
        
        unstable {
            echo "Pipeline completed with test failures"
            
            // Send unstable notification
            script {
                if (env.SEND_NOTIFICATIONS == 'true') {
                    emailext (
                        subject: "Test Execution Unstable - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
                            Test execution completed with some failures!
                            
                            Environment: ${params.ENVIRONMENT}
                            Browser: ${params.BROWSER}
                            Test Suite: ${params.TEST_SUITE}
                            
                            Build URL: ${env.BUILD_URL}
                            Reports: ${env.BUILD_URL}allure/
                        """,
                        to: "${env.NOTIFICATION_EMAIL}"
                    )
                }
            }
        }
    }
}