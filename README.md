# UserApp — Avaliação Desenvolvedor Android (UOL Inc.)

Aplicativo Android nativo, desenvolvido em **Kotlin**, que consome a API pública
[JSONPlaceholder](https://jsonplaceholder.typicode.com/) para exibir usuários, seus álbuns
e as fotos de cada álbum.

Projeto desenvolvido como avaliação técnica, seguindo **Clean Architecture**, **MVVM** e
princípios **SOLID**.

> A arquitetura foi propositalmente estruturada acima do mínimo exigido pelo enunciado —
> não por complexidade desnecessária, mas para demonstrar como eu organizaria um projeto
> real em produção: código de fácil manutenção, testável em isolamento e preparado para
> crescer sem retrabalho.

---

## 📱 Funcionalidades

| Tela | Descrição |
|---|---|
| **Listagem de usuários** | Lista todos os usuários da API com busca/filtro em tempo real (por nome, username ou e-mail) |
| **Detalhe do usuário** | Exibe nome, username e e-mail do usuário selecionado, além da lista de álbuns pertencentes a ele |
| **Álbum (grid de fotos)** | Renderiza as fotos do álbum em grid (`GridLayoutManager`, 3 colunas); ao tocar em uma foto, abre no app padrão de fotos do dispositivo |

Navegação: `Listagem de usuários → Detalhe do usuário → Álbum (grid de fotos)`

---

## 🏗️ Arquitetura

O projeto segue **Clean Architecture** combinada com **MVVM**, organizada por *feature* (não
por camada técnica global), e cada feature é dividida internamente em três camadas:

```
data          → DTOs (Response), Entities (Room), Mappers, RepositoryImpl
domain        → Modelos de domínio imutáveis, Repository (interface), UseCases
presentation  → UiState, ViewModel, Fragment, Adapter
```

**Regra de dependência:** `presentation → domain ← data`. A camada `domain` nunca conhece
Retrofit, Room, Fragment ou qualquer detalhe de framework — só interfaces e modelos puros
em Kotlin. Isso é o que permite trocar a fonte de dados (ex.: API → cache local) sem alterar
UseCase nem ViewModel.

### Princípios SOLID aplicados
- **SRP**: cada UseCase resolve uma única operação de negócio; Mappers só convertem tipos;
  ViewModels só orquestram estado de tela.
- **OCP**: `Result<T>` e `UiState` são `sealed class`/`interface` — novos estados são
  adicionados sem quebrar quem já consome os `when` exaustivos existentes.
- **LSP/ISP**: Repositories expõem só os métodos que cada UseCase realmente precisa
  (`UserRepository`, `AlbumRepository`.
- **DIP**: toda camada superior depende de abstrações (`UserRepository`, não
  `UserRepositoryImpl`; `ApiService`, não `Retrofit` diretamente). A inversão é resolvida em
  tempo de compilação pelo **Hilt**.

### Padrão de camada `presentation`
- ViewModel expõe estado via **`StateFlow<XxxUiState>`** (não LiveData).
- `XxxUiState` é uma `sealed class` própria de cada tela: no mínimo `Loading / Success /
  Error` (a listagem de usuários também tem `Empty`, para filtro sem resultado).
- Fragment coleta o estado com `viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle
  (STARTED) { ... } }`.
- RecyclerViews usam `ListAdapter` + `DiffUtil`.

---

## 🧱 Estratégia Offline-First

O app implementa uma estratégia de **Single Source of Truth simplificada** com Room:

1. Toda chamada de leitura (`getUsers`, `getUserById`, `getAlbumsByUser`,
   `getPhotosByAlbum`) tenta a API **primeiro**.
2. Em caso de **sucesso**, o resultado é salvo no Room (cache) e devolvido à UI.
3. Em caso de **`IOException`** (falha de conectividade — sem internet, timeout, DNS, etc.),
   o repositório recorre ao cache local do Room:
    - Se o cache tiver dados → devolve como `Result.Success` (o app continua funcional
      offline, mostrando o último estado conhecido).
    - Se o cache estiver vazio → devolve `Result.Error` com mensagem amigável ("Falha de
      conexão. Verifique sua internet.").
4. Um **erro HTTP** (ex.: 404, 500) **não** aciona o fallback de cache — o servidor
   respondeu, então não é tratado como cenário "offline"; o erro é repassado diretamente à UI.

Essa lógica está coberta por testes unitários (`UserRepositoryImplTest`,
`AlbumRepositoryImplTest`) para todos os cenários: sucesso, erro HTTP, fallback com cache
populado, fallback com cache vazio e exceção genérica.

> **Nota sobre URLs de imagem:** durante o desenvolvimento, percebi que a API JSONPlaceholder
> retorna URLs de fotos hospedadas em `via.placeholder.com`, um serviço com falhas conhecidas
> e persistentes de certificado SSL desde 2023 — problema documentado pela própria comunidade,
> não uma instabilidade pontual da minha rede. Como isso impedia demonstrar corretamente a
> tela de álbum (tanto o grid via Picasso quanto a abertura no app padrão de fotos, que são
> requisitos centrais desta tela), tomei a liberdade de sanitizar essas URLs no `PhotoMapper`,
> substituindo-as por `picsum.photos` (usando o id da foto como seed, para manter
> determinismo). É uma pequena alteração no dado vindo da API, feita conscientemente e
> documentada aqui para ficar transparente — o objetivo foi só garantir que a funcionalidade
> pudesse ser avaliada como pretendido.


---

## 🛠️ Stack Técnica

| Categoria | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| Arquitetura | Clean Architecture + MVVM + SOLID |
| Injeção de dependência | Hilt (`hilt-android`, `hilt-navigation-fragment`, KSP) |
| Rede | Retrofit + Gson Converter + OkHttp (`HttpLoggingInterceptor`) |
| Persistência local | Room (cache offline-first) |
| Imagens | Picasso |
| Navegação | Navigation Component + Safe Args (single-Activity, multi-Fragment) |
| Assíncrono | Kotlin Coroutines + StateFlow |
| Listas | RecyclerView (`ListAdapter` + `DiffUtil`), `GridLayoutManager` para o grid de fotos |
| UI | Material 3, ViewBinding, layouts XML, ícones vetoriais, dark mode automático (`DayNight`) |
| Testes | JUnit4, MockK, kotlinx-coroutines-test |
| Build | Gradle Kotlin DSL (`build.gradle.kts`) + Version Catalog (`libs.versions.toml`) |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36 |

---

## 📂 Estrutura de Pacotes

```
com.uol.userapp/
├── MainActivity.kt                    — single-Activity host, Toolbar + Edge-to-Edge
├── UserAppApplication.kt              — @HiltAndroidApp
│
├── core/
│   ├── data/
│   │   ├── local/AppDatabase.kt       — RoomDatabase
│   │   └── remote/ApiService.kt       — contratos Retrofit
│   ├── di/
│   │   ├── DatabaseModule.kt          — provê AppDatabase + DAOs
│   │   └── NetworkModule.kt           — provê OkHttp/Retrofit/ApiService
│   ├── domain/util/
│   │   ├── Result.kt                  — sealed class genérica (Success/Error/Loading)
│   │   └── ApiException.kt            — exception para status HTTP != 2xx
│   └── extensions/
│       └── ViewExtensions.kt          — applyWindowInsets() (edge-to-edge reutilizável)
│
└── features/
    ├── users/
    │   ├── data/       — UserResponse (DTO), UserEntity (Room), UserMapper, UserRepositoryImpl
    │   ├── di/         — UserModule (@Binds)
    │   ├── domain/     — User, Address, Company / UserRepository / GetUsersUseCase, GetUserByIdUseCase
    │   └── presentation/
    │       ├── list/    — Tela 1: UsersUiState, UsersViewModel, UserListFragment, UsersAdapter
    │       └── detail/  — Tela 2: UserDetailUiState, UserDetailViewModel, UserDetailFragment
    │
    └── albums/
        ├── data/       — AlbumResponse/PhotoResponse, AlbumEntity/PhotoEntity, Mappers, AlbumRepositoryImpl
        ├── di/         — AlbumModule (@Binds)
        ├── domain/     — Album, Photo / AlbumRepository / GetAlbumsByUserUseCase, GetPhotosByAlbumUseCase
        └── presentation/
            ├── list/adapter/   — AlbumsAdapter (usado dentro do UserDetailFragment)
            └── detail/         — Tela 3: AlbumDetailUiState, AlbumDetailViewModel, AlbumDetailFragment, PhotosAdapter
```

---

## 🎨 UI, Dark Mode e Edge-to-Edge

- **Ícones vetoriais** (`ic_person`, `ic_error_outline`, `ic_search_off`, `ic_broken_image`,
  `ic_arrow_back`, `ic_chevron_right`) — sem nenhum PNG/bitmap para ícones de UI.
- **Dark mode automático**: tema base `Theme.Material3.DayNight.NoActionBar`, com
  `values/themes.xml` (light) e `values-night/themes.xml` (dark) sobrescrevendo
  `colorPrimary`, `colorSurface`, `colorOnSurface`, etc. Segue o padrão do sistema — nenhum
  toggle manual é necessário.
- **Edge-to-edge** (`enableEdgeToEdge()`, obrigatório a partir do target SDK 35): a
  `MainActivity` aplica inset de status bar no `AppBarLayout` e ajusta a cor dos ícones da
  status bar (claros/escuros) conforme o tema. Cada Fragment aplica seus próprios insets de
  status bar / navigation bar via a extensão reutilizável `applyWindowInsets()` em
  `core/extensions/ViewExtensions.kt`, evitando que conteúdo fique escondido atrás das barras
  do sistema em qualquer tela.

---

## ✅ Requisitos do Enunciado

### Obrigatórios
- [x] Kotlin
- [x] Consome `https://jsonplaceholder.typicode.com/` (users, albums, photos)
- [x] Arquitetura permitida: **MVVM** (+ Clean Architecture e SOLID como decisão extra)
- [x] Layouts em XML (sem Compose)
- [x] Retrofit
- [x] Picasso (usado no grid de fotos — `PhotosAdapter`)
- [x] RecyclerView (listagem de usuários, listagem de álbuns, grid de fotos)
- [x] Tela de listagem de usuários com filtro
- [x] Tela de detalhe do usuário + listagem de álbuns
- [x] Tela de álbum em grid + abrir foto no app padrão de fotos do dispositivo

### Opcionais
- [x] Ícones em formato vector
- [x] Ícone customizado do aplicativo
- [x] Dark mode seguindo o padrão do dispositivo

### Extras (decisão própria, além do enunciado)
- [x] Clean Architecture (camadas `data`/`domain`/`presentation` por feature)
- [x] Injeção de dependência com Hilt
- [x] Persistência local com Room — estratégia offline-first
- [x] Testes unitários de Mapper, Repository e ViewModel (MockK + Coroutines Test)
- [x] Edge-to-edge / suporte a Android 15+

---

## 🧪 Testes

```bash
./gradlew test
```

Cobertura atual:
- **Mappers**: `UserMapperTest`, `AlbumMapperTest`, `PhotoMapperTest` — conversão
  `Response ↔ Entity ↔ Domain`, incluindo campos nulos/ausentes.
- **Repositories**: `UserRepositoryImplTest`, `AlbumRepositoryImplTest` — sucesso, erro
  HTTP, fallback offline (cache populado e vazio), exceção genérica.
- **ViewModels**: `UsersViewModelTest` (filtro de busca e transições de `UiState`),
  `UserDetailViewModelTest`, `AlbumDetailViewModelTest`.
- `MainDispatcherRule` (`testutil/`) troca o dispatcher principal por um de teste, permitindo
  rodar `viewModelScope.launch` de forma síncrona nos testes.

> Decisão consciente: **não foram criados testes unitários dedicados para os UseCases**
> (`GetUsersUseCase`, `GetUserByIdUseCase`, `GetAlbumsByUserUseCase`,
> `GetPhotosByAlbumUseCase`). Cada UseCase é um passthrough de uma única linha para o
> Repository correspondente (`operator fun invoke() = repository.getX()`); testá-los
> isoladamente só reafirmaria o mock do Repository, sem agregar cobertura real — o
> comportamento de negócio já está integralmente coberto pelos testes de Repository.

---

## ▶️ Como rodar o projeto

### Pré-requisitos
- Android Studio (versão recente, compatível com AGP usado no projeto)
- JDK 17
- SDK Android 36 instalado (compileSdk / targetSdk)
- Conexão com internet (para a primeira carga de dados via API)

### Passos
1. Clone ou extraia o projeto.
2. Abra a pasta raiz no Android Studio.
3. Aguarde o **Gradle Sync** (baixa todas as dependências via `libs.versions.toml`).
4. Rode em um emulador ou dispositivo físico com **Android 7.0 (API 24) ou superior**.
5. Nenhuma configuração adicional (API Key, `.env`, etc.) é necessária — o app consome a
   API pública do JSONPlaceholder diretamente.

### Testando o modo offline
1. Abra o app normalmente com internet ativa (isso popula o cache Room).
2. Ative o **modo avião** no dispositivo/emulador.
3. Feche e reabra o app, ou navegue entre as telas — os dados já visitados continuam
   disponíveis, vindos do cache local.

---

## 📌 Observações técnicas

- O projeto usa **Gradle Kotlin DSL** com **Version Catalog** (`libs.versions.toml`) — todas
  as versões de dependências ficam centralizadas nesse arquivo.
- Injeção de dependência migrada de `kapt` para **KSP** (mais rápido em tempo de build) —
  usar uma versão do Android Studio/Kotlin compatível com KSP.
- `compileSdk`/`targetSdk = 36`: se o Android Studio usado para avaliar o projeto tiver uma
  versão de SDK/AGP mais antiga instalada, pode ser necessário atualizar o SDK Manager antes
  do build.

---

## 👤 Autor

Desenvolvido por Pedro Henrique como avaliação técnica para **UOL Inc.**