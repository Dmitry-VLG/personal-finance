FinTrack — Personal Finance (CLI, Java, Maven, Multi-Module)

Мульти-модульное CLI-приложение для учёта личных финансов.

Архитектура
•	fintrack-core — доменные модели, API, SPI
•	fintrack-infra — файловые репозитории (Jackson), PBKDF2, сервисы
•	fintrack-cli — точка входа + сборка fat-jar (maven-shade-plugin)

Требования
•	Java 21+ (JDK). Компиляция таргетит байткод Java 17 через maven.compiler.release=17.
•	Maven не обязателен — в репозитории есть Maven Wrapper (mvnw/mvnw.cmd).

Сборка
Windows (PowerShell):
.\mvnw.cmd -q -DskipTests clean package
macOS/Linux:
./mvnw -q -DskipTests clean package
Итоговый JAR:
fintrack-cli/target/fintrack-cli-0.1.0-SNAPSHOT.jar

Запуск
Windows (PowerShell):
java -jar ".\fintrack-cli\target\fintrack-cli-0.1.0-SNAPSHOT.jar"
macOS/Linux:
java -jar ./fintrack-cli/target/fintrack-cli-0.1.0-SNAPSHOT.jar
Если кириллица в консоли отображается некорректно (Windows), перед запуском выполните: chcp 65001

Команды CLI
help
register <login>
login <login>
logout
income <category> <amount> [note]
expense <category> <amount> [note]
budget set <category> <limit>
summary [--from yyyy-mm-dd] [--to yyyy-mm-dd]
exit

Пример сценария
register ivan
login ivan
budget set food 10000
expense food 1200 обед
income salary 50000
expense food 9500 продукты
summary
logout
exit

Где лежат данные
./data/users.json
./data/wallet_<login>.json

Структура проекта
pom.xml                   # parent (packaging=pom, список modules)
fintrack-core/            # домен + API/SPI (без Jackson/IO)
fintrack-infra/           # файловые репозитории (Jackson), PBKDF2, сервисы
fintrack-cli/             # CLI, main class, maven-shade-plugin
mvnw, mvnw.cmd, .mvn/     # Maven Wrapper

Полезные команды
Сборка без тестов:
.\mvnw.cmd -q -DskipTests clean package
Полная проверка с тестами:
.\mvnw.cmd verify
Очистка:
.\mvnw.cmd clean

Замечания по безопасности/качеству
•	Пароли хранятся в виде хэшей PBKDF2.
•	Деньги — BigDecimal.
•	Даты/время — Instant (операции), фильтрация по LocalDate.

Типичные проблемы и решения
• 'java' is not recognized: Установите JDK 21+, проверьте переменные JAVA_HOME и Path (%JAVA_HOME%\bin).
• 'mvnw.cmd not found': Запускайте из корня репозитория; при необходимости используйте глобальный Maven: mvn -q -DskipTests clean package.
• Unable to access jarfile …-shaded.jar: Запускайте итоговый JAR fintrack-cli-0.1.0-SNAPSHOT.jar (shade подменяет обычный артефакт).
• Кириллица 'не работает': Выполните chcp 65001 перед запуском в Windows PowerShell.


