pipeline {
    agent any

    environment {
	DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credentials')
	DOCKER_IMAGE = 'b3nch4n/teedy-app'
	DOCKER_TAG = "latest"
	DEPLOYMENT_NAME = "teedy"
	CONTAINER_NAME = "teedy-app"
    }

    stages {
	stage('Build') {
	    steps {
		checkout scmGit(
		     branches: [[name: '*/vanilla']],
	 	     extensions: [],
	 	     userRemoteConfigs: [[url: 'https://github.com/chanbengz/Teedy.git']]
	 	)
	        sh 'mvn -B -DskipTests clean package'
	    }
	}

	stage('Building image') {
	    steps {
	         script {
		     docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
	         }
	    }
	}

        stage('Start Minikube'){
            steps {
                sh '''
                    if ! minikube status | grep -q "Running"; then
                        echo "Starting Minikube..."
                        minikube start
                    else
                        echo "Minikube already running."
                    fi
                '''
            }
        }

        stage('Set Image'){
            steps {
                sh '''
                    echo "Setting image for deployment..."
                    kubectl set image deployment/${DEPLOYMENT_NAME} ${CONTAINER_NAME}=${DOCKER_IMAGE}:${DOCKER_TAG}
                '''
                }
        }

        stage('Verify'){
            steps {
                sh 'kubectl rollout status deployment/${DEPLOYMENT_NAME}'
                sh 'kubectl get pods'
            }
        }
    }
}
