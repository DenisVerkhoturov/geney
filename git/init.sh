#!/usr/bin/env bash

function main {
  set_git_config
  install_hooks
}

# Adds the project-specific configurations to the repository-level
# configuration file.
function set_git_config {
  git config --local include.path "../git/.gitconfig"
}

function install_hooks {
  ./hooks/install.sh
}

main "${@}"
