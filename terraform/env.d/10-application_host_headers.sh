#!/usr/bin/env bash

# Crash on error
set -e

cat << EOF
  application_host_headers = {
    "APP" = "${DEPLOY_WEBHOST}",
  }
EOF
