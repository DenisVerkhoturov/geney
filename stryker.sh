#!/usr/bin/env bash

for module in assembler utils cli; do
  echo "stryker4s {base-dir: $module, reporters: [\"console\", \"dashboard\", \"html\"], dashboard.module=\"$module\"}" >stryker4s.conf
  if ! sbt "project $module" stryker; then
    exit
  fi
done
