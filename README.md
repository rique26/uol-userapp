##  Arquitetura
O projeto adota **Clean Architecture** combinada com **MVVM (Model-View-ViewModel)**, garantindo testabilidade, desacoplamento de responsabilidades e facilidade de manutenção.

##  Estrutura do Projeto
A organização de pacotes segue a abordagem **Feature-First**:
- `features/users/`: Módulo de usuários com suas respectivas camadas (`data`, `domain`, `presentation`).
- `features/albums/`: Módulo de álbuns e fotos com suas respectivas camadas (`data`, `domain`, `presentation`).
- `core/`: Utilitários compartilhados, clientes de rede e abstrações comuns.

##  Decisões Técnicas
- **UseCases:** Isola cada regra de negócio em classes com responsabilidade única (`Single Responsibility Principle`).
- **Mappers:** Converte modelos de resposta de rede (DTOs) em modelos de domínio imutáveis, evitando acoplamento da API com a UI.
- **UiState:** Gerenciamento reativo e imutável dos estados da tela (`Loading`, `Success`, `Error`).
- **Result<T>:** Tratamento padronizado de exceções de rede e falhas de comunicação sem lançar exceções descontroladas na UI.
- **Navigation Component + Safe Args:** Navegação centralizada via `nav_graph` garantindo tipagem estática na passagem de parâmetros entre Fragments.

##  Como Rodar o Projeto
1. Clone o repositório:
   ```bash
   git clone [https://github.com/rique26/uol-userapp.git](https://github.com/rique26/uol-userapp.git)