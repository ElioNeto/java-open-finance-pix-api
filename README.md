# 🏦 Java Open Finance Pix API

API REST simulando **iniciação de pagamentos Pix** conforme padrões do **Open Finance Brasil (Fase 3)**.

Desenvolvido com Java 17 + Spring Boot 3.2, demonstrando arquitetura orientada a eventos com Kafka, persistência JPA + PostgreSQL, autenticação JWT e containerização completa com Docker.

---

## 📖 O que é Open Finance?

**Open Finance** permite que **TPPs (Third Party Providers)** iniciem pagamentos e acessem dados financeiros via APIs padronizadas, mediante consentimento do usuário.

> Este projeto simula o endpoint de iniciação conforme **padrão Open Finance Brasil (fase 3)**.

- 🔗 [Documentação Oficial Open Finance Brasil](https://openfinancebrasil.atlassian.net/)
- O **SPI (Sistema de Pagamentos Instantâneos)** é operado pelo Banco Central e processa transações Pix em tempo real
- TPPs se comunicam com a instituição detentora via APIs padronizadas para iniciar pagamentos

---

## 🏗️ Arquitetura

```
┌─────────────┐     ┌─────────────┐     ┌──────────────────┐
│   Client    │────▶│ Controller  │────▶│    Service       │
│  (TPP/App)  │     │ (REST API)  │     │ (Business Logic) │
└─────────────┘     └─────────────┘     └────────┬─────────┘
                                                  │
                         ┌────────────────────────┼──────────────┐
                         │                        │              │
                         ▼                        ▼              ▼
                  ┌─────────────┐        ┌──────────────┐  ┌──────────┐
                  │ Repository  │        │   Kafka      │  │  JPA     │
                  │ (JPA/DB)    │        │   Producer   │  │ Entity   │
                  └─────────────┘        └──────┬───────┘  └──────────┘
                                                │
                                                ▼
                                        ┌──────────────┐
                                        │   Kafka      │
                                        │   Consumer   │
                                        └──────┬───────┘
                                               │
                                               ▼
                                  Atualiza Status no DB
                          (PENDING → PROCESSING → COMPLETED/FAILED)
```

### Fluxo de Pagamento

1. **TPP** autentica via `POST /auth/login` e recebe JWT
2. **TPP** inicia pagamento via `POST /pix/payments` (com Bearer token)
3. **Service** persiste pagamento com status `PENDING`
4. **Kafka Producer** publica evento `PixEvent` no tópico `pix-payments`
5. **Kafka Consumer** processa assincronamente:
   - Atualiza status para `PROCESSING` (simula validação SPI)
   - Atualiza status para `COMPLETED` (90%) ou `FAILED` (10%)
6. **TPP** consulta status via `GET /pix/payments/{id}`
7. **Banco Central** pode notificar via `POST /webhooks/pix-callback`

---

## 🛠️ Stack

| Componente | Tecnologia |
|-----------|------------|
| Backend | Java 17 + Spring Boot 3.2 |
| Persistência | Spring Data JPA + PostgreSQL |
| Mensageria | Apache Kafka |
| Autenticação | Spring Security + JWT (jjwt 0.11) |
| Containerização | Docker + Docker Compose |
| Build | Maven 3.9 |

---

## 🚀 Como Executar

### Pré-requisitos
- Docker e Docker Compose instalados

### 1. Clone o repositório
```bash
git clone https://github.com/ElioNeto/java-open-finance-pix-api.git
cd java-open-finance-pix-api
```

### 2. Suba todos os serviços
```bash
docker-compose up --build
```

Isso iniciará:
- **PostgreSQL** na porta `5432`
- **Zookeeper** na porta `2181`
- **Kafka** na porta `9092`
- **API** na porta `8080`

### 3. Verifique a saúde da API
```bash
curl http://localhost:8080/actuator/health
```

---

## 📋 Endpoints

### Autenticação
```bash
# Login (obtém JWT)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "tpp-user", "password": "senha123"}'
```

### Pagamentos Pix
```bash
# Criar pagamento Pix
curl -X POST http://localhost:8080/pix/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "pixKey": "joao@email.com",
    "amount": 150.00,
    "description": "Pagamento de serviço"
  }'

# Consultar status do pagamento
curl http://localhost:8080/pix/payments/{id} \
  -H "Authorization: Bearer {token}"
```

### Webhook (Simulação Banco Central)
```bash
curl -X POST http://localhost:8080/webhooks/pix-callback \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "{id}",
    "status": "COMPLETED",
    "message": "Pagamento confirmado pelo SPI"
  }'
```

---

## 📊 Estados do Pagamento

```
PENDING → PROCESSING → COMPLETED
                    ↘ FAILED
```

| Status | Descrição |
|--------|-----------|
| `PENDING` | Pagamento criado, aguardando processamento |
| `PROCESSING` | Sendo validado pelo SPI simulado |
| `COMPLETED` | Confirmado (90% dos casos simulados) |
| `FAILED` | Rejeitado (10% simulado) |

---

## 🔐 Credenciais de Teste

| Campo | Valor |
|-------|-------|
| Username | `tpp-user` |
| Password | `senha123` |

---

## 🧪 Testando o Fluxo Completo

```bash
# 1. Autenticar e capturar token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"tpp-user","password":"senha123"}' | jq -r .token)

# 2. Criar pagamento
PAYMENT=$(curl -s -X POST http://localhost:8080/pix/payments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pixKey":"destino@email.com","amount":100.00,"description":"Open Finance Demo"}')
echo $PAYMENT

# 3. Aguardar processamento assíncrono via Kafka (≈5s)
sleep 6

# 4. Consultar status final
ID=$(echo $PAYMENT | jq -r .id)
curl -s http://localhost:8080/pix/payments/$ID \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

## 🏛️ Padrões Enterprise Aplicados

- **SOLID**: Services desacoplados, responsabilidade única
- **DTOs vs Entities**: Separação clara entre camadas
- **Exception Handling**: `@ControllerAdvice` global centralizado
- **Bean Validation**: `@Valid`, `@NotNull`, `@DecimalMin`
- **Structured Logging**: SLF4J com JSON logs
- **Event-Driven**: Desacoplamento via Kafka (eventual consistency)
- **Stateless Auth**: JWT sem estado no servidor

---

## 📁 Estrutura do Projeto

```
src/main/java/com/elioneto/pixapi/
├── controller/          # REST endpoints (PixPayment, Auth, Webhook)
├── service/             # Lógica de negócio
├── repository/          # JPA repositories
├── model/               # Entities JPA (PixPayment, PixStatus)
├── dto/                 # Request/Response DTOs
├── kafka/               # Producer/Consumer + PixEvent
├── security/            # JWT config, filtros e SecurityConfig
├── config/              # KafkaConfig
└── exception/           # GlobalExceptionHandler + custom exceptions
```

---

> Projeto desenvolvido para demonstrar conceitos de **Open Finance Brasil**, **arquitetura orientada a eventos** e **sistemas de pagamento instantâneo** com Java/Spring Boot.
