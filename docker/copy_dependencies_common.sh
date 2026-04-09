#!/usr/bin/env bash

SOURCE_DIR='../source'
DOCKER_COMMON_DIR='common'

# Exit on error
set -e

# Change current directory to directory of script so it can be called from everywhere
SCRIPT_PATH=$(readlink -f "${0}")
SCRIPT_DIR=$(dirname "${SCRIPT_PATH}")
cd "${SCRIPT_DIR}"

# include functions
source "${CICD_SCRIPTS_DIR}"/docker/images/v1.0/functions.sh

# api
if _cicd_is_module_enabled "${1}" 'api'; then
  cp -auv "${SOURCE_DIR}"/api/target/smm-api-*.jar \
    "${DOCKER_COMMON_DIR}"/api/app.jar
fi

# Change current directory to previous one so scripts calling this one can function properly
cd - > /dev/null
