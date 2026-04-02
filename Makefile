# RaptorClient - Makefile
# Plugin HTTP REST Client para JetBrains IDEs

# Variáveis
GRADLE = ./gradlew
# Gradle toolchain baixará JDK 21 automaticamente se necessário

.PHONY: help build clean test run verify package install publish lint lint-fix version-patch version-minor version-major version-auto

# Help - comando padrão
help:
	@echo "╔══════════════════════════════════════════════════════════════╗"
	@echo "║              RaptorClient - Comandos Disponíveis             ║"
	@echo "╠══════════════════════════════════════════════════════════════╣"
	@echo "║  make build      - Compila o plugin                          ║"
	@echo "║  make clean      - Limpa arquivos de build                   ║"
	@echo "║  make test       - Executa os testes                         ║"
	@echo "║  make run        - Inicia IDE de teste com o plugin          ║"
	@echo "║  make verify     - Verifica compatibilidade do plugin        ║"
	@echo "║  make package    - Cria o arquivo ZIP do plugin              ║"
	@echo "║  make install    - Build + Package                           ║"
	@echo "║  make publish    - Publica no JetBrains Marketplace          ║"
	@echo "║  make deps       - Baixa dependências                        ║"
	@echo "║  make refresh    - Atualiza dependências                     ║"
	@echo "║  make lint       - Verifica estilo do código (ktlint)        ║"
	@echo "║  make lint-fix   - Corrige estilo automaticamente            ║"
	@echo "║  make info       - Mostra informações do ambiente            ║"
	@echo "║                                                              ║"
	@echo "║  Versionamento:                                              ║"
	@echo "║  make version-patch  - Incrementa versão PATCH (1.0.X)       ║"
	@echo "║  make version-minor  - Incrementa versão MINOR (1.X.0)       ║"
	@echo "║  make version-major  - Incrementa versão MAJOR (X.0.0)       ║"
	@echo "║  make version-auto   - Detecta tipo baseado em commits       ║"
	@echo "╚══════════════════════════════════════════════════════════════╝"

# Build do projeto
build:
	@echo "🔨 Compilando RaptorClient..."
	$(GRADLE) build --no-daemon

# Limpar arquivos de build
clean:
	@echo "🧹 Limpando arquivos de build..."
	$(GRADLE) clean --no-daemon
	@rm -rf .gradle .kotlin .intellijPlatform

# Executar testes
test:
	@echo "🧪 Executando testes..."
	$(GRADLE) test --no-daemon

# Executar IDE de teste
run:
	@echo "🚀 Iniciando IDE de teste..."
	$(GRADLE) runIde --no-daemon

# Verificar compatibilidade
verify:
	@echo "✅ Verificando compatibilidade do plugin..."
	$(GRADLE) verifyPlugin --no-daemon

# Criar pacote distribuível
package:
	@echo "📦 Criando pacote do plugin..."
	$(GRADLE) buildPlugin --no-daemon
	@echo ""
	@echo "✅ Plugin criado em: build/distributions/"
	@ls -lh build/distributions/*.zip 2>/dev/null || echo "Nenhum arquivo encontrado"

# Build completo + Package
install: clean build package
	@echo ""
	@echo "🎉 Plugin pronto para instalação!"
	@echo "📍 Arquivo: build/distributions/raptor-client-*.zip"

# Publicar no Marketplace (requer PUBLISH_TOKEN)
publish:
	@echo "📤 Publicando no JetBrains Marketplace..."
	@if [ -f .env ]; then \
		set -a; . ./.env; set +a; \
	fi; \
	if [ -z "$$PUBLISH_TOKEN" ]; then \
		echo "❌ Erro: PUBLISH_TOKEN não definido"; \
		echo "   Adicione PUBLISH_TOKEN=seu_token no arquivo .env"; \
		echo "   Ou exporte: export PUBLISH_TOKEN=seu_token"; \
		exit 1; \
	fi; \
	echo "🔑 Token encontrado (tamanho: $${#PUBLISH_TOKEN} chars)"; \
	$(GRADLE) publishPlugin --no-daemon

# Baixar dependências
deps:
	@echo "📥 Baixando dependências..."
	$(GRADLE) dependencies --no-daemon

# Atualizar dependências
refresh:
	@echo "🔄 Atualizando dependências..."
	$(GRADLE) build --refresh-dependencies --no-daemon

# Verificar estilo do código
lint:
	@echo "🔍 Verificando estilo do código..."
	$(GRADLE) ktlintCheck --no-daemon

# Corrigir estilo automaticamente
lint-fix:
	@echo "🔧 Corrigindo estilo do código..."
	$(GRADLE) ktlintFormat --no-daemon

# Informações do ambiente
info:
	@echo "╔══════════════════════════════════════════════════════════════╗"
	@echo "║                  Informações do Ambiente                     ║"
	@echo "╠══════════════════════════════════════════════════════════════╣"
	@echo "║ Java Version: $$(java -version 2>&1 | head -1)"
	@echo "║ Gradle: $$($(GRADLE) --version 2>/dev/null | grep 'Gradle' | head -1)"
	@echo "║ Toolchain: JDK 21 (Adoptium) - baixado automaticamente"
	@echo "║ OS: $$(uname -s) $$(uname -m)"
	@echo "╚══════════════════════════════════════════════════════════════╝"

# Versionamento
version-patch:
	@echo "📦 Incrementando versão PATCH..."
	@./scripts/bump-version.sh patch

version-minor:
	@echo "📦 Incrementando versão MINOR..."
	@./scripts/bump-version.sh minor

version-major:
	@echo "📦 Incrementando versão MAJOR..."
	@./scripts/bump-version.sh major

version-auto:
	@echo "🤖 Detectando tipo de versão automaticamente..."
	@./scripts/auto-version.sh

# Atalhos úteis
b: build
c: clean
t: test
r: run
p: package
i: install
l: lint
lf: lint-fix
vp: version-patch
vm: version-minor
vM: version-major
va: version-auto
