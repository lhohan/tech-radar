#!/bin/bash
set -euxo pipefail

sbt publishLocal

# '0.+' will keep on working until 1.x is released
curl -fLo cs https://git.io/coursier-cli-linux &&
    chmod +x cs &&
    ./cs bootstrap io.github.lhohan::tech-radar-from-csv:0.+ --standalone -o techradar -f

