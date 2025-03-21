pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "roshanpatro4177/spring-boot-app:${BUILD_NUMBER}"
        MANIFEST_REPO = "/home/ubuntu/manifest-repo/manifest-repo"
    }

    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'main', url: 'https://github.com/roshanpatro4177/Roshan.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Push Docker Image') {
            steps {
                sh "docker login -u your-username -p your-password"
                sh "docker push ${DOCKER_IMAGE}"
            }
        }

        stage('Update Kubernetes Manifest') {
            steps {
                script {
                    sh "sed -i 's|image: .*|image: ${DOCKER_IMAGE}|' ${MANIFEST_REPO}/deployment.yaml"
                    sh "git -C ${MANIFEST_REPO} add ."
                    sh "git -C ${MANIFEST_REPO} commit -m 'Update image tag to ${BUILD_NUMBER}'"
                    sh "git -C ${MANIFEST_REPO} push origin main"
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "kubectl apply -f ${MANIFEST_REPO}/deployment.yaml"
            }
        }
    }
}
 
