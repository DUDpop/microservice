Запуск микросервиса

Требования:
  - Java JDK 21;
  - PostgreSQL (БД: microservice, логин/пароль: postgres/postgres).

Запуск

1. PostgreSQL
   
Убедитесь, что PostgreSQL запущен и создана БД:
"CREATE DATABASE microservice;"

2. Укажите путь к Java (если PowerShell не видит JDK 21)
   
powershell
$env:Path = " "путь к jdk 21" + bin;" + $env:Path
java -version

3. Запустите приложение
   
windows powershell
cd "путь к микросервису"
.\mvnw.cmd spring-boot:run

4. Проверьте работу
   
Перейдите по адресу:
http://localhost:8081/swagger-ui.html

При первом запуске Maven автоматически загрузит зависимости.
