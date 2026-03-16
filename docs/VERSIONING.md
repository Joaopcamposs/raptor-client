# Guia de Versionamento

Este projeto utiliza [Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH).

## Uso Rápido

### Opção 1: Automático (Recomendado)
Analisa os commits e determina o tipo de versão automaticamente:

```bash
make version-auto
# ou
make va
```

### Opção 2: Manual
Especifique o tipo de incremento:

```bash
# Incrementar PATCH (1.0.2 → 1.0.3)
make version-patch
# ou
make vp

# Incrementar MINOR (1.0.2 → 1.1.0)
make version-minor
# ou
make vm

# Incrementar MAJOR (1.0.2 → 2.0.0)
make version-major
# ou
make vM
```

## Conventional Commits

Para que o versionamento automático funcione corretamente, siga o padrão de commits:

### Formato
```
<tipo>(<escopo>): <descrição>

[corpo opcional]

[rodapé opcional]
```

### Tipos e Impacto na Versão

- **fix:** Correção de bug → **PATCH** (1.0.X)
  ```bash
  git commit -m "fix: corrige erro ao salvar requisição"
  ```

- **feat:** Nova funcionalidade → **MINOR** (1.X.0)
  ```bash
  git commit -m "feat: adiciona suporte a GraphQL"
  ```

- **BREAKING CHANGE:** Mudança incompatível → **MAJOR** (X.0.0)
  ```bash
  git commit -m "feat!: redesenha API de plugins
  
  BREAKING CHANGE: remove método legado getRequest()"
  ```

- **Outros tipos** (não afetam versão automática):
  - `chore:` - Tarefas de manutenção
  - `docs:` - Documentação
  - `style:` - Formatação
  - `refactor:` - Refatoração
  - `perf:` - Performance
  - `test:` - Testes

### Exemplos Completos

```bash
# Correção simples
git commit -m "fix: resolve memory leak no request pool"

# Nova feature com escopo
git commit -m "feat(auth): adiciona autenticação OAuth2"

# Breaking change
git commit -m "feat!: migra para nova API de storage

BREAKING CHANGE: RequestStorage agora retorna CompletableFuture"

# Múltiplas mudanças
git commit -m "refactor: reorganiza estrutura de pastas

- Move services para package dedicado
- Atualiza imports
- Remove código duplicado"
```

## Workflow Completo

```bash
# 1. Fazer suas mudanças
git add .
git commit -m "feat: adiciona nova funcionalidade"

# 2. Incrementar versão automaticamente
make version-auto

# 3. Enviar para repositório (se necessário)
# git push && git push --tags
```

## O que os Scripts Fazem

1. **bump-version.sh**: 
   - Lê versão atual do `plugin.xml`
   - Incrementa baseado no tipo (major/minor/patch)
   - Atualiza `plugin.xml`
   - Cria commit e tag git

2. **auto-version.sh**:
   - Analisa commits desde última tag
   - Detecta tipo de mudança (breaking/feat/fix)
   - Chama `bump-version.sh` com tipo correto

## Notas Importantes

- ⚠️ Os scripts **não fazem push automaticamente**
- ✅ Sempre revise as mudanças antes de fazer push
- 📝 Mantenha as release notes atualizadas no `plugin.xml`
- 🏷️ Tags seguem formato `vX.Y.Z` (ex: v1.0.2)

## Integração com CI/CD

Para automatizar em pipelines:

```yaml
# Exemplo GitHub Actions
- name: Bump version
  run: make version-auto

- name: Push changes
  run: |
    git push
    git push --tags
```
