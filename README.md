# 🛰️ simple-netty-server

비동기 이벤트 기반 네트워크 서버 프로젝트로,  
**Netty + Reactor + R2DBC** 기반의 고성능 서버 구조를 실험 및 구현한 프로젝트입니다.  
Command 기반 메시지 처리 구조를 통해 확장성과 유지보수성을 극대화하였습니다.

---

## 🧩 전체 모듈 구조

```
📦 simple-netty-server(parent)
 ├──📦 server(module)
 │  ├── 📂 server(package)
 │  │   ├── 📂 command
 │  │   │   ├── Echo.java
 │  │   │   ├── Login.java
 │  │   │   ├── Logout.java
 │  │   │   ├── Say.java
 │  │   │   └──  TimeSync.java
 │  │   ├── 📂 netty
 │  │   │   └──  SimpleNettyRunner.java
 │  │   ├── 📂 service
 │  │   │   ├── 📂 command
 │  │   │   │   ├── NotificationCommand.java
 │  │   │   │   ├── RequestCommand.java
 │  │   │   │   └── ResponseCommand.java
 │  │   │   ├── CommandDispatcher.java
 │  │   │   ├── CommandRegistry.java
 │  │   │   ├── CommandService.java
 │  │   │   └── ServerTime.java
 │  │   ├── MyUser.java
 │  │   └── Main.java
 │  └── 📂 resources
 │      └── logback.xml 
 │
 ├── 📦 netcore(module)
 │   ├── 📂 netcore(package)
 │   │   ├── 📂 command
 │   │   │   ├── Command.java
 │   │   │   └── CommandService.java
 │   │   ├── 📂 handler
 │   │   │   ├── InboundHandlerBindHelper.java
 │   │   │   └── ServiceHandler.java
 │   │   └── 📂 message
 │   │       ├── Codec.java
 │   │       └── Message.java
 │   └── 📂 resources
 │       └── logback.xml    
 │
 └── 📦 dao(module)
     ├──📂 dao(package)
     │   ├── 📂 mapper
     │   │   ├── R2dbcMapper.java
     │   │   └── RowMapper.java
     │   ├── 📂 model
     │   │   ├── LocalUser.java
     │   │   ├── Login.java
     │   │   └── PubUser.java
     │   ├── 📂 util
     │   │   ├── BCryptHelper.java
     │   │   └── PropertyHelper.java
     │   ├── DaoConnectionPool.java
     │   └── DaoModule.java
     ├── 📂 resources
     │   ├── dao.properties
	 │   └── logback.xml
     └── build.gradle
```
---

## ⚙️ 계층 아키텍처

Client → Netty ServerBootstrap → CommandDispatcher → CommandRegistry → CommandService 구현체  
→ DaoModule (R2DBC 비동기 DB 접근) → MSSQL / ConnectionPool

---

## 🧠 주요 컴포넌트

| 모듈 | 역할 |
|------|------|
| **netcore** | 메시지·핸들러·서비스의 공통 인터페이스 제공 |
| **server** | 실제 서버 로직 (Command 분배, 실행, 세션 관리 등) |
| **dao** | R2DBC 기반의 비동기 데이터 접근 계층 |

---

### 🧩 netcore 모듈

#### `com.znzlspt.netcore.command.CommandService`
모든 서버 명령(`Login`, `Echo`, `Logout` 등)의 기본 인터페이스.

#### `com.znzlspt.netcore.handler.InboundHandlerBindHelper`
Netty 이벤트를 `ServiceHandler` 구현체 (`SimpleNettyRunner`)로 위임.

#### `com.znzlspt.netcore.message.Message`
`ByteBuf` 기반 커스텀 메시지 객체. 직렬화/역직렬화 및 타입 안전 접근 지원.

---

### 🧩 server 모듈

#### `com.znzlspt.server.netty.SimpleNettyRunner`
- Netty `ServerBootstrap` 구성  
- `IdleStateHandler`, `Codec`, `InboundHandlerBindHelper` 등록  
- 채널 그룹 관리  
- `CommandDispatcher`를 통해 커맨드 분배

#### `com.znzlspt.server.service.CommandDispatcher`
수신된 `Message`를 `CommandRegistry`로 전달하고,  
등록된 `CommandService` 구현체를 생성해 `execute(Message)` 실행.

#### `com.znzlspt.server.service.CommandRegistry`
명령 코드와 `CommandService` 구현체(`Login`, `Logout`,  `TimeSync`, `CAHT_ECHO`, `CHAT_ALL` )를 **명시적 코드로 등록**  

```java
        register(RequestCommand.LOGIN, Login::new);
        register(RequestCommand.LOGOUT, Logout::new);
        register(RequestCommand.TIME_SYNC, TimeSync::new);
        register(RequestCommand.CHAT_ECHO, Echo::new);
        register(RequestCommand.CHAT_ALL, Say::new);
```

#### `com.znzlspt.server.command.*`
각 CommandService 구현체 집합.  
예시:
```java
public class Login extends CommandService {
    @Override
    public void execute(Message message) {
        // 로그인 로직
    }
}
```

---

### 🗄️ dao 모듈

#### `com.znzlspt.dao.DaoModule`
R2DBC 기반 Reactive DAO 중심. 모든 SQL은 Mono/Flux로 수행.

#### `com.znzlspt.dao.util.DaoConnectionPool`
`io.r2dbc.pool.ConnectionPool` 설정 관리  
(초기 5개, 최대 20개, Idle 30분 유지)

#### `com.znzlspt.dao.util.PropertyHelper`
DB 접속 URL 생성:  
`r2dbc:mssql://tester:nmklop90@localhost:1433/test`

#### `com.znzlspt.dao.mapper.R2dbcMapper`
`Result` 객체를 매핑하여 `RowMapper`를 통해 POJO로 변환.

---

## 📡 Command 실행 흐름

Client → Codec.decode() → InboundHandlerBindHelper  
→ SimpleNettyRunner.channelRead() → CommandDispatcher.dispatch()  
→ CommandRegistry.create() → CommandService.execute()  
→ DaoModule (Reactive DB Query)

---

## 🧰 기술 스택

| 구분 | 기술 |
|------|------|
| 언어 | Java 21 |
| 네트워킹 | Netty |
| 비동기 프레임워크 | Reactor (Mono / Flux) |
| 데이터베이스 | Microsoft SQL Server |
| DB 접근 | R2DBC + ConnectionPool |
| 로깅 | SLF4J + Logback |
| 빌드도구 | Gradle |

---

## 🧠 설계 요약

- CommandDispatcher 패턴으로 동적 명령 실행  
- 명시적 Command 등록 기반 (어노테이션 미사용)  
- Netty + R2DBC 완전 비동기 구조  
- netcore/server/dao 3계층 완전 분리  

---

## 📦 빌드 & 실행

```bash
gradle clean build
java -jar server/build/libs/simple-netty-server.jar
```

---

## 🧑‍💻 작성자
**znzlspt17 (개발자)**  
- Backend
- Netty, Reactor, R2DBC 기반 서버 설계 및 최적화에 집중  
- GitHub: [https://github.com/znzlspt17](https://github.com/znzlspt17)
- E-mail: znzlsit@naver.com

---

## 🪶 라이선스 안내

이 프로젝트는 다음과 같은 오픈소스 라이브러리를 사용하고 있습니다:

- **Netty** — Apache License 2.0  
- **Reactor Core** — Apache License 2.0  
- **R2DBC (SPI / Pool)** — Apache License 2.0  
- **SLF4J** — MIT License  
- **Logback** — EPL 1.0 (Eclipse Public License)
