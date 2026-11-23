#!/bin/bash

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
SERVER_DIR="$ROOT_DIR/llm-server"

# ==============================
# 配置
# ==============================
VERSION=$(mvn -f "$SERVER_DIR/pom.xml" help:evaluate -Dexpression=project.version -q -DforceStdout)
JAR_NAME="llm-server-${VERSION}.jar"

REMOTE_USER="treasure"
REMOTE_HOST="124.222.46.122"
REMOTE_DIR="/opt/llm-gateway"
SERVICE_NAME="llm-gateway"

echo "========== 1. Maven 打包 =========="

mvn -f "$SERVER_DIR/pom.xml" clean package -DskipTests

echo "========== 2. 上传 Jar =========="

scp "$SERVER_DIR/target/$JAR_NAME" \
    "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/llm-gateway.jar"

echo "========== 3. 重启服务 =========="

ssh "$REMOTE_USER@$REMOTE_HOST" \
    "sudo systemctl restart $SERVICE_NAME && sudo systemctl is-active $SERVICE_NAME"

echo "========== 发布完成 =========="