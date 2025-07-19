#!/bin/bash

# This script starts a PostgreSQL Docker container and a Keycloak Docker container.
# It assumes Docker is installed and running on your Mac.

# --- Configuration Variables ---
# Define variables for common values to make the script easier to modify.
POSTGRES_USER="quarkus"
POSTGRES_PASSWORD="quarkus"
POSTGRES_DB="quarkusdb"
POSTGRES_PORT="5432"
POSTGRES_IMAGE="postgres:16"
POSTGRES_CONTAINER_NAME="quarkus_test"

KEYCLOAK_ADMIN="admin"
KEYCLOAK_ADMIN_PASSWORD="admin"
KEYCLOAK_PORT="8180"
KEYCLOAK_IMAGE="quay.io/keycloak/keycloak:26.0"
KEYCLOAK_CONTAINER_NAME="keycloak"

REALM_FILE_NAME="quarkus-realm-realm.json"
REALM_FILE_PATH="$(pwd)/${REALM_FILE_NAME}"


# --- Functions for better organization ---

# Function to check if a Docker container exists and is running
check_container_status() {
  local container_name=$1
  if docker ps -f name="${container_name}" --format "{{.Names}}" | grep -q "${container_name}"; then
    echo "✅ Docker container '${container_name}' is already running."
    return 0 # Container is running
  elif docker ps -a -f name="${container_name}" --format "{{.Names}}" | grep -q "${container_name}"; then
    echo "⚠️ Docker container '${container_name}' exists but is not running. Attempting to start it..."
    docker start "${container_name}"
    sleep 5 # Give it a moment to start
    if docker ps -f name="${container_name}" --format "{{.Names}}" | grep -q "${container_name}"; then
      echo "✅ Docker container '${container_name}' started successfully."
      return 0
    else
      echo "❌ Failed to start Docker container '${container_name}'. Please check Docker logs."
      return 1
    fi
  else
    echo "ℹ️ Docker container '${container_name}' does not exist. Will create it."
    return 2 # Container does not exist
  fi
}

# Function to stop and remove a Docker container
stop_and_remove_container() {
  local container_name=$1
  echo "Attempting to stop and remove existing container: ${container_name}"
  docker stop "${container_name}" &>/dev/null
  docker rm "${container_name}" &>/dev/null
  if [ $? -eq 0 ]; then
    echo "🗑️ Successfully stopped and removed '${container_name}'."
  else
    echo "ℹ️ Container '${container_name}' was not running or did not exist to remove."
  fi
}

# Function to start Docker Desktop on macOS
start_docker_desktop() {
  echo "--- Checking Docker Desktop Status ---"
  if ! docker info &>/dev/null; then
    echo "⚠️ Docker Desktop is not running. Attempting to start it..."
    open -a Docker
    # Wait for Docker to start. This can take a while.
    # We'll loop and check `docker info` until it's ready.
    local timeout=60
    local elapsed=0
    while ! docker info &>/dev/null && [ ${elapsed} -lt ${timeout} ]; do
      echo "Waiting for Docker Desktop to fully start... (${elapsed}/${timeout}s)"
      sleep 5
      elapsed=$((elapsed + 5))
    done

    if docker info &>/dev/null; then
      echo "✅ Docker Desktop started successfully."
    else
      echo "❌ Failed to start Docker Desktop within the timeout. Please start it manually."
      exit 1
    fi
  else
    echo "✅ Docker Desktop is already running."
  fi
}

# --- Main Script Execution ---

# Start Docker Desktop if not running
start_docker_desktop

# --- Start PostgreSQL ---
echo "--- Starting PostgreSQL Container ---"
stop_and_remove_container "${POSTGRES_CONTAINER_NAME}" # Ensure a clean start

echo "Attempting to start PostgreSQL container: ${POSTGRES_CONTAINER_NAME}"
docker run \
  -d \
  --ulimit memlock=-1:-1 \
  --rm=true \
  --memory-swappiness=0 \
  --name "${POSTGRES_CONTAINER_NAME}" \
  -e POSTGRES_USER="${POSTGRES_USER}" \
  -e POSTGRES_PASSWORD="${POSTGRES_PASSWORD}" \
  -e POSTGRES_DB="${POSTGRES_DB}" \
  -p "${POSTGRES_PORT}:${POSTGRES_PORT}" \
  "${POSTGRES_IMAGE}"

if [ $? -eq 0 ]; then
  echo "✅ PostgreSQL container '${POSTGRES_CONTAINER_NAME}' started successfully."
else
  echo "❌ Failed to start PostgreSQL container. Please check Docker logs for errors."
  exit 1
fi

# Give PostgreSQL some time to initialize
echo "Waiting for PostgreSQL to initialize (approx. 10 seconds)..."
sleep 10


# --- Start Keycloak ---
echo ""
echo "--- Starting Keycloak Container ---"

# Check if the realm file exists before attempting to mount it
if [ ! -f "${REALM_FILE_PATH}" ]; then
  echo "❌ Error: Keycloak realm file not found at '${REALM_FILE_PATH}'."
  echo "Please ensure 'quarkus-realm-realm.json' is in the correct directory or update REALM_FILE_PATH in the script."
  exit 1
fi

stop_and_remove_container "${KEYCLOAK_CONTAINER_NAME}" # Ensure a clean start

echo "Attempting to start Keycloak container: ${KEYCLOAK_CONTAINER_NAME}"
docker run \
  -d \
  --rm \
  --name "${KEYCLOAK_CONTAINER_NAME}" \
  -p "${KEYCLOAK_PORT}:8080" \
  -e KEYCLOAK_ADMIN="${KEYCLOAK_ADMIN}" \
  -e KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD}" \
  -e KC_FEATURES="scripts" \
  -v "${REALM_FILE_PATH}:/opt/keycloak/data/import/quarkus-realm-realm.json" \
  "${KEYCLOAK_IMAGE}" \
  start-dev --import-realm

if [ $? -eq 0 ]; then
  echo "✅ Keycloak container '${KEYCLOAK_CONTAINER_NAME}' started successfully."
else
  echo "❌ Failed to start Keycloak container. Please check Docker logs for errors."
  exit 1
fi

echo ""
echo "--- All containers started. ---"
echo "PostgreSQL should be accessible on port ${POSTGRES_PORT}."
echo "Keycloak should be accessible on http://localhost:${KEYCLOAK_PORT}."
echo "Keycloak Admin UI: http://localhost:${KEYCLOAK_PORT}/admin"

# Add a delay to allow Keycloak to fully initialize before Quarkus tries to connect
echo "Waiting for Keycloak to fully initialize (approx. 15 seconds)..."
sleep 15 # Increased sleep time for Keycloak initialization

echo ""
echo "--- Starting Quarkus Application in Dev Mode ---"
echo "Note: This will run 'mvn quarkus:dev' in the current terminal."
echo "To stop Quarkus and the script, press Ctrl+C."
echo "If you want to run this in a new terminal, you can open a new terminal and run 'mvn quarkus:dev' manually."
mvn quarkus:dev
