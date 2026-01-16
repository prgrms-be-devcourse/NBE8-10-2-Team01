pipeline {
    agent any

    environment {
        DOCKER_IMAGE_NAME = 'plog:latest'
        DOCKER_IMAGE_FILE = 'plog.tar.gz'

        // Config‑file‑management 플러그인에 등록한 파일 ID
        ENV_FILE_ID       = 'plog-file'
    }

    stages {
        /* 1) 환경 변수 로드 ---------------------------------------------- */
        stage('Init Env') {
            steps {
                configFileProvider([configFile(fileId: ENV_FILE_ID, variable: 'ENV_SRC')]) {
                    script {
                        // 1) 워크스페이스에 .env 파일로 복사
                        sh 'cp "$ENV_SRC" "$WORKSPACE/.env"'

                        // 2) 전역 변수로 경로 보존
                        env.ENV_FILE_PATH = "$WORKSPACE/.env"

                        // 3) 프로퍼티 로드 → env 주입
                        readProperties(file: env.ENV_FILE_PATH).each { k, v ->
                            env."$k" = v
                        }
                    }
                }
            }
        }

        /* 2) Git 체크아웃 -------------------------------------------------- */
        stage('CheckOut') { steps { checkout scm } }


        /* 3 + 4) 테스트 와 빌드  --------------------------------------------- */
        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
            }
        }

        /* 5) Docker 이미지 빌드 ------------------------------------------- */
        stage('Build Docker Image') { steps { sh "docker build -t ${DOCKER_IMAGE_NAME} ." } }

        /* 6) 이미지 아카이브 ---------------------------------------------- */
        stage('Save Docker Image') {
            steps {
                sh "docker save ${DOCKER_IMAGE_NAME} | gzip > ${DOCKER_IMAGE_FILE}"
                archiveArtifacts artifacts: "${DOCKER_IMAGE_FILE}"
            }
        }

        /* 7) 로컬 배포 ---------------------------------------------------- */
        stage('Deploy Docker Image Locally') {
            steps {
                script {
                    sh "docker load -i ${DOCKER_IMAGE_FILE}"
                    sh "docker stop plog-container || true"
                    sh "docker rm   plog-container || true"

                    // prod 프로필로 컨테이너 실행. 모든 env 는 env‑file 로 전달
                    sh """
                        docker run -d --name plog-container -p 4040:8080 \\
                          --env-file ${env.ENV_FILE_PATH} \\
                          --network docker_backend \\
                          ${DOCKER_IMAGE_NAME}
                    """

                    sh "docker system prune -f"
                }
            }
        }
    }

    /* 8) 성공/실패 Discord 알림 (기존 코드 그대로) ------------------------- */
    post {
        success {
            script {
                def branch = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                def commitMsg = sh(script: "git log -1 --pretty=%s", returnStdout: true).trim()
                def author = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
                def shortSha = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()

                def jenkinsUrl = "http://jack8226.ddns.net:3005/job/plog/${env.BUILD_NUMBER}/"
                def reportUrl = "${jenkinsUrl}execution/node/3/ws/build/reports/tests/test/index.html"


                def desc = """
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    📦 plog Jenkins Pipeline

    ✅ 빌드 성공

    🌿 브랜치: ${branch}
    ✍️ 커밋 메시지:
          ${commitMsg}
    🧑‍💻 작성자: ${author}
    🔗 SHA: ${shortSha}

    📄 테스트 리포트 보기: ${reportUrl}
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    """.stripIndent().trim()

                discordSend(
                    description: desc,
                    link: jenkinsUrl,
                    result: currentBuild.currentResult,
                    title: "📦 plog Jenkins Pipeline",
                    webhookURL: env.DISCORD_WEBHOOK
                )
            }
        }

        failure {
            script {
                def branch = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                def commitMsg = sh(script: "git log -1 --pretty=%s", returnStdout: true).trim()
                def author = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
                def shortSha = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()

                def jenkinsUrl = "http://jack8226.ddns.net:3005/job/plog/${env.BUILD_NUMBER}/"
                def reportUrl = "${jenkinsUrl}execution/node/3/ws/build/reports/tests/test/index.html"

                def desc = """
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    📦 plog Jenkins Pipeline

    ❌ 빌드 실패

    🌿 브랜치: ${branch}
    ✍️ 커밋 메시지:
          ${commitMsg}
    🧑‍💻 작성자: ${author}
    🔗 SHA: ${shortSha}

    📄 테스트 리포트 보기: ${reportUrl}
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    """.stripIndent().trim()

                discordSend(
                    description: desc,
                    link: jenkinsUrl,
                    result: currentBuild.currentResult,
                    title: "📦 plog Jenkins Pipeline",
                    webhookURL: env.DISCORD_WEBHOOK
                )
            }
        }
    }
}
