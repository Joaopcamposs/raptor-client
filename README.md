# RaptorClient

<p align="center">
  <img src="src/main/resources/icons/raptor_dark.svg" alt="RaptorClient Logo" width="128" height="128">
</p>

<p align="center">
  <strong>Um plugin leve de cliente HTTP REST para IDEs JetBrains</strong>
</p>

<p align="center">
  <a href="https://plugins.jetbrains.com/plugin/30072-raptor-client"><img src="https://img.shields.io/badge/JetBrains%20Marketplace-RaptorClient-blue?logo=jetbrains" alt="JetBrains Marketplace"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/Joaopcamposs/raptor-client" alt="License"></a>
</p>

---

O RaptorClient permite criar, organizar e executar requisições HTTP diretamente dentro do IntelliJ IDEA, PyCharm, WebStorm e outras IDEs JetBrains — sem necessidade de alternar para uma ferramenta externa como Postman.

## Funcionalidades

- **Todos os métodos HTTP** — GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS
- **Editor em abas** — cada requisição abre como uma aba da IDE, igual a um arquivo de código
- **Coleções e pastas** — organize requisições hierarquicamente; tudo é persistido por projeto
- **Rascunhos** — requisições rápidas e descartáveis que ficam fora das suas coleções
- **Importação de cURL** — cole um comando cURL e obtenha uma requisição pronta para enviar
- **Variáveis de ambiente** — defina variáveis por ambiente (dev, staging, prod) e use `{{variavel}}` em URLs, headers e bodies
- **Autenticação** — Bearer Token, Basic Auth e API Key (header ou query param)
- **Múltiplos tipos de body** — raw (JSON, XML, Text, HTML, JS), form-data, x-www-form-urlencoded
- **Visualizador de resposta** — JSON formatado, body raw e headers de resposta em abas separadas com código de status, tempo e tamanho

## Instalação

### Do JetBrains Marketplace (recomendado)

1. Abra sua IDE → **Settings** → **Plugins** → **Marketplace**
2. Busque por **RaptorClient**
3. Clique em **Install** e reinicie a IDE

Ou instale diretamente da [página do Marketplace](https://plugins.jetbrains.com/plugin/30072-raptor-client).

### Do código-fonte

```bash
git clone https://github.com/Joaopcamposs/raptor-client.git
cd raptor-client
make install
```

O arquivo ZIP distribuível estará em `build/distributions/raptor-client-*.zip`.
Instale via **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk…**

## Início Rápido

1. Clique no ícone **RaptorClient** na barra lateral direita (ou **View → Tool Windows → RaptorClient**).
2. Clique em **+** para criar uma nova requisição.
3. Escolha um método, insira uma URL, configure headers/body/auth conforme necessário.
4. Clique em **Send**.
5. Visualize a resposta (JSON, Raw, Headers) no painel inferior.

### Importando cURL

Clique em **Import** na barra de ferramentas, cole seu comando cURL e pressione **OK**. A requisição será criada automaticamente com headers, body e auth pré-preenchidos.

### Variáveis de Ambiente

Crie ambientes (ex: Development, Production) com pares chave-valor. Referencie-os em qualquer lugar com `{{chave}}`:

```
{{base_url}}/api/v1/users
Authorization: Bearer {{access_token}}
```

## Requisitos

| Requisito | Versão |
|-----------|--------|
| IDE JetBrains | 2024.3+ (IntelliJ IDEA, PyCharm, WebStorm, etc.) |
| JDK (para build do código-fonte) | 21+ (baixado automaticamente pelo Gradle toolchain) |

## Desenvolvimento

```bash
make help       # Mostra todos os comandos disponíveis
make build      # Compila o plugin
make run        # Inicia uma IDE sandbox com o plugin
make test       # Executa testes unitários
make lint       # Verifica estilo do código (ktlint)
make lint-fix   # Corrige estilo automaticamente
make package    # Cria o ZIP distribuível
make verify     # Verifica compatibilidade do plugin
make clean      # Limpa artefatos de build
```

Ou use o Gradle diretamente:

```bash
./gradlew build          # Compilar
./gradlew runIde         # IDE sandbox
./gradlew buildPlugin    # Criar ZIP
./gradlew test           # Executar testes
./gradlew ktlintFormat   # Formatar código
```

## Estrutura do Projeto

```
src/main/kotlin/com/raptorclient/
├── actions/      # Ações da IDE (Nova Requisição, Nova Pasta, Importar cURL, Atualizar)
├── editor/       # FileEditor customizado que abre requisições como abas da IDE
├── models/       # Classes de dados: RequestItem, Collection, HttpResponse, etc.
├── services/     # Lógica de negócio: execução HTTP, armazenamento, ambientes, parsing de cURL
├── toolwindow/   # Painel lateral com a árvore de coleções
└── ui/           # Painéis Swing: editor de requisição, auth, body, tabelas chave-valor
```

Para uma explicação detalhada da arquitetura e cada módulo, veja [ARCHITECTURE.md](ARCHITECTURE.md).

## Contribuindo

Contribuições são bem-vindas! Por favor, leia [CONTRIBUTING.md](CONTRIBUTING.md) para orientações sobre configuração do ambiente de desenvolvimento, estilo de código e envio de pull requests.

## Licença

Este projeto está licenciado sob a [Licença MIT](LICENSE).

Construído com o [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html).
