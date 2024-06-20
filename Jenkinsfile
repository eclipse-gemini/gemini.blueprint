pipeline {
  agent {
    kubernetes {
      inheritFrom 'gemini-blueprint-agent-pod'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9.6-eclipse-temurin-8
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
    stage('Install and execute unit tests') {
      steps {
        container('maven') {
          sh 'mvn install'
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }
    stage('Integration tests with Equinox profile') {
      steps {
        container('maven') {
          dir("integration-tests") {
            sh 'mvn clean install -P equinox'
            junit '**/target/surefire-reports/*.xml'
          }
        }
      }
    }
    stage('Integration tests with Felix profile') {
      steps {
        container('maven') {
          dir("integration-tests") {
            sh 'mvn clean install -P felix'
            junit '**/target/surefire-reports/*.xml'
          }
        }
      }
    }
    stage('Integration tests with Knopflerfish profile') {
      steps {
        container('maven') {
          dir("integration-tests") {
            sh 'mvn clean install -P knopflerfish'
            junit '**/target/surefire-reports/*.xml'
          }
        }
      }
    }
  }
}
