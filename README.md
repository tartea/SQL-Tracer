# 🧪 SQL Tracer - sql agent

> 使用 Java Agent 技术拦截 MyBatis 执行的 SQL，并输出至控制台及本地文件。

---

## 📌 项目简介

本项目通过 **Java Agent** 技术对运行中的 MyBatis 应用进行字节码增强，实现对所有执行的 SQL 语句的捕获和记录。可以将 SQL 输出到控制台或写入本地日志文件，便于调试、性能分析或审计用途。

无需修改业务代码，只需添加一个 JVM 启动参数即可启用 SQL 拦截功能。

---

## 🚀 功能特性

- ✅ 支持拦截 MyBatis 所有执行的 SQL（包括动态 SQL 和参数绑定）
- ✅ 支持将 SQL 写入本地文件（支持自定义路径）
- ✅ 零侵入性：无需修改源码，仅需启动时添加 `-javaagent` 参数

---

## ⚙️ 使用方式

### 1. 编译 Agent

```bash
cd agent
mvn install
```

构建完成后，Agent JAR 文件位于：

```
target/SQL-Tracer.jar
```

### 2. 启动你的项目并启用 Agent

在启动命令中加入如下 JVM 参数：

```bash
java -javaagent:SQL-Tracer.jar -jar your-application.jar
```

### 3. 查看输出

- 同时会将 SQL 写入本地文件：
  ```
  sql-tracer.log
  ```

> ✅ 可通过配置文件或启动参数自定义日志路径和格式（见下文）

---

## 🛠️ 自定义配置（可选）

你可以在启动时通过 `options` 参数传入配置：

```bash
-javaagent:/path/to/mybatis-sql-tracer-agent.jar=logFilePath=D:\edition\cloud\12.log,outputToConsole=true
```

| 配置项 | 说明 |
|--------|------|
| `logFilePath` | 日志文件路径（默认 `当前项目下的sql-tracer.log`） |
| `outputToConsole` | 控制台输出，可选 `true`, `false` |

---

## 🔒 注意事项

- 本工具适用于开发/测试环境，不建议在生产环境中长期开启。
- 若应用中有大量 SQL，日志文件可能会增长迅速。
- 每一次重新启动，之前的文件都会被重命名，然后产生一个新的文件。

---

## 🤝 贡献与反馈

欢迎提交 Issues 和 PR！如果你希望扩展以下功能，也欢迎参与：

- 支持更多 ORM 框架（如 Hibernate）
- 支持远程日志上报
- 支持性能统计和慢 SQL 检测

---

## 📄 License

MIT License

