pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "roshanpatro/spring-boot-app:${BUILD_NUMBER}"
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
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh "echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin"
                    sh "docker push ${DOCKER_IMAGE}"
                }
            }
        }

        stage('Update Kubernetes Manifest') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'git-creds', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                    sh """
                    git -C ${MANIFEST_REPO} config user.email "jenkins@localhost"
                    git -C ${MANIFEST_REPO} config user.name "Jenkins"
                    git -C ${MANIFEST_REPO} add .
                    git -C ${MANIFEST_REPO} commit -m 'Update image tag to ${BUILD_NUMBER}'
                    git -C ${MANIFEST_REPO} push https://${GIT_USER}:${GIT_PASS}@github.com/roshanpatro4177/manifest-repo.git main
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "microk8s.kubectl apply -f ${MANIFEST_REPO}/deployment.yaml"
            }
        }
    }
}
