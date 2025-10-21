# Project Rule for SWP_CodeGen_Agent

## 1. Project Structure
The project must follow this **clean, layered structure**:

spring_boot.project_swp
├── config
├── controller
├── dto
│ ├── request
│ └── response
├── entity
├── exception
│ ├── Print_Exception
│ └── HandleException
├── mapper
├── repository
└── service
├── impl
└── RoleService/UserService

## 2. Layering Rules
- **Controller Layer**: handles REST endpoints, delegates to Service Layer, returns `ResponseEntity<DTO>`  
- **Service Interface**: declares business methods (authentication, CRUD)  
- **Service Implementation**: contains full business logic, uses Repository & Mapper  
- **Repository Layer**: extends `JpaRepository<Entity, ID>`  
- **Entity Layer**: annotated with JPA/Hibernate (`@Entity`, `@Table`)  
- **DTO Layer**: request/response objects for validation and responses  
- **Mapper Layer**: MapStruct interfaces to map Entity ↔ DTO  
- **Exception Layer**: custom exceptions + global handler (`@RestControllerAdvice`)  
- **Config Layer**: CORS, DataInitializer, other app-level configurations  

---

## 3. Naming Conventions
- **Packages**: lowercase with underscores (`spring_boot.project_swp.controller`)  
- **Classes**: PascalCase  
- **Interfaces**: PascalCase ending with `Service`  
- **DTOs**: `<Entity>Request` or `<Entity>Response`  
- **Repository**: `<Entity>NameRepository`  
- **Methods**: camelCase, descriptive  
- **Variables**: camelCase, meaningful

---

## 4. Database Rules
- Use **SQL Server 2019**  
- Table and column names in **PascalCase** (`Users`, `Roles`, `UserId`)  
- Relationships:
  - `User → Role`: ManyToOne, LAZY, `@JsonIgnore` back reference  
  - `Role → User`: OneToMany  
- ID: `@GeneratedValue(strategy = GenerationType.IDENTITY)`  
- Timestamps: `@CreationTimestamp` for createdAt  
- Default values via **builder or field initializer**  

---

## 5. Configuration
- **CorsConfig**: allow only specified frontend URLs  
- **DataInitializer**: insert default roles (`admin`, `staff`, `user`) and admin user  
- **application.properties**: correct datasource, JPA, server port, mail settings  

---

## 6. Coding Style
- **Use Lombok**: `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Builder`, `@FieldDefaults(level = AccessLevel.PRIVATE)`  
- Avoid lambda and Stream API  
- Reuse helper methods to reduce repeated logic  
- Keep code readable, concise, and logically complete  

---

## 7. Exception Handling
- Global exception handler using `@RestControllerAdvice`  
- Handle:
  - Validation errors → 400 with field details  
  - NotFound → 404  
  - Conflict → 409  
  - Generic errors → 500 with stack trace  

---

## 8. Full-Layer Rule
When generating new code, always maintain **multi-layer architecture**:  
**Controller → Service → ServiceImpl → Repository → Entity → DTO → Mapper → Exception → Config**

---

## 9. Code Samples
Use the following **code samples** as the standard for naming, style, DTO mapping, exception handling, service structure, controller structure, configuration, and repository.  
*(Insert all previously provided code blocks exactly as given, including:)*

- `application.properties`  
- `CorsConfig`  
- `AuthController`, `UserController`  
- `DTOs` (`UserLoginRequest`, `UserRegistrationRequest`, `UserLoginResponse`, `UserRegistrationResponse`, `UserResponse`)  
- `Entity` (`User`, `Role`)  
- `Exception` (`NotFoundException`, `ConflictException`, `HandleException`)  
- `Mapper` (`UserMapper`)  
- `Repository` (`UserRepository`)  
- `Service` (`UserService`)  
- `ServiceImpl` (`UserServiceImpl`)  
- `DataInitializer`  
- `pom.xml` (Maven config with MapStruct, Lombok, Spring Boot dependencies)

---

## 10. Additional Rules
- Follow **User Rule** exactly for coding style, naming, validation, mapping, exception handling  
- Code must be **optimized, readable, and logically complete**  
- No lambda, no Stream, no password hashing