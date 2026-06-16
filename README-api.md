# 🏥 Clínica Transport API

Sistema completo de gestão de transportes clínicos desenvolvido com **Java 21 + Spring Boot 3.2**.

O sistema permite que contratantes solicitem operações de transporte clínico via API REST autenticada, com **seleção automática de equipe e veículo** baseada no tipo e urgência da operação.

---

## 🚀 Tecnologias

- **Java 21**
- **Spring Boot 3.2**
- **Spring Security + JWT** — autenticação stateless
- **Spring Data JPA + Hibernate** — persistência com herança de tabela
- **H2 Database** — banco em memória para desenvolvimento
- **PostgreSQL** — banco para produção
- **Lombok** — redução de boilerplate
- **Maven** — gerenciamento de dependências

---

## 🏗️ Arquitetura

O projeto segue arquitetura em camadas com separação clara de responsabilidades:

```
controller/   → endpoints REST (recebe e devolve HTTP)
service/      → regras de negócio e seleção automática de equipe
repository/   → acesso ao banco via Spring Data JPA
model/        → entidades JPA organizadas por domínio
dto/          → objetos de transferência (segurança de entrada/saída)
security/     → JWT filter, geração e validação de tokens
config/       → Spring Security e CORS
exception/    → tratamento global de erros
enums/        → tipos do domínio clínico
interfaces/   → contratos: Validavel, Custeavel, Priorizavel...
```

---

## ✨ Funcionalidades

- ✅ Cadastro e autenticação de contratantes via JWT
- ✅ Solicitação de 4 tipos de operação clínica
- ✅ **Seleção automática de equipe** pelo nível de urgência/criticidade
- ✅ **Seleção automática de veículo** compatível com cada tipo de operação
- ✅ Fluxo de status: `SOLICITADA → APROVADA → EM_EXECUCAO → CONCLUIDA`
- ✅ Controle de disponibilidade de profissionais e veículos
- ✅ Tratamento global de erros com respostas padronizadas
- ✅ CORS configurado para integração com frontend

---

## 📋 Tipos de operação

| Tipo | Equipe automática | Veículo automático |
|------|------------------|-------------------|
| Remoção CRÍTICO | Motorista + Médico + Enfermeiro | Ambulância UTI |
| Remoção GRAVE | Motorista + Enfermeiro | Ambulância Simples |
| Remoção ESTÁVEL | Motorista | Ambulância Simples |
| Medicamento controlado | Motorista + Farmacêutico | Van Refrigerada ou Utilitário |
| Amostra biológica crítica | Motorista + Enfermeiro | Ambulância Simples |
| Equipamento c/ técnico | Motorista + Técnico | Utilitário de Carga |

---

## 📡 Endpoints

### Autenticação
| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/auth/register` | Cadastra contratante |
| `POST` | `/api/auth/login` | Login e retorna token JWT |

### Operações
| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/operacoes` | Solicita nova operação |
| `GET` | `/api/operacoes` | Lista operações do contratante |
| `GET` | `/api/operacoes/{id}` | Detalhe de uma operação |
| `PUT` | `/api/operacoes/{id}/aprovar` | Aprova operação |
| `PUT` | `/api/operacoes/{id}/iniciar` | Inicia operação |
| `PUT` | `/api/operacoes/{id}/concluir` | Conclui operação |
| `DELETE` | `/api/operacoes/{id}` | Cancela operação |

### Profissionais
| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/profissionais` | Lista todos |
| `GET` | `/api/profissionais/disponiveis` | Lista disponíveis |
| `POST` | `/api/profissionais` | Cadastra profissional |

---

## ▶️ Como rodar

### Pré-requisitos
- Java 21+
- Maven 3.8+

### Passos

```bash
# Clone o repositório
git clone https://github.com/AndreiFros/clinica-transport-api.git
cd clinica-transport-api

# Execute a aplicação
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`

Console H2 (banco em memória): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:clinicadb`
- User: `sa` | Password: *(vazio)*

### Dados de exemplo

Ao iniciar, o sistema popula automaticamente o banco com:
- 6 profissionais (2 motoristas, 1 enfermeiro, 1 médico, 1 farmacêutico, 1 técnico)
- 4 veículos (Ambulância Simples, Ambulância UTI, Van Refrigerada, Utilitário de Carga)

---

## 🧪 Exemplo de uso

### 1. Cadastrar conta
```http
POST /api/auth/register
Content-Type: application/json

{
  "nome": "Andrei Frós",
  "empresa": "Hospital Central",
  "email": "andrei@hospital.com",
  "password": "123456"
}
```

### 2. Solicitar remoção crítica
```http
POST /api/operacoes
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "tipo": "REMOCAO_PACIENTE",
  "origem": "UPA Central",
  "destino": "Hospital Referência",
  "localDestino": "UTI",
  "distanciaKm": 18,
  "nomePaciente": "José Santos",
  "idadePaciente": 67,
  "nivelClinico": "CRITICO",
  "precisaUTI": true
}
```

O sistema seleciona automaticamente: **médico + enfermeiro + motorista + Ambulância UTI**

---

## 🗂️ Estrutura do projeto

```
src/main/java/com/andrei/clinica/
├── ClinicaApiApplication.java
├── DataInitializer.java
├── config/
│   ├── CorsConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── OperacaoController.java
│   └── ProfissionalController.java
├── dto/
│   ├── AuthDTO.java
│   └── OperacaoDTO.java
├── enums/
│   ├── NivelClinico.java
│   ├── NivelUrgencia.java
│   ├── StatusOperacao.java
│   └── TipoProfissional.java
├── exception/
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── NotFoundException.java
├── interfaces/
│   ├── Auditavel.java
│   ├── Custeavel.java
│   ├── Priorizavel.java
│   ├── Rastreavel.java
│   └── Validavel.java
├── model/
│   ├── User.java
│   ├── equipe/Profissional.java
│   ├── operacao/
│   │   ├── OperacaoClinica.java
│   │   ├── RemocaoPaciente.java
│   │   ├── TransporteAmostraBiologica.java
│   │   ├── TransporteEquipamentoMedico.java
│   │   └── TransporteMedicamentoControlado.java
│   └── veiculo/
│       ├── Veiculo.java
│       ├── AmbulanciaSimples.java
│       ├── AmbulanciaUTI.java
│       ├── UtilitarioCarga.java
│       └── VanRefrigerada.java
├── repository/
│   ├── OperacaoRepository.java
│   ├── ProfissionalRepository.java
│   ├── UserRepository.java
│   └── VeiculoRepository.java
├── security/
│   ├── JwtAuthFilter.java
│   └── JwtService.java
└── service/
    ├── AuthService.java
    ├── OperacaoService.java
    ├── ProfissionalService.java
    └── VeiculoService.java
```

---

## 👨‍💻 Autor

**Andrei Frós Duarte**
- LinkedIn: [linkedin.com/in/andrei-frós-533420224](https://linkedin.com/in/andrei-frós-533420224)
- Email: andrei.fros@gmail.com
- GitHub: [github.com/AndreiFros](https://github.com/AndreiFros)
