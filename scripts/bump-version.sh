#!/bin/bash
# Script para incrementar versão automaticamente
# Uso: ./scripts/bump-version.sh [major|minor|patch]

set -e

VERSION_TYPE=${1:-patch}
PLUGIN_XML="src/main/resources/META-INF/plugin.xml"

# Obter versão atual
CURRENT_VERSION=$(grep -oP '(?<=<version>)[^<]+' "$PLUGIN_XML")
echo "📌 Versão atual: $CURRENT_VERSION"

# Separar versão em partes
IFS='.' read -r -a VERSION_PARTS <<< "$CURRENT_VERSION"
MAJOR="${VERSION_PARTS[0]}"
MINOR="${VERSION_PARTS[1]}"
PATCH="${VERSION_PARTS[2]}"

# Incrementar baseado no tipo
case $VERSION_TYPE in
  major)
    MAJOR=$((MAJOR + 1))
    MINOR=0
    PATCH=0
    ;;
  minor)
    MINOR=$((MINOR + 1))
    PATCH=0
    ;;
  patch)
    PATCH=$((PATCH + 1))
    ;;
  *)
    echo "❌ Tipo inválido: $VERSION_TYPE"
    echo "   Use: major, minor ou patch"
    exit 1
    ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
echo "🆕 Nova versão: $NEW_VERSION"

# Atualizar plugin.xml
sed -i.bak "s/<version>$CURRENT_VERSION<\/version>/<version>$NEW_VERSION<\/version>/" "$PLUGIN_XML"
rm "${PLUGIN_XML}.bak"

# Criar tag git
echo "🏷️  Criando tag v$NEW_VERSION..."
git add "$PLUGIN_XML"
git commit -m "chore: bump version to $NEW_VERSION"
git tag -a "v$NEW_VERSION" -m "Release version $NEW_VERSION"

echo "✅ Versão atualizada com sucesso!"
echo "   Para enviar: git push && git push --tags"
