# Contribuindo para o RaptorClient

Obrigado pelo seu interesse em contribuir para o RaptorClient! Este guia vai ajudá-lo a começar.

## Começando

### Pré-requisitos

- **JDK 21+** (baixado automaticamente pelo Gradle toolchain se não estiver presente)
- **Gradle 8.0+** (incluído via wrapper)
- Uma IDE JetBrains (IntelliJ IDEA recomendado para desenvolvimento)

### Configurando o Ambiente de Desenvolvimento

1. Faça fork e clone o repositório:
   ```bash
   git clone https://github.com/joaopcamposs/raptor-client.git
   cd raptor-client
   ```

2. Abra o projeto no IntelliJ IDEA.

3. Faça o build do projeto:
   ```bash
   make build
   ```

4. Execute uma IDE sandbox com o plugin carregado:
   ```bash
   make run
   ```

## Fluxo de Desenvolvimento

### Estilo de Código

- **Código** é escrito em **Inglês**
- **Docstrings e comentários** são escritos em **Português (pt-BR)**
- Formatação de código é aplicada pelo [ktlint](https://pinterest.github.io/ktlint/)
- Execute `make lint` para verificar e `make lint-fix` para corrigir problemas de formatação automaticamente

### Estrutura do Projeto

Veja [ARCHITECTURE.md](ARCHITECTURE.md) para uma explicação detalhada de cada módulo.

### Fazendo Alterações

1. Crie uma branch de feature a partir da `main`:
   ```bash
   git checkout -b feature/nome-da-sua-feature
   ```

2. Faça suas alterações seguindo os padrões existentes.

3. Execute linting e testes:
   ```bash
   make lint-fix
   make test
   ```

4. Teste o plugin manualmente:
   ```bash
   make run
   ```

5. Faça commit das suas alterações com uma mensagem significativa:
   ```bash
   git commit -m "feat: adiciona suporte para XYZ"
   ```

### Convenção de Mensagens de Commit

Seguimos [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` — Uma nova funcionalidade
- `fix:` — Correção de bug
- `docs:` — Mudanças na documentação
- `refactor:` — Refatoração de código sem mudanças de funcionalidade
- `test:` — Adição ou atualização de testes
- `chore:` — Tarefas de manutenção (build, CI, deps)

### Enviando um Pull Request

1. Faça push da sua branch para o seu fork.
2. Abra um Pull Request contra a branch `main`.
3. Preencha a descrição do PR explicando **o que** e **por que**.
4. Certifique-se de que as verificações de CI passam (lint, build, testes).
5. Um mantenedor revisará seu PR.

## Comandos Úteis

| Comando          | Descrição                                    |
|------------------|----------------------------------------------|
| `make build`     | Compila o plugin                             |
| `make run`       | Inicia uma IDE sandbox com o plugin          |
| `make test`      | Executa testes unitários                     |
| `make lint`      | Verifica estilo do código (ktlint)           |
| `make lint-fix`  | Corrige estilo automaticamente               |
| `make package`   | Cria o ZIP distribuível                      |
| `make verify`    | Verifica compatibilidade do plugin           |
| `make clean`     | Limpa artefatos de build                     |

## Reportando Problemas

- Use [GitHub Issues](https://github.com/Joaopcamposs/raptor-client/issues) para reportar bugs ou solicitar funcionalidades.
- Inclua passos para reproduzir, comportamento esperado e versão da IDE ao reportar bugs.

## Código de Conduta

Por favor, seja respeitoso e construtivo. Estamos comprometidos em fornecer uma experiência acolhedora e inclusiva para todos.

## Licença

Ao contribuir, você concorda que suas contribuições serão licenciadas sob a [Licença MIT](LICENSE).
