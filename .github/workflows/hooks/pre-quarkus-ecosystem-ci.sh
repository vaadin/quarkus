#!/usr/bin/env bash
set -euo pipefail

# Install Chrome for UI tests
wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo apt-get install -y ./google-chrome-stable_current_amd64.deb
rm google-chrome-stable_current_amd64.deb

# Set up Vaadin TestBench license
if [ -n "${TB_LICENSE:-}" ]; then
    mkdir -p ~/.vaadin/
    echo "{\"username\":\"$(echo "$TB_LICENSE" | cut -d / -f1)\",\"proKey\":\"$(echo "$TB_LICENSE" | cut -d / -f2)\"}" > ~/.vaadin/proKey
fi
