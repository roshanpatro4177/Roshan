pipeline {
    agent any

    environment {
        REPO_NAME = "roshanpatro/spring-boot-app"
        MANIFEST_REPO = "/home/ubuntu/manifest-repo/manifest-repo"
    }

    stages {
        stage('Install jq') {
            steps {
                sh '''
                if ! command -v jq &> /dev/null
                then
                    echo "Installing jq..."
                    apt-get update && apt-get install -y jq
                fi
                /usr/bin/jq --version
                '''
            }
        }

        stage('Get Latest Image Version') {
            steps {
                script {
                    def latestTag = sh(
                        script: """
                        curl -s https://hub.docker.com/v2/repositories/${REPO_NAME}/tags | \
                        /usr/bin/jq -r '[.results[].name | select(test("^[0-9]+\$"))] | max' || echo "0"
                        """,
                        returnStdout: true
                    ).trim()

                    def nextVersion = latestTag.isInteger() ? (latestTag.toInteger() + 1) : 1
                    env.DOCKER_IMAGE = "${REPO_NAME}:${nextVersion}"
                    echo "Next image version: ${env.DOCKER_IMAGE}"
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
                withCredentials([string(credentialsId: 'git-token', variable: 'GIT_TOKEN')]) {
                    sh """
                    if [ ! -d "${MANIFEST_REPO}/.git" ]; then
                        git clone https://${GIT_TOKEN}@github.com/roshanpatro4177/manifest-repo.git ${MANIFEST_REPO}
                    fi

                    cd ${MANIFEST_REPO}

                    git config user.email "jenkins@localhost"
                    git config user.name "Jenkins"

                    # Force overwrite local files with the latest from GitHub
                    git fetch origin main
                    git reset --hard origin/main

                    # Modify deployment.yaml with the new image tag
                    sed -i "s|image: roshanpatro/spring-boot-app:.*|image: ${env.DOCKER_IMAGE}|" deployment.yaml

                    # Check if there are any changes before committing
                    if ! git diff --quiet deployment.yaml; then
                        git add deployment.yaml
                        git commit -m 'Update image tag to ${env.DOCKER_IMAGE}'
                        git push https://${GIT_TOKEN}@github.com/roshanpatro4177/manifest-repo.git main
                    else
                        echo "No changes in deployment.yaml, skipping commit."
                    fi
                    """
                }
            }
        }
    }
}
