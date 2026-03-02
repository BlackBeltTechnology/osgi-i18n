# i18n-api Specification

## Purpose

Defines the public API contracts for the OSGi I18N library: service interfaces for registering and resolving localized messages (both interface-proxy-based and enum-based), SPI interfaces for message resolution and stream loading, annotations for controlling message key mapping, and a utility for BCP47 locale parsing.

## Architecture

The API module contains no implementation — only interfaces, annotations, and a utility class:

- **`I18nService`** — primary service interface for registering message interfaces as dynamic proxies and resolving messages by key
- **`EnumI18nService`** — service interface for registering enum classes and resolving localized messages for enum constants
- **`MessageResolver`** — SPI for resolving a message string from a key (locale handling is delegated to the implementation)
- **`MessageStreamLoader`** — SPI for loading an `InputStream` by resource name (abstraction over classloader resource loading)
- **`LocaleProvider`** — optional OSGi service providing the current `Locale`
- **`@Message`** — method annotation for inline templates or property key overrides
- **`@MessageByKey`** — method annotation marking a method as a dynamic key resolver (takes `String key, Object[] params`)
- **`@MessageByEnum`** — method annotation for enum-based message resolution
- **`I18NUtil`** — utility for parsing BCP47 language tags into `java.util.Locale`

## Requirements

### Requirement: Interface proxy registration

The `I18nService` SHALL allow registering a Java interface class and returning a proxy instance whose methods resolve to localized messages.

#### Scenario: Register interface with default locale
- **GIVEN** an `I18nService` instance is active
- **WHEN** `register(MyMessages.class)` is called
- **THEN** a proxy implementing `MyMessages` is returned and registered as an OSGi service

#### Scenario: Register interface with custom locale supplier
- **GIVEN** an `I18nService` instance is active
- **WHEN** `register(MyMessages.class, () -> Locale.FRENCH)` is called
- **THEN** a proxy is returned that resolves messages using French locale

### Requirement: Interface proxy unregistration

The `I18nService` SHALL allow unregistering a previously registered interface, removing it from the OSGi service registry.

#### Scenario: Unregister a registered interface
- **GIVEN** `MyMessages.class` has been registered via `register()`
- **WHEN** `unregister(MyMessages.class)` is called
- **THEN** the OSGi service registration is removed and the proxy is no longer available

### Requirement: Key-based message formatting

The `I18nService` SHALL provide `format(String key, Object... params)` to look up a message template by key across all registered resolvers and apply parameter substitution.

#### Scenario: Format a message with parameters
- **GIVEN** a message resolver is registered containing key `"greeting"` with template `"Hello, {0}!"`
- **WHEN** `format("greeting", "Alice")` is called
- **THEN** the result is `"Hello, Alice!"`

#### Scenario: Key not found
- **GIVEN** no resolver contains the key `"missing"`
- **WHEN** `format("missing", "arg")` is called
- **THEN** the result is `null`

### Requirement: Template retrieval

The `I18nService` SHALL provide `getTemplate(String key)` to retrieve the raw message template without parameter substitution.

#### Scenario: Retrieve existing template
- **GIVEN** a resolver contains key `"welcome"` with template `"Welcome, {0}"`
- **WHEN** `getTemplate("welcome")` is called
- **THEN** the result is `"Welcome, {0}"`

### Requirement: Enum message registration and resolution

The `EnumI18nService` SHALL allow registering enum classes and resolving localized messages for individual enum constants.

#### Scenario: Register and resolve enum message
- **GIVEN** an `EnumI18nService` instance is active
- **WHEN** `register(MyEnum.class)` is called, then `getMessageForEnum(MyEnum.VALUE_A)` is called
- **THEN** the localized message for `VALUE_A` is returned

#### Scenario: Register enum by class name with custom classloader
- **GIVEN** an `EnumI18nService` instance is active
- **WHEN** `register("com.example.MyEnum", customClassLoader)` is called
- **THEN** the enum class is loaded from the custom classloader and registered

### Requirement: LocaleProvider optional service

The `LocaleProvider` interface SHALL provide an `Optional<Locale>` representing the current locale. Implementations are optional OSGi services — when absent, the i18n service falls back to configured defaults or JVM locale.

#### Scenario: Locale provider present
- **GIVEN** a `LocaleProvider` service is registered returning `Optional.of(Locale.GERMAN)`
- **WHEN** the i18n service resolves a message
- **THEN** German locale is used for property file lookup

#### Scenario: Locale provider absent
- **GIVEN** no `LocaleProvider` service is registered
- **WHEN** the i18n service resolves a message
- **THEN** the configured default locale or `Locale.getDefault()` is used

### Requirement: @Message annotation

The `@Message` annotation SHALL allow overriding message resolution on interface proxy methods, providing either an inline template (`value`) or an alternative property key (`key`).

#### Scenario: Inline template via value
- **GIVEN** a method annotated with `@Message("Direct template: {0}")`
- **WHEN** the method is invoked with argument `"test"`
- **THEN** the result is `"Direct template: test"` (property file lookup is skipped)

#### Scenario: Property key override via key
- **GIVEN** a method annotated with `@Message(key = "custom.key")`
- **WHEN** the method is invoked
- **THEN** the property `"custom.key"` is looked up instead of the method name

### Requirement: @MessageByKey annotation

The `@MessageByKey` annotation SHALL mark a method as a dynamic key resolver. The method must accept `(String key, Object[] params)` and resolve the message for the given key at runtime.

#### Scenario: Dynamic key resolution
- **GIVEN** a method annotated with `@MessageByKey`
- **WHEN** `method("error.notFound", new Object[]{"item"})` is called
- **THEN** the property `"error.notFound"` is resolved and `{0}` is replaced with `"item"`

### Requirement: BCP47 locale parsing

`I18NUtil.getLocaleFromBCP47()` SHALL parse BCP47 language tags (e.g., `"en-US"`, `"hu"`) into `java.util.Locale` instances. Strings containing underscores (Java `Locale.toString()` format) SHALL return `null`.

#### Scenario: Valid BCP47 tag
- **GIVEN** the string `"en-US"`
- **WHEN** `I18NUtil.getLocaleFromBCP47("en-US")` is called
- **THEN** a `Locale` with language `"en"` and country `"US"` is returned

#### Scenario: Null or underscore format
- **GIVEN** the string `"en_US"` (Java toString format)
- **WHEN** `I18NUtil.getLocaleFromBCP47("en_US")` is called
- **THEN** `null` is returned
