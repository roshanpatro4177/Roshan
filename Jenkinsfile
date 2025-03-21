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
					if [ ! -d "${MANIFEST_REPO}/.git" ]; then
						echo "Cloning the manifest repo..."
						git clone https://${GIT_USER}:${GIT_PASS}@github.com/roshanpatro4177/manifest-repo.git ${MANIFEST_REPO}
					fi

					cd ${MANIFEST_REPO}  # Ensure we are inside the correct Git directory

					git config user.email "jenkins@localhost"
					git config user.name "Jenkins"

					# Securely update remote URL to avoid exposing credentials in logs
					git remote set-url origin https://${GIT_USER}:${GIT_PASS}@github.com/roshanpatro4177/manifest-repo.git

					git add .
					
					# Check if there are changes before committing
					if ! git diff --staged --quiet; then
						git commit -m 'Update image tag to ${BUILD_NUMBER}'
						git push origin main
					else
						echo "No changes to commit, skipping push."
					fi
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
