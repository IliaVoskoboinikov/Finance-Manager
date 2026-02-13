#!/bin/bash
set -e

# Папка с KtLint SARIF
SARIF_DIR="ktlint-sarif"
MERGED_SARIF="merged-ktlint.sarif"

# Собираем все уникальные ruleId и формируем список правил
RULES=$(jq -s '[.[][] | .runs[].results[] | {id: .ruleId, name: .ruleId, shortDescription: {text: .message.text}, properties: {severity: .level}, helpUri: "https://ktlint.github.io/rules/"}] | unique_by(.id)' $SARIF_DIR/*.sarif)

# Объединяем все результаты в один run
jq -s \
  --argjson rules "$RULES" \
  '{
    version: "2.1.0",
    runs: [
      {
        tool: {
          driver: {
            name: "ktlint",
            fullName: "ktlint",
            informationUri: "https://github.com/pinterest/ktlint/",
            semanticVersion: "1.8.0",
            rules: $rules
          }
        },
        results: (
          map(.runs[].results[] |
            # Меняем абсолютные пути на относительные
            .locations[].physicalLocation.artifactLocation.uri |=
              sub("^.*/Finance-Manager/"; "")
          )
        )
      }
    ]
  }' $SARIF_DIR/*.sarif > $MERGED_SARIF

echo "✅ Merged SARIF created: $MERGED_SARIF"
