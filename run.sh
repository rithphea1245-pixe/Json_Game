#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

echo "=== World of Wonder (MVC Game) ==="
mkdir -p bin
javac -d bin $(find src -name "*.java")
java -cp bin com.worldofwonder.Main

