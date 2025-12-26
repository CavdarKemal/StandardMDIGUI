#!/bin/bash
# StandardMDIGUI Starter Script
# ==============================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

java -jar lib/standard-mdi-gui-app-1.0-SNAPSHOT.jar "$@"
