pipeline {
    agent {
        kubernetes {
            // 定义要在 Kubernetes 中运行的 Pod 模板
            yaml '''
            apiVersion: v1
            kind: Pod
            metadata:
            name: jenkins-slave
            namespace: cicd
            labels:
              name: jenkins-slave
            spec:
            containers:
            - name: jnlp
              image: harbor.local.com/cicd/jenkins-agent:v1.0
              resources:
                limits:
                  memory: "512Mi"
                  cpu: "500m"
              securityContext:
                privileged: true
              volumeMounts:
                - name: buildkit
                  mountPath: "/run/buildkit"
                - name: containerd
                  mountPath: "/run/containerd/containerd.sock"
                - name: kube-config
                  mountPath: "/root/.kube/"
                  readOnly: true
            - name: maven
              image: harbor.local.com/cicd/maven:3.9.3
              resources:
                limits:
                  memory: "512Mi"
                  cpu: "500m"
              command:
                - 'sleep'
              args:
                - '9999'
              volumeMounts:
                - name: maven-data
                  mountPath: "/root/.m2"
            - name: buildkitd
              image: harbor.local.com/cicd/buildkit:v0.13.2
              resources:
                limits:
                  memory: "256Mi"
                  cpu: "500m"
              securityContext:
                privileged: true
              volumeMounts:
                - name: buildkit
                  mountPath: "/run/buildkit"
                - name: buildkit-data
                  mountPath: "/var/lib/buildkit"
                - name: containerd
                  mountPath: "/run/containerd/containerd.sock"
            volumes:
              - name: maven-data
                persistentVolumeClaim:
                  claimName: jenkins-maven
              - name: buildkit
                hostPath:
                  path: /run/buildkit
              - name: buildkit-data
                hostPath:
                  path: /var/lib/buildkit
              - name: containerd
                hostPath:
                  path: /run/containerd/containerd.sock
              - name: kube-config
                secret:
                  secretName: kube-config
      '''
        }
    }
    environment {
        // 全局变量
        HARBOR_CRED = "harbor-cuiliang-password"
        IMAGE_NAME = ""
        IMAGE_APP = "demo"
    }
    stages {
        stage('拉取代码') {
            environment {
                // gitlab仓库信息
                GITLAB_CRED = "gitlab-cuiliang-password"
                GITLAB_URL = "http://gitlab.cicd.svc/develop/sprint_boot_demo.git"
            }
            steps {
                echo '开始拉取代码'
                checkout scmGit(branches: [[name: '*/${BRANCH}']], extensions: [], userRemoteConfigs: [[credentialsId: "${GITLAB_CRED}", url: "${GITLAB_URL}"]])
                echo '拉取代码完成'
            }
        }
        stage('编译打包') {
            steps {
                container('maven') {
                    // 指定使用maven container进行打包
                    echo '开始编译打包'
                    sh 'mvn clean package'
                    echo '编译打包完成'
                }
            }
        }
        stage('代码审查') {
            environment {
                // SonarQube信息
                SONARQUBE_SCANNER = "SonarQubeScanner"
                SONARQUBE_SERVER = "SonarQubeServer"
            }
            steps {
                echo '开始代码审查'
                script {
                    def scannerHome = tool "${SONARQUBE_SCANNER}"
                    withSonarQubeEnv("${SONARQUBE_SERVER}") {
                        sh "${scannerHome}/bin/sonar-scanner"
                    }
                }
                echo '代码审查完成'
            }
        }
        stage('构建镜像') {
            environment {
                // harbor仓库信息
                HARBOR_URL = "harbor.local.com"
                HARBOR_PROJECT = "spring_boot_demo"
                // 镜像标签
                IMAGE_TAG = VersionNumber versionPrefix: 'v', versionNumberString: '${BUILD_DATE_FORMATTED, "yyMMdd"}.${BUILDS_TODAY}'
            }
            steps {
                echo '开始构建镜像'
                script {
                    IMAGE_NAME = "${HARBOR_URL}/${HARBOR_PROJECT}/${IMAGE_APP}:${IMAGE_TAG}"
                    sh "nerdctl build --insecure-registry -t ${IMAGE_NAME} . "
                }
                echo '构建镜像完成'
                echo '开始推送镜像'
                // 获取harbor账号密码
                withCredentials([usernamePassword(credentialsId: "${HARBOR_CRED}", passwordVariable: 'HARBOR_PASSWORD', usernameVariable: 'HARBOR_USERNAME')]) {
                    // 登录Harbor仓库
                    sh """nerdctl login --insecure-registry ${HARBOR_URL} -u ${HARBOR_USERNAME} -p ${HARBOR_PASSWORD}
          nerdctl push --insecure-registry ${IMAGE_NAME}"""
                }
                echo '推送镜像完成'
                echo '开始删除镜像'
                script {
                    sh "nerdctl rmi -f ${IMAGE_NAME}"
                }
                echo '删除镜像完成'
            }
        }
        stage('项目部署') {
            environment {
                // 资源清单名称
                YAML_NAME = "k8s.yaml"
            }
            steps {
                echo '开始修改资源清单'
                script {
                    if ("${params.BRANCH}" == 'master') {
                        NAME_SPACE = 'default'
                        DOMAIN_NAME = 'demo.local.com'
                    } else if (params.BRANCH == 'test') {
                        NAME_SPACE = 'test'
                        DOMAIN_NAME = 'demo.test.com'
                    } else {
                        error("Unsupported branch: ${params.BRANCH}")
                    }
                }
                // 使用Content Replace插件进行k8s资源清单内容替换
                contentReplace(configs: [fileContentReplaceConfig(configs: [fileContentReplaceItemConfig(replace: "${IMAGE_NAME}", search: 'IMAGE_NAME'),
                                                                            fileContentReplaceItemConfig(replace: "${NAME_SPACE}", search: 'NAME_SPACE'),
                                                                            fileContentReplaceItemConfig(replace: "${DOMAIN_NAME}", search: 'DOMAIN_NAME')],
                        fileEncoding: 'UTF-8',
                        filePath: "${YAML_NAME}",
                        lineSeparator: 'Unix')])
                echo '修改资源清单完成'
                sh "cat ${YAML_NAME}"
                echo '开始部署资源清单'
                sh "kubectl apply -f ${YAML_NAME}"
                echo '部署资源清单完成'
            }
        }
    }
    post {
        always {
            echo '开始发送邮件通知'
            emailext(subject: '构建通知：${PROJECT_NAME} - Build # ${BUILD_NUMBER} - ${BUILD_STATUS}!',
                    body: '${FILE,path="email.html"}',
                    to: 'cuiliang0302@qq.com')
            echo '邮件通知发送完成'
        }
    }
}