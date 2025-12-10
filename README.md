# 🛰️ simple-netty-server

비동기 이벤트 기반 네트워크 서버 프로젝트로,
**Spring Boot + Netty + Reactor + R2DBC** 기반의 고성능 서버 구조를 실험 및 구현한 프로젝트입니다.
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

**Spring Boot 통합**:
- `@SpringBootApplication`으로 애플리케이션 부트스트랩
- `@Service`로 CommandRegistry, CommandDispatcher 등 DI 관리
- `@Bean`과 `CommandLineRunner`로 Netty 서버 자동 시작
- `application.properties`로 중앙화된 설정 관리

---

## 🧠 주요 컴포넌트

| 모듈 | 역할 |
|------|------|
| **netcore** | 메시지·핸들러·서비스의 공통 인터페이스 제공 |
| **server** | 실제 서버 로직 (Command 분배, 실행, 세션 관리 등) |
| **dao** | R2DBC 기반의 비동기 데이터 접근 계층 |

---

### 🧩 netcore 모듈

#### `com.znzlspt.netcore.handler.InboundHandlerBindHelper`
Netty 이벤트를 `ServiceHandler` 구현체 (`SimpleNettyRunner`)로 위임.

#### `com.znzlspt.netcore.message.Message`
`ByteBuf` 기반 커스텀 메시지 객체. 직렬화/역직렬화 및 타입 안전 접근 지원.

---

### 🧩 server 모듈

#### `com.znzlspt.server.Main`
- `@SpringBootApplication`으로 Spring Boot 애플리케이션 진입점
- `scanBasePackages`로 `server`, `dao` 패키지 컴포넌트 스캔
- `@Bean CommandLineRunner`를 통해 Netty 서버 자동 시작

#### `com.znzlspt.server.netty.SimpleNettyRunner`
- Netty `ServerBootstrap` 구성 (`@Service`로 Spring 관리)
- `IdleStateHandler`, `Codec`, `InboundHandlerBindHelper` 등록
- 채널 그룹 관리
- `CommandDispatcher`를 통해 커맨드 분배

#### `com.znzlspt.server.service.command.ServerCommandService`
모든 서버 명령(`Login`, `Echo`, `Logout` 등)의 기본 인터페이스.

#### `com.znzlspt.server.service.CommandDispatcher`
- `@Service`로 Spring Bean 등록
- 수신된 `Message`를 `CommandRegistry`로 전달하고,
  등록된 `ServerCommandService` 구현체를 생성해 `execute(Message)` 실행

#### `com.znzlspt.server.service.CommandRegistry`
- `@Service`로 Spring Bean 등록
- 명령 코드와 `ServerCommandService` 구현체(`Login`, `Logout`, `TimeSync`, `CHAT_ECHO`, `CHAT_ALL`)를 **명시적 코드로 등록**
- `DaoModule`을 생성자 주입받아 의존성 관리

```java
@Service
public class CommandRegistry {
    private final DaoModule daoModule;

    public CommandRegistry(DaoModule daoModule) {
        this.daoModule = daoModule;
        registerDefaults();
    }

    private void registerDefaults() {
        register(RequestCommand.LOGIN, Login::new);
        register(RequestCommand.LOGOUT, Logout::new);
        register(RequestCommand.TIME_SYNC, TimeSync::new);
        register(RequestCommand.CHAT_ECHO, Echo::new);
        register(RequestCommand.CHAT_ALL, Say::new);
    }
}
```

#### `com.znzlspt.server.command.*`
각 CommandService 구현체 집합.
예시:
```java
public class Login extends ServerCommandService {
    @Override
    public void execute(Message message) {
        // 로그인 로직
    }
}
```

---

### 🗄️ dao 모듈

#### `com.znzlspt.dao.DaoModule`
- `@Service`로 Spring Bean 등록
- R2DBC 기반 Reactive DAO 중심. 모든 SQL은 Mono/Flux로 수행
- Spring의 `ConnectionFactory` 자동 구성 활용

#### `com.znzlspt.dao.DaoConnectionPool`
`io.r2dbc.pool.ConnectionPool` 설정 관리
(초기 5개, 최대 20개, Idle 30분 유지)

#### Spring R2DBC 설정
`application.properties`에서 중앙 관리:
```properties
spring.r2dbc.url=r2dbc:mssql://tester:nmklop90@localhost:1433/test
```

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
| 프레임워크 | Spring Boot 3.4.1 |
| DI/IoC | Spring Framework 6.2.1 |
| 네트워킹 | Netty 4.2.6 |
| 비동기 프레임워크 | Reactor (Mono / Flux) |
| 데이터베이스 | Microsoft SQL Server |
| DB 접근 | Spring Data R2DBC + ConnectionPool |
| 로깅 | SLF4J + Logback |
| 빌드도구 | Gradle |

---

## 🧠 설계 요약

- **Spring Boot 기반 DI/IoC**: `@Service` 로 의존성 자동 관리
- **CommandDispatcher 패턴**: 동적 명령 실행
- **명시적 Command 등록**: 어노테이션 스캔 없이 코드로 직접 등록
- **완전 비동기 구조**: Netty + R2DBC + Reactor
- **3계층 분리**: netcore/server/dao 모듈 완전 분리
- **중앙화된 설정**: `application.properties`로 통합 관리  

---

## 📦 빌드 & 실행

```bash
# Gradle로 빌드 및 실행
./gradlew clean build
./gradlew :server:bootRun

# 또는 JAR 파일로 실행
./gradlew :server:bootJar
java -jar server/build/libs/server-1.0-SNAPSHOT.jar
```

### 설정 파일
`server/src/main/resources/application.properties`:
```properties
# R2DBC 데이터베이스 연결
spring.r2dbc.url=r2dbc:mssql://tester:nmklop90@localhost:1433/test

# Netty 서버 포트
netty.server.port=20999

# 로깅 레벨
logging.level.com.znzlspt=DEBUG
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

- **Spring Framework** — Apache License 2.0
- **Spring Boot** — Apache License 2.0
- **Spring Data R2DBC** — Apache License 2.0
- **Netty** — Apache License 2.0
- **Reactor Core** — Apache License 2.0
- **R2DBC (SPI / Pool)** — Apache License 2.0
- **SLF4J** — MIT License
- **Logback** — EPL 1.0 (Eclipse Public License)
