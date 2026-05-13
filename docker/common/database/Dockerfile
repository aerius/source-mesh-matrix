# syntax = docker/dockerfile:1
FROM clickhouse/clickhouse-server:26.3-alpine

ARG DATABASE_NAME
ARG DATABASE_USER
ARG DATABASE_PASSWORD

ENV CLICKHOUSE_DB="${DATABASE_NAME}" \
    CLICKHOUSE_USER="${DATABASE_USER:-aerius}" \
    CLICKHOUSE_PASSWORD="${DATABASE_PASSWORD:-aerius}"

# Copy all SQL initialization scripts
COPY ./source/*.sql /docker-entrypoint-initdb.d/

# Ensure proper permissions
RUN chown -R clickhouse:clickhouse /docker-entrypoint-initdb.d
