#!/bin/bash

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
STARTER_DIR="$ROOT_DIR/llm-gateway-starter"

# ==============================
# 配置
# ==============================
VERSION=$(mvn -f "$STARTER_DIR/pom.xml" help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "========== 1. 发布 llm-gateway-starter ${VERSION} =========="

mvn -f "$STARTER_DIR/pom.xml" clean deploy -DskipTests

echo "========== 发布完成 =========="
