# aio-api

這個專案是用來總結自己的後端技術的，順便把自己在工作中學到的技能做一個總整理，發想取自於
https://github.com/PureFuncInc/pure-backend-practice?tab=readme-ov-file

## 目錄
- [背景](#背景)
- [功能](#功能)
- [安裝](#安裝)
- [使用](#使用)
- [API 文件](#API-文件)
- [貢獻](#貢獻)
- [授權](#授權)

## 功能

主要是有關會員與錢包功能。功能如下：
- 帳戶註冊
- 帳戶登入
- 查詢會員資料
- 更新會員資料
- 帳戶停用、凍結帳戶
- 錢包啟用 (開戶)
- 查詢交易紀錄、錢包餘額
- 存款、提款、轉帳
- 錢包停用、錢包凍結
Check my document for more information.http://localhost:8080/swagger-ui.html
OpenAPI-docs:http://localhost:8080/openapi-docs

## 安裝

填入安裝步驟

### 系統需求

- Java 17
- PostgreSQL
- Maven

### 安裝步驟
1. Clone the repository
2. Create the Database of table schema using "./member_wallet.sql"
3. Run the Project

## Contribute

描述如何參與貢獻你的專案。例如：

1. Fork 這個倉庫
2. 建立一個分支 (`git checkout -b feature/fooBar`)
3. 提交你的修改 (`git commit -am 'Add some fooBar'`)
4. 推送到分支 (`git push origin feature/fooBar`)
5. 發起一個 Pull Request

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.3.2/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.3.2/maven-plugin/reference/html/#build-image)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#data.sql.jpa-and-spring-data)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#web)

### Guides

The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

### 要小心Lombok，套件版本錯了會直接不能使用，且不會爆
#### http://localhost:8080/swagger-ui/index.html Swagger文件路徑
#### http://localhost:8080/v3/api-docs api-doc 路徑
#### @RepositoryRestResource 待研究 @SecurityRequirement 待研究
#### @RestResource(exported = false) 待研究
