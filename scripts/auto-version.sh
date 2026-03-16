#!/bin/bash
# Script para incrementar versão baseado em Conventional Commits
# Analisa commits desde a última tag e determina o tipo de bump automaticamente

set -e

PLUGIN_XML="src/main/resources/META-INF/plugin.xml"

# Obter última tag
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
if [ -z "$LAST_TAG" ]; then
    echo "⚠️  Nenhuma tag encontrada, usando commits desde o início"
    COMMIT_RANGE="HEAD"
else
    echo "📌 Última tag: $LAST_TAG"
    COMMIT_RANGE="$LAST_TAG..HEAD"
fi

# Analisar commits
COMMITS=$(git log --pretty=format:"%s" $COMMIT_RANGE)

if [ -z "$COMMITS" ]; then
    echo "ℹ️  Nenhum commit novo desde a última tag"
    exit 0
fi

echo "📝 Analisando commits..."

# Determinar tipo de bump
HAS_BREAKING=false
HAS_FEAT=false
HAS_FIX=false

while IFS= read -r commit; do
    if [[ $commit =~ ^(feat|fix|chore|docs|style|refactor|perf|test)(\(.+\))?!: ]] || \
       [[ $commit =~ BREAKING[[:space:]]CHANGE ]]; then
        HAS_BREAKING=true
    elif [[ $commit =~ ^feat(\(.+\))?: ]]; then
        HAS_FEAT=true
    elif [[ $commit =~ ^fix(\(.+\))?: ]]; then
        HAS_FIX=true
    fi
done <<< "$COMMITS"

# Determinar tipo de versão
if [ "$HAS_BREAKING" = true ]; then
    VERSION_TYPE="major"
    echo "🔴 Breaking changes detectados → MAJOR"
elif [ "$HAS_FEAT" = true ]; then
    VERSION_TYPE="minor"
    echo "🟡 Features detectados → MINOR"
elif [ "$HAS_FIX" = true ]; then
    VERSION_TYPE="patch"
    echo "🟢 Fixes detectados → PATCH"
else
    VERSION_TYPE="patch"
    echo "⚪ Outros commits → PATCH"
fi

# Executar bump
./scripts/bump-version.sh "$VERSION_TYPE"
