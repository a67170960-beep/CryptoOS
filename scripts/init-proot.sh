#!/usr/bin/env bash
set -euo pipefail

# Bootstrap a Debian PRoot environment in Termux. This does not install
# account/email/phone enumeration tools and is not called by the Android app.
if [[ -z "${PREFIX:-}" || ! -x "${PREFIX}/bin/pkg" ]]; then
  echo "Run this script from Termux on Android." >&2
  exit 1
fi

pkg update -y
pkg install -y proot-distro

if ! proot-distro login debian -- /bin/true >/dev/null 2>&1; then
  proot-distro install debian
fi

proot-distro login debian --user root -- bash -lc '
  set -euo pipefail
  export DEBIAN_FRONTEND=noninteractive
  apt-get update
  apt-get install -y --no-install-recommends \
    ca-certificates curl git python3 python3-venv python3-pip \
    libimage-exiftool-perl python3-requests python3-bs4
  apt-get clean
  rm -rf /var/lib/apt/lists/*
'

echo "Debian PRoot is ready. Start it with: proot-distro login debian"
echo "This bootstrap does not configure a Wayland-to-Android window bridge."
