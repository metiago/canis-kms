#!/bin/sh
set -eu

if [ -z "${RUNNER_TEMP:-}" ] || [ -z "${GITHUB_ENV:-}" ]; then
    echo 'This fixture setup requires RUNNER_TEMP and GITHUB_ENV.' >&2
    exit 1
fi

fixture_root=$(mktemp -d "$RUNNER_TEMP/canis-owner-XXXXXX")
file_owner_key="$fixture_root/root-owned.key"
directory_owner="$fixture_root/other-owned"
directory_owner_key="$directory_owner/master.key"

printf '%s\n' 'AAAAAAAAAAAAAAAAAAAAAA==' > "$file_owner_key"
chmod 600 "$file_owner_key"
sudo chown root:root "$file_owner_key"

mkdir -m 700 "$directory_owner"
printf '%s\n' 'AAAAAAAAAAAAAAAAAAAAAA==' > "$directory_owner_key"
chmod 600 "$directory_owner_key"
sudo chown nobody:nogroup "$directory_owner"
sudo chmod 711 "$directory_owner"

test "$(stat -c %U "$file_owner_key")" = root
test "$(stat -c %U "$directory_owner")" = nobody
test "$(stat -c %U "$directory_owner_key")" = "$(id -un)"

printf 'CANIS_TEST_POSIX_OTHER_OWNER_KEY=%s\n' "$file_owner_key" >> "$GITHUB_ENV"
printf 'CANIS_TEST_POSIX_OTHER_OWNER_DIRECTORY_KEY=%s\n' "$directory_owner_key" >> "$GITHUB_ENV"
