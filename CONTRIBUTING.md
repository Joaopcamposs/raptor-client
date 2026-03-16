# Contributing to RaptorClient

Thank you for your interest in contributing to RaptorClient! This guide will help you get started.

## Getting Started

### Prerequisites

- **JDK 21+** (automatically downloaded by Gradle toolchain if not present)
- **Gradle 8.0+** (included via wrapper)
- A JetBrains IDE (IntelliJ IDEA recommended for development)

### Setting Up the Development Environment

1. Fork and clone the repository:
   ```bash
   git clone https://github.com/joaopcamposs/raptor-client.git
   cd raptor-client
   ```

2. Open the project in IntelliJ IDEA.

3. Build the project:
   ```bash
   make build
   ```

4. Run a sandboxed IDE with the plugin loaded:
   ```bash
   make run
   ```

## Development Workflow

### Code Style

- **Code** is written in **English**
- **Docstrings and comments** are written in **Portuguese (pt-BR)**
- Code formatting is enforced by [ktlint](https://pinterest.github.io/ktlint/)
- Run `make lint` to check and `make lint-fix` to auto-fix formatting issues

### Project Structure

See [ARCHITECTURE.md](ARCHITECTURE.md) for a detailed explanation of each module.

### Making Changes

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following existing patterns.

3. Run linting and tests:
   ```bash
   make lint-fix
   make test
   ```

4. Test the plugin manually:
   ```bash
   make run
   ```

5. Commit your changes with a meaningful message:
   ```bash
   git commit -m "feat: add support for XYZ"
   ```

### Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` — A new feature
- `fix:` — A bug fix
- `docs:` — Documentation changes
- `refactor:` — Code refactoring without feature changes
- `test:` — Adding or updating tests
- `chore:` — Maintenance tasks (build, CI, deps)

### Submitting a Pull Request

1. Push your branch to your fork.
2. Open a Pull Request against the `main` branch.
3. Fill out the PR description explaining **what** and **why**.
4. Ensure CI checks pass (lint, build, tests).
5. A maintainer will review your PR.

## Useful Commands

| Command          | Description                           |
|------------------|---------------------------------------|
| `make build`     | Compile the plugin                    |
| `make run`       | Launch a sandboxed IDE with the plugin|
| `make test`      | Run unit tests                        |
| `make lint`      | Check code style (ktlint)            |
| `make lint-fix`  | Auto-fix code style                  |
| `make package`   | Create distributable ZIP              |
| `make verify`    | Verify plugin compatibility           |
| `make clean`     | Clean build artifacts                 |

## Reporting Issues

- Use [GitHub Issues](https://github.com/Joaopcamposs/raptor-client/issues) to report bugs or request features.
- Include steps to reproduce, expected behavior, and IDE version when reporting bugs.

## Code of Conduct

Please be respectful and constructive. We are committed to providing a welcoming and inclusive experience for everyone.

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).
