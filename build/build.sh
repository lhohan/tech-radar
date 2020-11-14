#!/bin/bash
set -euxo pipefail

# Run tests
sbt test
# Run quality checks in single step for some speed up
sbt scalafmtCheckAll coverage test coverageReport

