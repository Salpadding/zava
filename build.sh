#!/bin/bash

# 执行 junit 测试
test() {
  ./gradlew testClasses
  local cps=(`./gradlew cp`)
  local test_cp="${cps[3]}"
  local group="${cps[4]}"
  java -Duser.timezone=UTC -cp "${test_cp}" org.junit.platform.console.ConsoleLauncher \
    "${1}" "${group}.test${2}"
}

[[ -n "${*}" ]] && "${@}"