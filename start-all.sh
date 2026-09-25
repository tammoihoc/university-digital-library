#!/bin/bash
# ==============================================================
# start-all.sh — Quản lý khởi động các Spring Boot service
#
# CÁCH DÙNG:
#   ./start-all.sh            -> khởi động TẤT CẢ service (cách nhau vài giây)
#   ./start-all.sh list       -> liệt kê service và số thứ tự của chúng
#   ./start-all.sh 3          -> chỉ khởi động service số 3
#   ./start-all.sh stop       -> dừng TẤT CẢ service đang chạy
#   ./start-all.sh stop 3     -> chỉ dừng service số 3
#   ./start-all.sh status     -> xem service nào đang chạy/đang tắt
#
# LOGIC:
#   - Tự động quét các thư mục con trong BACKEND_DIR có file pom.xml,
#     coi mỗi thư mục là một service (không cần khai tay tên từng service).
#   - Trước khi chạy, so sánh thời gian sửa đổi mới nhất trong src/ với
#     thời gian tạo file .jar trong target/. Nếu code mới hơn jar (hoặc
#     chưa có jar) -> tự build lại bằng ./mvnw clean package -DskipTests.
#     Nếu code không đổi -> bỏ qua build, chạy thẳng jar có sẵn cho nhanh.
#   - Nếu service đang chạy rồi (theo dõi qua file PID) -> không khởi
#     động lại, tránh chạy trùng 2 instance của cùng 1 service.
# ==============================================================

set -uo pipefail

# ---- CẤU HÌNH: sửa đường dẫn này cho đúng máy bạn ----
BACKEND_DIR="$HOME/university-digital-library/backend"
JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
STAGGER_SECONDS=5 # thời gian chờ giữa các lần khởi động khi chạy "tất cả"
# Tên các thư mục KHÔNG phải service chạy được (thư viện dùng chung, v.v.)
EXCLUDE_NAMES=("common-service" "common" "common-library")
# --------------------------------------------------------

PID_DIR="/tmp/spring-services-pids"
LOG_DIR="/tmp/spring-services-logs"
mkdir -p "$PID_DIR" "$LOG_DIR"

if [[ ! -d "$BACKEND_DIR" ]]; then
  echo "LỖI: Không tìm thấy thư mục $BACKEND_DIR — sửa biến BACKEND_DIR trong script."
  exit 1
fi

mapfile -t ALL_DIRS < <(find "$BACKEND_DIR" -maxdepth 1 -mindepth 1 -type d -exec test -f "{}/pom.xml" \; -print | sort)

is_excluded() {
  local name="$1" ex
  for ex in "${EXCLUDE_NAMES[@]}"; do
    [[ "$name" == "$ex" ]] && return 0
  done
  return 1
}

SERVICES=()
for d in "${ALL_DIRS[@]}"; do
  is_excluded "$(basename "$d")" || SERVICES+=("$d")
done

if [[ ${#SERVICES[@]} -eq 0 ]]; then
  echo "LỖI: Không tìm thấy service nào (thư mục con có pom.xml) trong $BACKEND_DIR"
  exit 1
fi

is_running() {
  local pidfile="$1"
  [[ -f "$pidfile" ]] && kill -0 "$(cat "$pidfile" 2>/dev/null)" 2>/dev/null
}

list_services() {
  echo "Tìm thấy ${#SERVICES[@]} service trong $BACKEND_DIR:"
  for i in "${!SERVICES[@]}"; do
    local name status
    name=$(basename "${SERVICES[$i]}")
    if is_running "$PID_DIR/$name.pid"; then
      status="đang chạy (PID $(cat "$PID_DIR/$name.pid"))"
    else
      status="đang tắt"
    fi
    printf "  %d) %-25s [%s]\n" "$((i + 1))" "$name" "$status"
  done
}

needs_build() {
  local dir="$1" jar="$2"
  [[ ! -f "$jar" ]] && return 0
  local newest_src
  newest_src=$(find "$dir/src" -type f -newer "$jar" 2>/dev/null | head -n 1)
  [[ -n "$newest_src" ]]
}

find_jar() {
  local dir="$1"
  find "$dir/target" -maxdepth 1 -name "*.jar" ! -name "*.jar.original" 2>/dev/null | head -n 1
}

start_service() {
  local dir="$1"
  local name pidfile logfile jar
  name=$(basename "$dir")
  pidfile="$PID_DIR/$name.pid"
  logfile="$LOG_DIR/$name.log"

  if is_running "$pidfile"; then
    echo "[$name] đã đang chạy (PID $(cat "$pidfile")), bỏ qua."
    return
  fi

  jar=$(find_jar "$dir")

  if [[ -z "$jar" ]] || needs_build "$dir" "$jar"; then
    echo "[$name] code mới hoặc chưa build -> đang build..."
    if ! (cd "$dir" && ./mvnw -q clean package -DskipTests); then
      echo "[$name] LỖI: build thất bại, xem log Maven ở trên. Bỏ qua service này."
      return
    fi
    jar=$(find_jar "$dir")
  else
    echo "[$name] code không đổi -> dùng jar có sẵn, bỏ qua build."
  fi

  if [[ -z "$jar" ]]; then
    echo "[$name] LỖI: build xong nhưng không tìm thấy file .jar trong target/."
    return
  fi

  echo "[$name] đang khởi động..."
  nohup java $JAVA_OPTS -jar "$jar" --spring.output.ansi.enabled=never \
    >"$logfile" 2>&1 &
  echo $! >"$pidfile"
  sleep 1
  if is_running "$pidfile"; then
    echo "[$name] OK, PID $(cat "$pidfile"), log: $logfile"
  else
    echo "[$name] LỖI: tiến trình thoát ngay sau khi chạy, xem log: $logfile"
  fi
}

stop_service() {
  local dir="$1"
  local name pidfile
  name=$(basename "$dir")
  pidfile="$PID_DIR/$name.pid"

  if is_running "$pidfile"; then
    echo "[$name] đang dừng (PID $(cat "$pidfile"))..."
    kill "$(cat "$pidfile")"
    rm -f "$pidfile"
  else
    echo "[$name] không chạy, bỏ qua."
    rm -f "$pidfile"
  fi
}

resolve_index() {
  local n="$1"
  if ! [[ "$n" =~ ^[0-9]+$ ]] || ((n < 1 || n > ${#SERVICES[@]})); then
    echo "LỖI: số thứ tự phải từ 1 đến ${#SERVICES[@]}" >&2
    return 1
  fi
  echo "${SERVICES[$((n - 1))]}"
}

# ---------------- Main ----------------
cmd="${1:-}"

case "$cmd" in
"")
  echo "Khởi động tất cả ${#SERVICES[@]} service (cách nhau ${STAGGER_SECONDS}s)..."
  for dir in "${SERVICES[@]}"; do
    start_service "$dir"
    sleep "$STAGGER_SECONDS"
  done
  ;;
list)
  list_services
  ;;
status)
  list_services
  ;;
stop)
  if [[ -n "${2:-}" ]]; then
    dir=$(resolve_index "$2") || exit 1
    stop_service "$dir"
  else
    echo "Đang dừng tất cả service..."
    for dir in "${SERVICES[@]}"; do
      stop_service "$dir"
    done
  fi
  ;;
[0-9]*)
  dir=$(resolve_index "$cmd") || exit 1
  start_service "$dir"
  ;;
*)
  echo "Dùng: $0 [list|status|stop [số]| số thứ tự 1-${#SERVICES[@]} | (không có gì = chạy hết)]"
  list_services
  ;;
esac
