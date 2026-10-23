# ThinDesk: microsserviço de chamados

Evolução do projeto ThinDesk: um microsserviço em **Java 17 + Spring Boot 3 + MongoDB** com API REST para **abrir, acompanhar e resolver chamados técnicos**.

A autenticação fica no [Login Seguro](https://github.com/lucastadaieski/Sistema-de-Login-Seguro), que emite um **token JWT**. O ThinDesk só valida esse token: ele não guarda senhas e não acessa os dados de usuários.

## Arquitetura

```
                      1. e-mail e senha
Cliente (Postman) ───────────────────────▶ Login Seguro :8080  ──▶ MongoDB (banco loginseguro)
        │        ◀─────────── token ─────── POST /api/auth/token
        │
        │  2. Authorization: Bearer <token>
        └──────────────────────────────────▶ ThinDesk :8081      ──▶ MongoDB (banco loginseguro)
                                             valida o token com a
                                             mesma JWT_SECRET
```

- Os dois serviços usam o mesmo banco, mas o ThinDesk só acessa a coleção chamados. Os dados do usuário vêm do token.
- O ThinDesk valida o token **sozinho**: confere a assinatura com a `JWT_SECRET` e a validade. Ele não chama o Login Seguro a cada requisição, então funciona mesmo com o Login Seguro desligado, enquanto o token for válido.
- Do token, o ThinDesk lê o id do usuário (`sub`), o `email` e o `perfil`.

## Endpoints

| Método | Rota | Quem pode |
|--------|------|-----------|
| `POST` | `/api/chamados` | usuário autenticado |
| `GET` | `/api/chamados/usuario/{id}` | o próprio usuário ou técnico |
| `GET` | `/api/chamados/{id}` | quem abriu o chamado ou técnico |
| `PUT` | `/api/chamados/{id}/resolver` | somente técnico |

Todas as rotas exigem o cabeçalho `Authorization: Bearer <token>`, com o token obtido em `POST http://localhost:8080/api/auth/token` (Login Seguro).

## Regras de negócio

- Ao abrir um chamado, o sistema registra sozinho o **solicitante**, a **data de abertura** e o status **ABERTO**.
- Título e descrição são obrigatórios. A solução também é obrigatória ao resolver.
- Usuário comum só vê os próprios chamados.
- Só técnicos resolvem. Ao resolver, o status vira **RESOLVIDO** e a **data de resolução** é gravada.
- Chamado **resolvido não pode ser alterado** de novo (resposta 409).

Respostas de erro: 400 (dados inválidos), 401 (sem token, token inválido ou expirado), 403 (sem permissão), 404 (não encontrado), 409 (já resolvido).

**Técnicos:** os perfis `TECNICO` e `ADMIN` do Login Seguro podem resolver chamados.

## Como executar

1. Configure e rode o **Login Seguro** (porta 8080), com `JWT_SECRET` no `.env` dele.
2. No ThinDesk, copie `.env.example` para `.env` e preencha:
   - `MONGODB_URI`: conexão do MongoDB Atlas (pode ser o mesmo cluster do Login Seguro);
     `MONGODB_DATABASE`: o mesmo do Login Seguro.
   - `JWT_SECRET`: **exatamente a mesma** do Login Seguro.
3. Rode a classe `com.pfc.ThindeskApplication` no IntelliJ, ou `./mvnw spring-boot:run`. A API sobe em `http://localhost:8081`.

## Testando

- **Postman:** faça `POST http://localhost:8080/api/auth/token` com `{"email": "...", "senha": "..."}`, copie o `token` e use em **Authorization → Bearer Token** nas requisições para a porta 8081.
- **Testes automatizados:** `./mvnw test` (não precisam de banco).

## Estrutura

```
src/main/java/com/pfc
├── ThindeskApplication.java
└── thindesk
    ├── config       SecurityConfig (validação do token JWT e permissões)
    ├── controller   ChamadoController, TratadorDeErros
    ├── dto          ChamadoRequest, SolucaoRequest
    ├── entity       Chamado, StatusChamado
    ├── repository   ChamadoRepository
    └── service      ChamadoService (regras de negócio)
```

## O que mudou em relação ao ThinDesk original

- Removidas as telas, o cadastro de clientes e os horários de atendimento (fora do escopo).
- O usuário fixo `admin/thindesk` foi trocado pela validação do token JWT emitido pelo Login Seguro.
- A senha do banco saiu do código e foi para o `.env`.
- O `Chamado` ganhou título, solicitante, datas, solução e status `ABERTO`/`RESOLVIDO`.
- O `PUT` genérico que alterava qualquer chamado deu lugar ao `PUT /resolver`, que bloqueia chamados já resolvidos.
- O `pom.xml` foi atualizado para Spring Boot 3.3 e Java 17, iguais ao Login Seguro.
