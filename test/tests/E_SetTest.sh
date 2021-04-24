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

PURPLE='\033[0;35m'
GREEN='\033[0;32m'
RED='\033[0;31m'
LIGHT_BLUE='\033[0;34m'
NC='\033[0m' # No Color

printf "\n${LIGHT_BLUE}"$0"${NC}\n"

SECRET=secret

####

#########
TEST_NAME="CreateSetUnauthed"
SHOULD_STATUS="401"

INPUT_FILE="$(pwd)/test/data/$TEST_NAME-input.json"

#
OUTPUT_FILE="$(pwd)/test/files/${TEST_NAME}.json"
RESPONSE=$(curl -s -o "$OUTPUT_FILE" -w "%{http_code}" --location --request POST http://localhost:8080/en/sets \
  --header 'Content-Type: application/json'
)

# STATUS
if [[ "$RESPONSE" != "$SHOULD_STATUS" ]]; then
  printf "\n    "$TEST_NAME" ${RED}FAILED${NC}\n\n"
  printf "Status expected ${SHOULD_STATUS} but is ${PURPLE}${RESPONSE}${NC}\n"
  echo "$CONTENT"
  exit 1
fi

printf "\n"$TEST_NAME" ${GREEN}PASSED${NC}\n"

#########
TEST_NAME="CreateSet"
SHOULD_STATUS="200"

INPUT_FILE="$(pwd)/test/data/$TEST_NAME-input.json"
SHOULD_FILE="$(pwd)/test/data/$TEST_NAME-expect.json"

#
OUTPUT_FILE="$(pwd)/test/files/${TEST_NAME}.json"
RESPONSE=$(curl -s -o "$OUTPUT_FILE" -w "%{http_code}" --location --request POST http://localhost:8080/en/sets \
  --header 'Content-Type: application/json' \
  --header "Authorization: Bearer $SECRET" \
  --data @"$INPUT_FILE"
)

CONTENT="$(<$OUTPUT_FILE)"
SHOULD_CONTENT="$(<$SHOULD_FILE)"

# STATUS
if [[ "$RESPONSE" != "$SHOULD_STATUS" ]]; then
  printf "\n    "$TEST_NAME" ${RED}FAILED${NC}\n\n"
  printf "Status expected ${SHOULD_STATUS} but is ${PURPLE}${RESPONSE}${NC}\n"
  echo "$CONTENT"
  exit 1
fi

IS_CONTENT=$(echo "$CONTENT" | jq -r '.')
SHOULD_CONTENT=$(echo "$SHOULD_CONTENT" | jq -r '.')

# CONTENT
EQUAL=$(jq --argfile a "$SHOULD_FILE" --argfile b "$OUTPUT_FILE" -n '($a | (.. | arrays) |= sort) as $a | ($b | (.. | arrays) |= sort) as $b | $a == $b')
# -z var => var empty
if [[ "$EQUAL" == "false" ]]; then
  printf "\n    "$TEST_NAME" ${RED}FAILED${NC}\n\n"
  echo "Content expected:"
  echo "$SHOULD_CONTENT"
  echo "but is:"
  echo "$IS_CONTENT"
  exit 1
fi

printf "\n"$TEST_NAME" ${GREEN}PASSED${NC}\n"

exit 0