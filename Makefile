# RaptorClient - Makefile
# Plugin HTTP REST Client para JetBrains IDEs

# Variáveis
GRADLE = ./gradlew
# Gradle toolchain baixará JDK 21 automaticamente se necessário

.PHONY: help build clean test run verify package install publish lint lint-fix

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
	@if [ -z "$$PUBLISH_TOKEN" ]; then \
		echo "❌ Erro: PUBLISH_TOKEN não definido"; \
		echo "   Use: export PUBLISH_TOKEN=seu_token"; \
		exit 1; \
	fi
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

# Atalhos úteis
b: build
c: clean
t: test
r: run
p: package
i: install
l: lint
lf: lint-fix
