# RaptorClient — Arquitetura

Este documento descreve a arquitetura interna do RaptorClient, um plugin de IDE JetBrains para requisições HTTP REST. Ele foi criado para ajudar contribuidores a entender a base de código e navegar pelos módulos de forma eficiente.

## Visão Geral de Alto Nível

```
┌─────────────────────────────────────────────────────────┐
│                    JetBrains IDE                        │
│                                                         │
│  ┌──────────────┐   ┌──────────────┐   ┌────────────┐  │
│  │  Tool Window  │   │   Editor     │   │  Actions   │  │
│  │  (sidebar)    │   │  (tabs)      │   │  (toolbar) │  │
│  └──────┬───────┘   └──────┬───────┘   └─────┬──────┘  │
│         │                  │                  │         │
│         ▼                  ▼                  ▼         │
│  ┌─────────────────────────────────────────────────┐    │
│  │                 UI Components                    │    │
│  │  RequestEditorPanel · AuthPanel · BodyEditorPanel│    │
│  │  KeyValuePanel                                   │    │
│  └──────────────────────┬──────────────────────────┘    │
│                         │                               │
│                         ▼                               │
│  ┌─────────────────────────────────────────────────┐    │
│  │                   Services                       │    │
│  │  HttpClientService · RequestStorageService       │    │
│  │  EnvironmentService · CurlParser                 │    │
│  └──────────────────────┬──────────────────────────┘    │
│                         │                               │
│                         ▼                               │
│  ┌─────────────────────────────────────────────────┐    │
│  │                    Models                        │    │
│  │  RequestItem · Collection · FolderItem           │    │
│  │  HttpResponse · HttpMethod · AuthConfig · ...    │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## Estrutura de Pacotes

Todo o código-fonte está em `com.raptorclient` em `src/main/kotlin/com/raptorclient/`.

```
com.raptorclient/
├── actions/        # Ações da IDE disparadas por botões da toolbar e menus
├── editor/         # Editor de arquivo customizado para requisições HTTP (abas)
├── models/         # Classes de dados e enums (Kotlin puro, sem deps da IDE)
├── services/       # Lógica de negócio: execução HTTP, armazenamento, ambientes
├── toolwindow/     # Janela de ferramenta lateral (árvore de coleções)
└── ui/             # Componentes UI Swing para o editor de requisições
```

---

## Detalhes dos Módulos

### `models/`

Classes de dados Kotlin puras sem dependências da IDE. Serializadas de/para JSON via Jackson.

| Arquivo | Descrição |
|---------|-----------|
| `RequestItem.kt` | Modelo principal para uma requisição HTTP. Contém `KeyValuePair`, `RequestBody`, `AuthConfig` e enums relacionados (`BodyType`, `RawBodyType`, `AuthType`, `ApiKeyLocation`). |
| `Collection.kt` | Uma coleção de requisições, rascunhos e pastas. Fornece helpers de busca/filtragem. |
| `FolderItem.kt` | Representa uma pasta que pode conter requisições e subpastas (hierárquico via `parentId`). |
| `HttpMethod.kt` | Enum dos métodos HTTP suportados com cor de exibição para a UI. |
| `HttpResponse.kt` | Modelo de resposta imutável com helpers de tamanho/tempo formatados. |

**Principais decisões de design:**
- Modelos usam `@JsonIgnoreProperties(ignoreUnknown = true)` para compatibilidade futura.
- Campos mutáveis (`var`) são usados porque a UI se vincula diretamente às propriedades do modelo.
- `RequestItem.duplicate()` cria uma cópia profunda com um novo ID.

---

### `services/`

Camada de lógica de negócio. Serviços não têm dependências diretas de Swing/UI.

| Arquivo | Descrição |
|---------|-----------|
| `HttpClientService.kt` | Executa requisições HTTP usando OkHttp. Lida com todos os tipos de body, autenticação, parâmetros de query e resolução de variáveis de ambiente. |
| `RequestStorageService.kt` | Persiste a coleção de requisições no armazenamento XML em nível de projeto da IDE (`raptorClient.xml`). Implementa `PersistentStateComponent`. |
| `EnvironmentService.kt` | Gerencia variáveis de ambiente (ex: Development, Staging). Persistido em `raptorClientEnv.xml`. Resolve placeholders `{{variavel}}`. |
| `CurlParser.kt` | Faz parsing de comandos cURL em objetos `RequestItem`. Suporta headers, tipos de body, auth, cookies, user-agent e mais. |

**Principais decisões de design:**
- `RequestStorageService` e `EnvironmentService` são serviços em nível de projeto registrados no `plugin.xml`.
- Ambos usam um padrão de listener (`CollectionChangeListener`, `EnvironmentChangeListener`) para notificar a UI de mudanças.
- `HttpClientService` é stateless (exceto pela instância compartilhada do cliente OkHttp) e pode ser instanciado livremente.
- `CurlParser` é uma classe standalone sem dependências — fácil de testar isoladamente.

---

### `actions/`

Ações da IDE registradas no `plugin.xml` sob o grupo `RaptorClient.ToolbarActions`. Cada uma estende `AnAction`.

| Arquivo | Descrição |
|---------|-----------|
| `NewRequestAction.kt` | Cria uma nova requisição rascunho e a abre no editor. |
| `NewFolderAction.kt` | Solicita um nome de pasta e a adiciona à coleção. |
| `ImportCurlAction.kt` | Abre um diálogo para colar um comando cURL, faz o parsing e cria um rascunho. |
| `RefreshCollectionsAction.kt` | Dispara uma atualização da árvore na barra lateral da janela de ferramenta. |

---

### `editor/`

Implementação customizada de `FileEditor` que abre requisições HTTP como abas da IDE (como arquivos de código).

| Arquivo | Descrição |
|---------|-----------|
| `RaptorVirtualFile.kt` | Arquivo virtual baseado em um `RequestItem`. Também contém `RaptorFileType` e `RaptorFileSystem`. |
| `RaptorRequestEditor.kt` | A implementação do `FileEditor`. Cria um `RequestEditorPanel` como sua UI. |
| `RaptorRequestEditorProvider.kt` | Informa à IDE para usar `RaptorRequestEditor` para instâncias de `RaptorVirtualFile`. |
| `RaptorEditorManager.kt` | Singleton que rastreia editores de requisição abertos e previne abas duplicadas. |

**Principais decisões de design:**
- Usa um `VirtualFileSystem` customizado com o protocolo `raptor://`.
- `RaptorEditorManager` é um `object` (singleton) para fornecer gerenciamento global de abas.
- A inicialização do editor é adiada via `SwingUtilities.invokeLater` para evitar bloquear o EDT.

---

### `toolwindow/`

O painel lateral visível na IDE.

| Arquivo | Descrição |
|---------|-----------|
| `RaptorToolWindowFactory.kt` | Factory registrada no `plugin.xml` que cria o conteúdo da janela de ferramenta. |
| `RaptorToolWindowPanel.kt` | Um `SimpleToolWindowPanel` com uma JTree mostrando pastas, requisições e rascunhos. Inclui botões de toolbar e um menu de contexto (clique direito). |

**Principais decisões de design:**
- A árvore é reconstruída completamente a cada atualização (`refreshTree()`), o que é simples e correto para o tamanho de dados esperado.
- Escuta mudanças do `RequestStorageService` para auto-atualizar.
- `TreeCellRenderer` customizado mostra nomes de métodos HTTP com codificação de cores.

---

### `ui/`

Painéis Swing reutilizáveis usados dentro do editor de requisições.

| Arquivo | Descrição |
|---------|-----------|
| `RequestEditorPanel.kt` | Painel editor principal. Combina a barra de URL, seletor de método, botões Send/Save, abas de requisição (Params, Body, Headers, Auth) e visualizador de resposta. |
| `KeyValuePanel.kt` | Um editor baseado em tabela para pares chave-valor (usado para headers, query params, form data, dados URL-encoded). |
| `BodyEditorPanel.kt` | Gerencia seleção de tipo de body (nenhum, raw, form-data, URL-encoded) com um `CardLayout`. |
| `AuthPanel.kt` | Gerencia seleção de tipo de auth (None, Bearer, Basic, API Key) com um `CardLayout`. |

**Principais decisões de design:**
- Requisições HTTP são executadas em uma thread pooled (`ApplicationManager.getApplication().executeOnPooledThread`) para evitar bloquear a UI.
- Respostas JSON são formatadas usando Gson.
- Resolução de variáveis de ambiente acontece no momento da execução da requisição, não no momento da edição.

---

## Persistência de Dados

| Arquivo | Armazenamento |
|---------|---------------|
| `raptorClient.xml` | Requisições, rascunhos e pastas (JSON dentro do XML da IDE) |
| `raptorClientEnv.xml` | Variáveis de ambiente (JSON dentro do XML da IDE) |

Ambos os arquivos ficam no diretório `.idea/` do projeto e são gerenciados pelo mecanismo `PersistentStateComponent` do IntelliJ.

---

## Registro do Plugin (`plugin.xml`)

Todos os pontos de extensão e ações são declarados em `src/main/resources/META-INF/plugin.xml`:

- **Tool Window** — `RaptorToolWindowFactory` ancorada na barra lateral direita
- **File Editor Provider** — `RaptorRequestEditorProvider`
- **Project Services** — `RequestStorageService`, `EnvironmentService`
- **Notification Group** — `RaptorClient.Notifications`
- **Actions** — Nova Requisição, Nova Pasta, Importar cURL, Atualizar

---

## Dependências

| Biblioteca | Propósito |
|------------|-----------|
| OkHttp 4.x | Cliente HTTP para executar requisições |
| Jackson + Kotlin Module | Serialização JSON para modelos persistidos |
| Gson | Formatação JSON para exibição de respostas |
| IntelliJ Platform SDK | Integração com a IDE (serviços, editores, UI) |

---

## Build & Ferramentas

- **Gradle** com o IntelliJ Platform Gradle Plugin (`org.jetbrains.intellij.platform`)
- **ktlint** para aplicação de estilo de código
- **Qodana** para análise estática (configurado em `qodana.yaml`)
- **Makefile** fornece atalhos convenientes para todas as tarefas comuns
