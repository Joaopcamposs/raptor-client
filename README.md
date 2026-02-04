# RaptorClient

<p align="center">
  <img src="src/main/resources/icons/raptor-branco.svg" alt="RaptorClient Logo" width="128" height="128">
</p>

**RaptorClient** é um cliente HTTP REST completo integrado diretamente às IDEs JetBrains (IntelliJ IDEA, PyCharm, WebStorm, etc.). Similar ao JetClient e Postman, permite criar, organizar e executar requisições HTTP sem sair do seu ambiente de desenvolvimento.

## 🚀 Funcionalidades

### Métodos HTTP Suportados
- **GET** - Recuperar recursos
- **POST** - Criar recursos
- **PUT** - Atualizar recursos (substituição completa)
- **PATCH** - Atualizar recursos (parcial)
- **DELETE** - Remover recursos
- **HEAD** - Obter cabeçalhos
- **OPTIONS** - Verificar métodos permitidos

### Interface do Usuário
- **Tool Window (Barra Lateral)** - Acesse o RaptorClient no painel direito da IDE
- **Editor em Abas** - Cada requisição abre em uma aba separada, como arquivos de código
- **Visualização de Respostas** - JSON formatado, Raw e Headers em abas separadas

### Organização de Requisições
- **Drafts (Rascunhos)** - Requisições temporárias para testes rápidos
- **Pastas** - Organize requisições em pastas hierárquicas
- **Collections** - Todas as requisições são salvas automaticamente no projeto

### Tipos de Body Suportados
- **none** - Sem corpo na requisição
- **raw** - Texto livre com suporte a:
  - JSON (`application/json`)
  - XML (`application/xml`)
  - Text (`text/plain`)
  - HTML (`text/html`)
  - JavaScript (`application/javascript`)
- **form-data** - Multipart form data
- **x-www-form-urlencoded** - URL encoded form data

### Autenticação
- **No Auth** - Sem autenticação
- **Bearer Token** - Token JWT ou OAuth
- **Basic Auth** - Usuário e senha (Base64)
- **API Key** - Chave de API (Header ou Query Parameter)

### Import de cURL
Importe comandos cURL diretamente para criar requisições:
```bash
curl -X POST https://api.example.com/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token123" \
  -d '{"name": "John", "email": "john@example.com"}'
```

### Variáveis de Ambiente
Use variáveis em URLs, headers e body:
```
{{base_url}}/api/{{version}}/users
Authorization: Bearer {{access_token}}
```

## 📦 Instalação

### Método 1: Instalar do arquivo ZIP
1. Baixe o arquivo `raptor-client-1.0.0.zip` da pasta `build/distributions/`
2. Na IDE, vá em **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk...**
3. Selecione o arquivo ZIP
4. Reinicie a IDE

### Método 2: Build do código fonte
```bash
# Clone o repositório
git clone https://github.com/seu-usuario/raptor-client.git
cd raptor-client

# Build do plugin
export JAVA_HOME=/path/to/jdk-21-or-higher
./gradlew buildPlugin

# O plugin estará em build/distributions/raptor-client-1.0.0.zip
```

## 🎯 Como Usar

### Abrindo o RaptorClient
1. Clique no ícone **RaptorClient** na barra lateral direita da IDE
2. Ou use **View** → **Tool Windows** → **RaptorClient**

### Criando uma Nova Requisição
1. Clique no botão **+** (Add) na toolbar do RaptorClient
2. Uma nova aba de editor será aberta
3. Selecione o método HTTP (GET, POST, etc.)
4. Digite a URL
5. Configure headers, body e autenticação nas abas correspondentes
6. Clique em **Send** para executar

### Importando cURL
1. Clique no botão **Import** na toolbar
2. Cole o comando cURL na caixa de diálogo
3. Clique em **OK**
4. A requisição será criada automaticamente

### Organizando em Pastas
1. Clique no botão **Folder** na toolbar
2. Digite o nome da pasta
3. Arraste requisições para dentro das pastas (via menu de contexto)

### Salvando Requisições
- Clique em **Save** para salvar a requisição atual
- As requisições são automaticamente persistidas no projeto

## 📋 Visualização de Respostas

### Aba JSON
- Exibe o corpo da resposta formatado como JSON
- Syntax highlighting automático
- Indentação para fácil leitura

### Aba Raw
- Exibe o corpo da resposta sem formatação
- Útil para respostas não-JSON

### Aba Headers
- Lista todos os headers da resposta
- Formato: `Header-Name: value`

### Informações de Status
- **Status Code** - Código HTTP da resposta (200, 404, 500, etc.)
- **Time** - Tempo de resposta em ms ou segundos
- **Size** - Tamanho do corpo da resposta

## ⚙️ Configuração

### Estrutura de Arquivos
```
.idea/
└── raptorClient.xml      # Requisições salvas
└── raptorClientEnv.xml   # Variáveis de ambiente
```

### Variáveis de Ambiente
Crie ambientes (Development, Staging, Production) com variáveis específicas:

```json
{
  "Development": {
    "base_url": "http://localhost:3000",
    "api_key": "dev-key-123"
  },
  "Production": {
    "base_url": "https://api.production.com",
    "api_key": "prod-key-456"
  }
}
```

## 🛠️ Requisitos

- **IDE**: IntelliJ IDEA 2024.3+ ou outras IDEs JetBrains compatíveis
- **Java**: JDK 17 ou superior (para desenvolvimento)
- **Gradle**: 8.0+ (incluído via wrapper)

## 📁 Estrutura do Projeto

```
raptor-client/
├── build.gradle.kts              # Configuração do build
├── settings.gradle.kts           # Nome do projeto
├── gradle.properties             # Propriedades do Gradle
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── com/raptorclient/
│       │       ├── actions/      # Ações do plugin
│       │       ├── editor/       # Editor de requisições
│       │       ├── models/       # Modelos de dados
│       │       ├── services/     # Serviços (HTTP, Storage)
│       │       ├── toolwindow/   # Tool Window (sidebar)
│       │       └── ui/           # Componentes de UI
│       └── resources/
│           ├── META-INF/
│           │   └── plugin.xml    # Configuração do plugin
│           ├── icons/            # Ícones
│           └── messages/         # Strings localizadas
└── build/
    └── distributions/            # Plugin compilado (.zip)
```

## 🔧 Desenvolvimento

Use os comandos `make` para facilitar o desenvolvimento:

```bash
make help       # Mostra todos os comandos disponíveis
make build      # Compila o plugin
make run        # Inicia IDE de teste com o plugin
make test       # Executa os testes
make lint       # Verifica estilo do código (ktlint)
make lint-fix   # Corrige estilo automaticamente
make package    # Cria o arquivo ZIP do plugin
make install    # Build completo + Package
make verify     # Verifica compatibilidade do plugin
make clean      # Limpa arquivos de build
```

### Ou usando Gradle diretamente:
```bash
./gradlew build          # Compila
./gradlew runIde         # IDE de teste
./gradlew buildPlugin    # Cria ZIP
./gradlew ktlintFormat   # Formata código
```

## 📝 API de Modelos

### RequestItem
```kotlin
data class RequestItem(
    val id: String,
    var name: String,
    var method: HttpMethod,
    var url: String,
    var headers: MutableList<KeyValuePair>,
    var params: MutableList<KeyValuePair>,
    var body: RequestBody,
    var auth: AuthConfig
)
```

### HttpMethod
```kotlin
enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
}
```

### AuthConfig
```kotlin
data class AuthConfig(
    var type: AuthType,
    var bearerToken: String,
    var basicUsername: String,
    var basicPassword: String,
    var apiKeyName: String,
    var apiKeyValue: String,
    var apiKeyLocation: ApiKeyLocation
)
```

## 📝 Próximos Passos para Publicação

1. **Criar conta no JetBrains Marketplace**
   - Acesse: https://plugins.jetbrains.com/
   - Crie uma conta ou faça login

2. **Gerar token de publicação**
   - Vá em: Hub → Settings → Personal Access Tokens
   - Crie um token com escopo `Plugin Repository`

3. **Verificar o plugin**
   ```bash
   make verify
   ```

4. **Publicar**
   ```bash
   export PUBLISH_TOKEN=seu_token_aqui
   make publish
   ```

5. **Aguardar aprovação**
   - O JetBrains revisará o plugin (1-2 dias úteis)

## 🤝 Contribuindo

1. Fork o repositório
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Execute o linter: `make lint-fix`
4. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
5. Push para a branch (`git push origin feature/nova-feature`)
6. Abra um Pull Request

## 📄 Licença

Este projeto está licenciado sob a MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

- Construído com [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)

---

