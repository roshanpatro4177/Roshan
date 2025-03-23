pipeline {
    agent any

    environment {
        REPO_NAME = "roshanpatro/spring-boot-app"
        MANIFEST_REPO = "/home/ubuntu/manifest-repo/manifest-repo"
    }

    stages {
        stage('Get Latest Image Version') {
            steps {
                script {
                    def latestTag = sh(script: "curl -s https://hub.docker.com/v2/repositories/${REPO_NAME}/tags | jq -r '.results | map(.name | tonumber?) | max'", returnStdout: true).trim()
                    def nextVersion = latestTag.isNumber() ? (latestTag.toInteger() + 1) : 1
                    env.DOCKER_IMAGE = "${REPO_NAME}:${nextVersion}"
                }
            }
        }

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
                sh "docker build -t ${env.DOCKER_IMAGE} ."
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh "echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin"
                    sh "docker push ${env.DOCKER_IMAGE}"
                }
            }
        }

        stage('Update Kubernetes Manifest') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'git-creds', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                    sh """
                    if [ ! -d "${MANIFEST_REPO}/.git" ]; then
                        git clone https://${GIT_USER}:${GIT_PASS}@github.com/roshanpatro4177/manifest-repo.git ${MANIFEST_REPO}
                    fi

                    cd ${MANIFEST_REPO}

                    git config user.email "jenkins@localhost"
                    git config user.name "Jenkins"

                    sed -i 's|image: roshanpatro/spring-boot-app:.*|image: ${env.DOCKER_IMAGE}|' deployment.yaml

                    git add deployment.yaml
                    git commit -m 'Update image tag to ${env.DOCKER_IMAGE}'
                    git push origin main
                    """
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
