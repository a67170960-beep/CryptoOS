#!/usr/bin/env bash
set -euo pipefail

# Privacy-oriented file inspection dependencies for Debian PRoot.
# Intentionally excluded: automated username, email, and phone-number
# enumeration or account-discovery tools.
if [[ "$(id -u)" -ne 0 ]]; then
  echo "Run this script as root inside the Debian PRoot session." >&2
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y --no-install-recommends \
  libimage-exiftool-perl python3-requests python3-bs4
apt-get clean
rm -rf /var/lib/apt/lists/*

echo "Installed local metadata and HTML parsing utilities."
echo "No account, email, or phone-number discovery tools were installed."
