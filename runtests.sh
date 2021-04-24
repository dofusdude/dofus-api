#!/bin/bash
#
# Copyright 2021 Christopher Sieh (stelzo@steado.de)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

TEST_FILES="$(pwd)/test/tests/"

rm -f ./test/files/*

for file in "$TEST_FILES"*
do
  "$file"
  if [[ "$?" != 0 ]]; then # number of tests run. 255 (max) means everything ok
    printf "\n${RED}Error while testing :(${NC}\n\n"
    exit 1
  fi
done

rm -f ./test/files/*

printf "\n${GREEN}ALL TESTS PASSED :)${NC}\n\n"
exit 0
