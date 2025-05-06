pipeline {
    agent any

    environment {
	DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credentials')
	DOCKER_IMAGE = 'b3nch4n/teedy-app'
	DOCKER_TAG = "${env.BUILD_NUMBER}"
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

//	stage('Upload image') {
//	    steps {
//		script {
//		    docker.withRegistry('https://crpi-0i4dp2dcpc0rbado.cn-shenzhen.personal.cr.aliyuncs.com', 'dockerhub_credentials') {
//		        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
//			docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
//		    }
//		}
//	    }
//	}

	stage('Run containers') {
	    steps {
		script {
		    sh 'docker stop teedy-container-8081 || true'
		    sh 'docker rm teedy-container-8081 || true'

		    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
		        '--name teedy-container-8081 -d -p 8081:8080'
		    )

		    sh 'docker stop teedy-container-8082 || true'
		    sh 'docker rm teedy-container-8082 || true'

		    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
		        '--name teedy-container-8082 -d -p 8082:8080'
		    )

		    sh 'docker ps --filter "name=teedy-container"'
		}
	    }
	}
    }
}
