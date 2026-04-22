#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_FILE="${1:-${ROOT_DIR}/docs/DB_SNAPSHOT_SERVICIOS.md}"
POSTGRES_CONTAINER="${ARKAB2B_POSTGRES_CONTAINER:-arkab2b-postgres}"
POSTGRES_USER="${ARKAB2B_POSTGRES_USER:-postgres}"

DBS=(
  "identity_access"
  "directory"
  "arkab2b_catalog"
  "inventory"
  "order"
  "arkab2b_notification"
  "arkab2b_reporting"
)

psql_query() {
  local db="$1"
  local sql="$2"
  docker exec -i "$POSTGRES_CONTAINER" \
    psql -U "$POSTGRES_USER" -d "$db" -At -F $'\t' -c "$sql"
}

load_table_names() {
  local db="$1"
  local line
  LOADED_TABLE_NAMES=()
  while IFS= read -r line; do
    [[ -n "$line" ]] && LOADED_TABLE_NAMES+=("$line")
  done < <(psql_query "$db" "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;")
}

pretty_json() {
  if command -v jq >/dev/null 2>&1; then
    jq .
  else
    cat
  fi
}

if ! docker ps --format '{{.Names}}' | grep -qx "$POSTGRES_CONTAINER"; then
  echo "Postgres container '$POSTGRES_CONTAINER' is not running." >&2
  exit 1
fi

mkdir -p "$(dirname "$OUTPUT_FILE")"
TMP_FILE="$(mktemp)"
trap 'rm -f "$TMP_FILE"' EXIT

{
  echo "# Snapshot de tablas con datos (Postgres ArkaB2B)"
  echo
  echo "Generado: $(date '+%Y-%m-%d %H:%M:%S %z')"
  echo
  echo "## Resumen ejecutivo"
  echo
  echo "| Base de datos | Tablas con datos | Filas totales |"
  echo "|---|---:|---:|"

  for db in "${DBS[@]}"; do
    load_table_names "$db"
    non_empty_tables=()
    non_empty_table_count=0
    total_rows=0

    for table in "${LOADED_TABLE_NAMES[@]}"; do
      count="$(psql_query "$db" "SELECT COUNT(*) FROM public.\"${table}\";")"
      if [[ "$count" != "0" ]]; then
        non_empty_tables+=("${table}:${count}")
        non_empty_table_count=$((non_empty_table_count + 1))
        total_rows=$((total_rows + count))
      fi
    done

    echo "| \
\`${db}\` | ${non_empty_table_count} | ${total_rows} |"
  done

  echo

  for db in "${DBS[@]}"; do
    echo "## ${db}"
    echo
    load_table_names "$db"
    non_empty_tables=()
    non_empty_table_count=0
    total_rows=0

    for table in "${LOADED_TABLE_NAMES[@]}"; do
      count="$(psql_query "$db" "SELECT COUNT(*) FROM public.\"${table}\";")"
      if [[ "$count" != "0" ]]; then
        non_empty_tables+=("${table}:${count}")
        non_empty_table_count=$((non_empty_table_count + 1))
        total_rows=$((total_rows + count))
      fi
    done

    echo "### Tablas con datos"
    echo
    echo "| # | Tabla | Filas |"
    echo "|---:|---|---:|"

    if (( non_empty_table_count == 0 )); then
      echo "| 1 | _sin tablas con datos_ | 0 |"
    else
      index=1
      for entry in "${non_empty_tables[@]}"; do
        table="${entry%%:*}"
        count="${entry##*:}"
        echo "| ${index} | \`public.${table}\` | ${count} |"
        index=$((index + 1))
      done
    fi

    echo
    echo "### Muestras por tabla"
    echo

    if (( non_empty_table_count == 0 )); then
      echo "_No hay muestras porque no hay tablas con datos._"
      echo
    else
      for entry in "${non_empty_tables[@]}"; do
        table="${entry%%:*}"
        count="${entry##*:}"
        sample_json="$(psql_query "$db" "SELECT row_to_json(t) FROM (SELECT * FROM public.\"${table}\" LIMIT 1) t;")"
        echo "<details>"
        echo "<summary><strong>\`public.${table}\`</strong> — ${count} filas</summary>"
        echo
        echo '```json'
        printf '%s\n' "$sample_json" | pretty_json
        echo '```'
        echo
        echo "</details>"
        echo
      done
    fi

    echo
  done
} > "$TMP_FILE"

mv "$TMP_FILE" "$OUTPUT_FILE"
echo "Snapshot written to $OUTPUT_FILE"
