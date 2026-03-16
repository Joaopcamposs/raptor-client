# RaptorClient

<p align="center">
  <img src="src/main/resources/icons/raptor_dark.svg" alt="RaptorClient Logo" width="128" height="128">
</p>

<p align="center">
  <strong>A lightweight HTTP REST client plugin for JetBrains IDEs</strong>
</p>

<p align="center">
  <a href="https://plugins.jetbrains.com/plugin/30072-raptor-client"><img src="https://img.shields.io/badge/JetBrains%20Marketplace-RaptorClient-blue?logo=jetbrains" alt="JetBrains Marketplace"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/Joaopcamposs/raptor-client" alt="License"></a>
</p>

---

RaptorClient lets you create, organize, and execute HTTP requests directly inside IntelliJ IDEA, PyCharm, WebStorm, and other JetBrains IDEs — no need to switch to an external tool like Postman.

## Features

- **All HTTP methods** — GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS
- **Tabbed editor** — each request opens as an IDE tab, just like a code file
- **Collections & folders** — organize requests hierarchically; everything is persisted per-project
- **Drafts** — quick throwaway requests that live outside your collections
- **cURL import** — paste a cURL command and get a ready-to-send request
- **Environment variables** — define variables per environment (dev, staging, prod) and use `{{variable}}` in URLs, headers, and bodies
- **Authentication** — Bearer Token, Basic Auth, and API Key (header or query param)
- **Multiple body types** — raw (JSON, XML, Text, HTML, JS), form-data, x-www-form-urlencoded
- **Response viewer** — formatted JSON, raw body, and response headers in separate tabs with status code, time, and size

## Installation

### From the JetBrains Marketplace (recommended)

1. Open your IDE → **Settings** → **Plugins** → **Marketplace**
2. Search for **RaptorClient**
3. Click **Install** and restart the IDE

Or install directly from the [Marketplace page](https://plugins.jetbrains.com/plugin/30072-raptor-client).

### From source

```bash
git clone https://github.com/Joaopcamposs/raptor-client.git
cd raptor-client
make install
```

The distributable ZIP will be at `build/distributions/raptor-client-*.zip`.
Install it via **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk…**

## Quick Start

1. Click the **RaptorClient** icon in the right sidebar (or **View → Tool Windows → RaptorClient**).
2. Click **+** to create a new request.
3. Choose a method, enter a URL, configure headers/body/auth as needed.
4. Click **Send**.
5. View the response (JSON, Raw, Headers) in the bottom panel.

### Importing cURL

Click **Import** in the toolbar, paste your cURL command, and press **OK**. The request will be created automatically with headers, body, and auth pre-filled.

### Environment Variables

Create environments (e.g., Development, Production) with key-value pairs. Reference them anywhere with `{{key}}`:

```
{{base_url}}/api/v1/users
Authorization: Bearer {{access_token}}
```

## Requirements

| Requirement | Version |
|-------------|---------|
| JetBrains IDE | 2024.3+ (IntelliJ IDEA, PyCharm, WebStorm, etc.) |
| JDK (for building from source) | 21+ (auto-downloaded by Gradle toolchain) |

## Development

```bash
make help       # Show all available commands
make build      # Compile the plugin
make run        # Launch a sandboxed IDE with the plugin
make test       # Run unit tests
make lint       # Check code style (ktlint)
make lint-fix   # Auto-fix code style
make package    # Create distributable ZIP
make verify     # Verify plugin compatibility
make clean      # Clean build artifacts
```

Or use Gradle directly:

```bash
./gradlew build          # Compile
./gradlew runIde         # Sandboxed IDE
./gradlew buildPlugin    # Create ZIP
./gradlew test           # Run tests
./gradlew ktlintFormat   # Format code
```

## Project Structure

```
src/main/kotlin/com/raptorclient/
├── actions/      # IDE actions (New Request, New Folder, Import cURL, Refresh)
├── editor/       # Custom FileEditor that opens requests as IDE tabs
├── models/       # Data classes: RequestItem, Collection, HttpResponse, etc.
├── services/     # Business logic: HTTP execution, storage, environments, cURL parsing
├── toolwindow/   # Sidebar panel with the collection tree
└── ui/           # Swing panels: request editor, auth, body, key-value tables
```

For a detailed explanation of the architecture and each module, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on setting up the dev environment, code style, and submitting pull requests.

## License

This project is licensed under the [MIT License](LICENSE).

Built with the [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html).
