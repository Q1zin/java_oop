# Messenger

"Многофункциональный" клиент-серверный мессенджер с графическим интерфейсом, реализованный на Java, Rust (Tauri) и JavaScript. Поддерживает JSON и XML протоколы обмена.

## Структура проекта

```bash
.
├── client           # Клиентская часть (веб-интерфейс + Java-клиент)
│   ├── client-java  # Java-клиент (Gradle)
│   └── src-tauri    # Rust + Tauri оболочка
├── server           # Сервер на Java (Gradle)
└── README.md
```

## Сборка

### Сервер
```bash
cd server
gradle wrapper
./gradlew run
```
Настройки сервера: server/server/server.properties

### Клиент
```bash
cd client
npm install
npm run dev
```

Чтобы собрать .dmg необходимо прописать такие команды:
```bash
cd client
npm install
CI=true npm run build
```
.dmg вы найдёте по пути: client/src-tauri/target/release/bundle/dmg/messanger-tauri_2.2.8_aarch64.dmg 

## Конфигурация

### Сервер
Файл server.properties:
```
port=4042
db_path=database.db
xml_mode=false
```

### Клиент
Настройка ip и port: client/src/scripts/utils.js:
```
export async function initApp(host = "localhost", port = 4042)
```

Настройка send mode (xml/json): client/client-java/src/main/java/client/ClientConnection.java
```
private boolean xmlMode = false
```

## Подключение в локальной сети:

### Сервер
Необходимо узнать ip: прописываем в консоле (обычно самый верхний - нужный ip)
```
ifconfig | grep "inet " | grep -v 127.0.0.1
```

### Клиент
Подключитель к одной сети с сервером. Проверьте связь с сервером:
```
ping <ip_server'a>
```

Если связь есть, тогда меняем "localhost" на "<ip_server'a>" в client/src/scripts/utils.js
```
export async function initApp(host = "<ip_server'a>", port = 4042)
```

Отлично, теперь запускаете сервер и потом запускаете лаунчеры мессенджера.

🧪🧪 Тестил только на macos 🧪🧪