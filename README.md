# 🌱 Sprout Example Application

**Sprout Framework**를 기반으로 구축된 회원 관리 예제 애플리케이션입니다.
프레임워크가 제공하는 IoC, DI, AOP, Transaction, MVC 기능을 통합적으로 테스트할 수 있습니다.

## ⚙️ 사전 요구 사항 (Prerequisites)

* **Java 21** 이상
* **Docker** & **Docker Compose** (MySQL 실행용)

## 🚀 실행 가이드 (Getting Started)

### 1. 데이터베이스 실행 (Docker)

프로젝트 루트 경로에서 아래 명령어를 실행하여 MySQL을 구동합니다.
컨테이너 실행 시 `init.sql`이 자동으로 수행되어 테이블 생성 및 초기 데이터가 적재됩니다.

```
# MySQL 실행 (백그라운드 모드)
docker-compose up -d

# (참고) 데이터 초기화가 필요할 경우: 볼륨 삭제 후 재실행
docker-compose down -v
docker-compose up -d
```

- DB 정보: jdbc:mysql://localhost:3306/sprout_db (User: sprout / PW: sprout_password)

### 2. 애플리케이션 실행

**방법 A: Gradle로 실행 (권장)**
build.gradle에 필요한 JVM 옵션이 이미 설정되어 있어 가장 간편합니다.

```
./gradlew :myproject:run
```

**방법 B: IntelliJ IDEA로 실행**
IDE에서 MyProjectApplication의 main()을 직접 실행하려면, Java 17+ 모듈 시스템 제약(CGLIB 호환성) 해결을 위해 VM 옵션 설정이 필수입니다.

1. Run/Debug Configurations 메뉴 진입
2. Modify options → Add VM options 클릭
3. 아래 옵션을 입력칸에 붙여넣기:

```
java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED
```

## 🧪 기능 테스트

서버가 성공적으로 실행되면 (http://localhost:8080), 아래 방법으로 기능을 검증할 수 있습니다.

### 📡 API 엔드포인트

| Feature | Method | URL    | Description                                |
|---------|--------|--------|--------------------------------------------|
| 회원 가입   | POST   | /join  | Form Data (name, age) 전송 시 DB 저장 (트랜잭션 커밋) |
| 단건 조회   | POST   | /find  | Form Data (id) 전송 시 회원 정보 반환               |
| 전체 조회   | GET    | /users | 전체 회원 목록 반환                                |

## 📂 프로젝트 구조

- sprout-core: IoC 컨테이너 및 빈 생명주기 관리
- sprout-data: DB 연결, 트랜잭션 매니저, 리포지토리 프록시
- sprout-web: 웹 요청 처리 및 디스패처 서블릿
- myproject: 실제 비즈니스 로직이 구현된 예제 모듈