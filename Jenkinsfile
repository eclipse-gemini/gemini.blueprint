pipeline {
  agent {
    kubernetes {
      label 'gemini-blueprint-agent-pod'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9.4-eclipse-temurin-8
    command:
    - cat
    tty: true

    env:
    - name: "MAVEN_OPTS"
      value: "-Duser.home=/home/jenkins"
    volumeMounts:
    - name: settings-xml
      mountPath: /home/jenkins/.m2/settings.xml
      subPath: settings.xml
      readOnly: true
    - name: m2-repo
      mountPath: /home/jenkins/.m2/repository

    resources:
      limits:
        memory: "2Gi"
        cpu: "1"
      requests:
        memory: "2Gi"
        cpu: "1"

  volumes:
  - name: settings-xml
    secret:
      secretName: m2-secret-dir
      items:
      - key: settings.xml
        path: settings.xml
  - name: m2-repo
    emptyDir: {}
"""
    }
  }
  stages {
    stage('Build') {
      steps {
        container('maven') {
          sh 'mvn verify -Dmaven.test.failure.ignore=true'
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }
    stage('Build with Equinox profile') {
      steps {
        container('maven') {
          sh 'mvn verify -P it,equinox -Dmaven.test.failure.ignore=true'
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }
    stage('Build with Felix profile') {
      steps {
        container('maven') {
          sh 'mvn verify -P it,felix -Dmaven.test.failure.ignore=true'
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }
    stage('Build with Knopflerfish profile') {
      steps {
        container('maven') {
          sh 'mvn verify -P it,knopflerfish -Dmaven.test.failure.ignore=true'
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }
  }
}
