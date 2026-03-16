# RaptorClient — Architecture

This document describes the internal architecture of RaptorClient, a JetBrains IDE plugin for HTTP REST requests. It is intended to help contributors understand the codebase and navigate the modules efficiently.

## High-Level Overview

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

## Package Structure

All source code lives under `com.raptorclient` in `src/main/kotlin/com/raptorclient/`.

```
com.raptorclient/
├── actions/        # IDE actions triggered by toolbar buttons and menus
├── editor/         # Custom file editor for HTTP requests (tabs)
├── models/         # Data classes and enums (pure Kotlin, no IDE deps)
├── services/       # Business logic: HTTP execution, storage, environments
├── toolwindow/     # Sidebar tool window (collection tree)
└── ui/             # Swing UI components for the request editor
```

---

## Module Details

### `models/`

Pure Kotlin data classes with no IDE dependencies. Serialized to/from JSON via Jackson.

| File | Description |
|------|-------------|
| `RequestItem.kt` | Core model for an HTTP request. Contains `KeyValuePair`, `RequestBody`, `AuthConfig`, and related enums (`BodyType`, `RawBodyType`, `AuthType`, `ApiKeyLocation`). |
| `Collection.kt` | A collection of requests, drafts, and folders. Provides lookup/filtering helpers. |
| `FolderItem.kt` | Represents a folder that can hold requests and subfolders (hierarchical via `parentId`). |
| `HttpMethod.kt` | Enum of supported HTTP methods with display color for the UI. |
| `HttpResponse.kt` | Immutable response model with formatted size/time helpers. |

**Key design decisions:**
- Models use `@JsonIgnoreProperties(ignoreUnknown = true)` for forward compatibility.
- Mutable fields (`var`) are used because the UI binds directly to model properties.
- `RequestItem.duplicate()` creates a deep copy with a new ID.

---

### `services/`

Business logic layer. Services have no direct Swing/UI dependencies.

| File | Description |
|------|-------------|
| `HttpClientService.kt` | Executes HTTP requests using OkHttp. Handles all body types, authentication, query parameters, and environment variable resolution. |
| `RequestStorageService.kt` | Persists the request collection to the IDE's project-level XML storage (`raptorClient.xml`). Implements `PersistentStateComponent`. |
| `EnvironmentService.kt` | Manages environment variables (e.g., Development, Staging). Persisted in `raptorClientEnv.xml`. Resolves `{{variable}}` placeholders. |
| `CurlParser.kt` | Parses cURL commands into `RequestItem` objects. Supports headers, body types, auth, cookies, user-agent, and more. |

**Key design decisions:**
- `RequestStorageService` and `EnvironmentService` are project-level services registered in `plugin.xml`.
- Both use a listener pattern (`CollectionChangeListener`, `EnvironmentChangeListener`) to notify the UI of changes.
- `HttpClientService` is stateless (except for the shared OkHttp client instance) and can be instantiated freely.
- `CurlParser` is a standalone class with no dependencies — easy to test in isolation.

---

### `actions/`

IDE actions registered in `plugin.xml` under the `RaptorClient.ToolbarActions` group. Each extends `AnAction`.

| File | Description |
|------|-------------|
| `NewRequestAction.kt` | Creates a new draft request and opens it in the editor. |
| `NewFolderAction.kt` | Prompts for a folder name and adds it to the collection. |
| `ImportCurlAction.kt` | Opens a dialog to paste a cURL command, parses it, and creates a draft. |
| `RefreshCollectionsAction.kt` | Triggers a tree refresh in the tool window sidebar. |

---

### `editor/`

Custom `FileEditor` implementation that opens HTTP requests as IDE tabs (like code files).

| File | Description |
|------|-------------|
| `RaptorVirtualFile.kt` | Virtual file backed by a `RequestItem`. Also contains `RaptorFileType` and `RaptorFileSystem`. |
| `RaptorRequestEditor.kt` | The `FileEditor` implementation. Creates a `RequestEditorPanel` as its UI. |
| `RaptorRequestEditorProvider.kt` | Tells the IDE to use `RaptorRequestEditor` for `RaptorVirtualFile` instances. |
| `RaptorEditorManager.kt` | Singleton that tracks open request editors and prevents duplicate tabs. |

**Key design decisions:**
- Uses a custom `VirtualFileSystem` with the `raptor://` protocol.
- `RaptorEditorManager` is an `object` (singleton) to provide global tab management.
- Editor initialization is deferred via `SwingUtilities.invokeLater` to avoid blocking the EDT.

---

### `toolwindow/`

The sidebar panel visible in the IDE.

| File | Description |
|------|-------------|
| `RaptorToolWindowFactory.kt` | Factory registered in `plugin.xml` that creates the tool window content. |
| `RaptorToolWindowPanel.kt` | A `SimpleToolWindowPanel` with a JTree showing folders, requests, and drafts. Includes toolbar buttons and a context menu (right-click). |

**Key design decisions:**
- The tree is rebuilt entirely on each refresh (`refreshTree()`), which is simple and correct for the expected data size.
- Listens to `RequestStorageService` changes to auto-refresh.
- Custom `TreeCellRenderer` shows HTTP method names with color coding.

---

### `ui/`

Reusable Swing panels used inside the request editor.

| File | Description |
|------|-------------|
| `RequestEditorPanel.kt` | Main editor panel. Combines the URL bar, method selector, Send/Save buttons, request tabs (Params, Body, Headers, Auth), and response viewer. |
| `KeyValuePanel.kt` | A table-based editor for key-value pairs (used for headers, query params, form data, URL-encoded data). |
| `BodyEditorPanel.kt` | Manages body type selection (none, raw, form-data, URL-encoded) with a `CardLayout`. |
| `AuthPanel.kt` | Manages auth type selection (None, Bearer, Basic, API Key) with a `CardLayout`. |

**Key design decisions:**
- HTTP requests are executed on a pooled thread (`ApplicationManager.getApplication().executeOnPooledThread`) to avoid blocking the UI.
- JSON responses are pretty-printed using Gson.
- Environment variable resolution happens at request execution time, not at edit time.

---

## Data Persistence

| File | Storage |
|------|---------|
| `raptorClient.xml` | Requests, drafts, and folders (JSON inside IDE XML) |
| `raptorClientEnv.xml` | Environment variables (JSON inside IDE XML) |

Both files live in the project's `.idea/` directory and are managed by IntelliJ's `PersistentStateComponent` mechanism.

---

## Plugin Registration (`plugin.xml`)

All extension points and actions are declared in `src/main/resources/META-INF/plugin.xml`:

- **Tool Window** — `RaptorToolWindowFactory` anchored to the right sidebar
- **File Editor Provider** — `RaptorRequestEditorProvider`
- **Project Services** — `RequestStorageService`, `EnvironmentService`
- **Notification Group** — `RaptorClient.Notifications`
- **Actions** — New Request, New Folder, Import cURL, Refresh

---

## Dependencies

| Library | Purpose |
|---------|---------|
| OkHttp 4.x | HTTP client for executing requests |
| Jackson + Kotlin Module | JSON serialization for persisted models |
| Gson | JSON pretty-printing for response display |
| IntelliJ Platform SDK | IDE integration (services, editors, UI) |

---

## Build & Tooling

- **Gradle** with the IntelliJ Platform Gradle Plugin (`org.jetbrains.intellij.platform`)
- **ktlint** for code style enforcement
- **Qodana** for static analysis (configured in `qodana.yaml`)
- **Makefile** provides convenient shortcuts for all common tasks
