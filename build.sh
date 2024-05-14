#!/bin/bash

# 执行 junit 测试
# 全部 ./build.sh test
# 单个类 ./build.sh test -c 'MybatisTest'
# 单个方法 ./build.sh test -m 'MybatisTest#test1'
test() {
  ./gradlew testClasses
  local cps=(`./gradlew cp`)
  local test_cp="${cps[3]}"
  local group="${cps[4]}"
  local args=("${@}")
  [[ "${#args}" == 2 ]] && args[1]="com.github.zava.test.${args[1]}"
  [[ "${#args}" == 0 ]] && args=(-p com.github.zava.test)
  set -x
  java -Duser.timezone=UTC -cp "${test_cp}" org.junit.platform.console.ConsoleLauncher "${args[@]}"
}

[[ -n "${*}" ]] && "${@}"