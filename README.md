# OSGi I18N

An internationalization (i18n) library for OSGi-based Java applications. It provides locale-aware message resolution from `.properties` files, dynamic proxy-based interface implementations for type-safe message access, and enum message translation — all integrated with the OSGi service registry.

## How It Works

You define a Java interface whose methods correspond to message keys. The library creates a JDK dynamic proxy for that interface and registers it as an OSGi service. When you call a method on the proxy, it resolves the matching property key, loads the correct locale-specific `.properties` file, applies parameter substitution, and returns the localized string.

### Quick Example

1. Define a message interface:

```java
public interface AppMessages {
    String welcomeUser(String name);       // key: "welcomeUser"
    String itemCount(int count);           // key: "itemCount"
}
```

2. Create properties files:

```properties
# AppMessages.properties (default)
welcomeUser=Welcome, {0}!
itemCount=You have {0} items.

# AppMessages_hu.properties (Hungarian)
welcomeUser=Üdvözöljük, {0}!
itemCount={0} elemed van.
```

3. Register and use:

```java
@Reference
I18nService i18nService;

AppMessages messages = i18nService.register(AppMessages.class);
messages.welcomeUser("Alice");  // → "Welcome, Alice!" or "Üdvözöljük, Alice!"
```

## Module Structure

```mermaid
graph TD
    API["i18n-api<br/><i>Interfaces, annotations, SPI</i>"]
    IMPL["i18n-resourcebundle<br/><i>Properties-based implementation</i>"]
    IMPL --> API
```

| Module | Artifact ID | Description |
|--------|-------------|-------------|
| `i18n-api/` | `i18n-api` | Public API: `I18nService`, `EnumI18nService`, `MessageResolver`, `MessageStreamLoader`, `LocaleProvider` interfaces and `@Message`, `@MessageByKey`, `@MessageByEnum` annotations |
| `i18n-resourcebundle/` | `i18n-impl` | Implementation using Java `.properties` files with Guava caching for locale-based resolver instances |

## Architecture

### Message Resolution Pipeline

```mermaid
sequenceDiagram
    participant Client
    participant Proxy as JDK Dynamic Proxy
    participant Handler as InvocationHandler
    participant Cache as Guava LoadingCache
    participant Resolver as ClassBasedPropertiesFileMessageResolver
    participant Loader as LocalePropertiesFileLoader
    participant Props as .properties File

    Client->>Proxy: messages.welcomeUser("Alice")
    Proxy->>Handler: invoke(method, args)
    Handler->>Handler: Resolve property key from method name or annotations
    Handler->>Cache: get(currentLocale)
    Cache->>Resolver: load(locale) [on cache miss]
    Resolver->>Loader: loadProperties(interface, lang, country, variant)
    Loader->>Props: Load most specific matching file
    Props-->>Loader: Properties
    Loader-->>Resolver: Populated properties
    Resolver-->>Cache: MessageResolver instance
    Cache-->>Handler: resolver.get(key)
    Handler->>Handler: SimpleParameterTemplater.templateMessage(template, args)
    Handler-->>Client: "Welcome, Alice!"
```

### Locale Resolution

The locale used for message lookup is determined by this fallback chain:

1. Custom `Supplier<Locale>` passed to `register(clazz, localeSupplier)`
2. `LocaleProvider` OSGi service (optional `@Reference`)
3. Configured `defaultLocale` from OSGi Config Admin
4. `Locale.getDefault()` (JVM default)

### Property File Fallback

`LocalePropertiesFileLoader` looks for the most specific properties file first and falls back to less specific ones:

1. `Messages_en_US_variant.properties`
2. `Messages_en_US.properties`
3. `Messages_en.properties`
4. `Messages.properties`

The file path is derived from the interface's fully qualified name (dots replaced with slashes).

### Key Annotations

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@MessageByKey` | Method | Method takes `(String key, Object[] params)` — looks up an arbitrary key at runtime |
| `@Message(value, key)` | Method | Provides an inline template (`value`) or overrides the property key (`key`) |
| `@MessageByEnum` | Method | Resolves messages for enum constants |

### Class Diagram

```mermaid
classDiagram
    class I18nService {
        <<interface>>
        +register(Class~T~) T
        +register(Class~T~, Supplier~Locale~) T
        +unregister(Class~T~) void
        +format(String key, Object... params) String
        +getTemplate(String key) String
    }

    class EnumI18nService {
        <<interface>>
        +register(Class~Enum~) void
        +register(Class~Enum~, Supplier~Locale~) void
        +unregister(Class~Enum~) void
        +getMessageForEnum(Enum, Object...) String
    }

    class MessageResolver {
        <<interface>>
        +get(String key) String
    }

    class MessageStreamLoader {
        <<interface>>
        +load(String name) InputStream
    }

    class LocaleProvider {
        <<interface>>
        +getLocale() Optional~Locale~
    }

    class I18nServiceImpl {
        -registrationMap Map
        -messageResolverMap Map
        +activate(BundleContext, Config)
    }

    class EnumI18nServiceImpl {
        -resolvers Map
    }

    class ClassAndRequestContextLocaleBasedMessageResolver {
        -localeMessageResolverLoadingCache LoadingCache
    }

    class InterfaceMessageTemplaterInvocationHandler {
        +invoke(Object, Method, Object[]) Object
    }

    I18nServiceImpl ..|> I18nService
    EnumI18nServiceImpl ..|> EnumI18nService
    ClassAndRequestContextLocaleBasedMessageResolver ..|> MessageResolver
    I18nServiceImpl --> ClassAndRequestContextLocaleBasedMessageResolver : creates
    I18nServiceImpl --> InterfaceMessageTemplaterInvocationHandler : creates
    I18nServiceImpl ..> LocaleProvider : optional reference
    ClassAndRequestContextLocaleBasedMessageResolver --> MessageStreamLoader : uses
```

### Dependency Graph

```mermaid
graph LR
    subgraph External
        OSGi["OSGi R6<br/>Core + DS + Metatype"]
        Guava["Google Guava 30<br/>Caching"]
        Lombok["Lombok<br/>Boilerplate reduction"]
    end
    subgraph "osgi-i18n"
        API["i18n-api"]
        IMPL["i18n-resourcebundle"]
    end
    API --> OSGi
    IMPL --> API
    IMPL --> OSGi
    IMPL --> Guava
    IMPL --> Lombok
```

## Build Commands

```bash
# Full build (compile + test + package)
./mvnw clean install

# Run all tests
./mvnw clean test

# Run a specific test class
./mvnw test -pl i18n-resourcebundle -Dtest=I18nServiceImplTest

# Run a specific test method
./mvnw test -pl i18n-resourcebundle -Dtest=I18nServiceImplTest#testChangeLanguage
```

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 1.8 (source) / 21 (runtime) | Language level |
| OSGi | R6 (6.0.0) | Service registry, bundle lifecycle |
| Guava | 30.0-jre | `LoadingCache` for locale-based resolver caching |
| Lombok | 1.18.34 | `@Slf4j`, boilerplate reduction |
| JUnit | 4.12 | Unit testing |
| Mockito | 3.0.0 | Mocking in tests |
| Maven Bundle Plugin | 5.1.2 | OSGi bundle packaging |

## License

[Apache License 2.0](LICENSE)
