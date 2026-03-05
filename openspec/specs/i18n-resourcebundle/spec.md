# i18n-resourcebundle Specification

## Purpose

Provides the properties-file-based implementation of the `I18nService` and `EnumI18nService` APIs. Registers as OSGi Declarative Services components, creates JDK dynamic proxies for message interfaces, resolves messages from `.properties` files with locale fallback, caches locale-specific resolvers using Guava `LoadingCache`, and applies `{0}`-style parameter substitution.

## Architecture

Key classes and their relationships:

- **`I18nServiceImpl`** — OSGi `@Component` implementing `I18nService`. Maintains maps of registered interface proxies (`registrationMap`) and their message resolvers (`messageResolverMap`). Creates `ClassAndRequestContextLocaleBasedMessageResolver` and `InterfaceMessageTemplaterInvocationHandler` per registered interface.
- **`EnumI18nServiceImpl`** — OSGi `@Component` implementing `EnumI18nService`. Maintains a map of enum class → `MessageResolver`. Uses same resolver chain as `I18nServiceImpl`.
- **`InterfaceMessageTemplaterInvocationHandler`** — `InvocationHandler` for dynamic proxies. Resolves the property key from method name, `@MessageByKey`, or `@Message` annotations, then delegates to `MessageResolver` and `SimpleParameterTemplater`.
- **`ClassAndRequestContextLocaleBasedMessageResolver`** — Guava `LoadingCache`-backed resolver keyed by `Locale`. On cache miss, creates a `ClassBasedPropertiesFileMessageResolver` for that locale.
- **`ClassBasedPropertiesFileMessageResolver`** — Loads properties for a specific interface class and locale. Delegates to an optional primary resolver first, then falls back to its own properties.
- **`LocaleBasedMessageResolver`** — Same pattern as above but name-based (arbitrary base name instead of class-derived).
- **`LocalePropertiesFileLoader`** — Loads `.properties` files with locale suffix fallback: variant → country → language → default. Walks the interface's superinterface hierarchy.
- **`ClassLoaderBasedMessageStreamLoader`** — `MessageStreamLoader` implementation that tries multiple classloaders: provided → thread context → this class → system.
- **`SimpleParameterTemplater`** — Static utility replacing `{0}`, `{1}`, ... placeholders via regex.

## Requirements

### Requirement: OSGi component activation with default locale configuration

`I18nServiceImpl` and `EnumI18nServiceImpl` SHALL activate as immediate OSGi components and accept a `defaultLocale` configuration property (BCP47 format) via Config Admin.

#### Scenario: Activate with default locale
- **GIVEN** the OSGi component is configured with `defaultLocale = "hu"`
- **WHEN** the component activates
- **THEN** `I18NUtil.getLocaleFromBCP47("hu")` is called and the resulting `Locale` is used as the fallback

#### Scenario: Activate without default locale
- **GIVEN** the OSGi component is configured with `defaultLocale = ""`
- **WHEN** the component activates
- **THEN** `Locale.getDefault()` is used as the final fallback

### Requirement: Dynamic proxy creation and OSGi service registration

`I18nServiceImpl.register()` SHALL create a JDK dynamic proxy for the given interface, register it as an OSGi service in the `BundleContext`, and store both the `ServiceRegistration` and `MessageResolver` for later use.

#### Scenario: Register an interface
- **GIVEN** `I18nServiceImpl` is activated with a valid `BundleContext`
- **WHEN** `register(FooMessages.class)` is called
- **THEN** a proxy implementing `FooMessages` is created using `Proxy.newProxyInstance()`, registered via `context.registerService()`, and stored in `registrationMap`

### Requirement: Locale-aware message resolution with caching

`ClassAndRequestContextLocaleBasedMessageResolver` SHALL use a Guava `LoadingCache` (max 500 entries) keyed by `Locale`. On cache miss, it SHALL create a `ClassBasedPropertiesFileMessageResolver` for the requested locale.

#### Scenario: First request for a locale
- **GIVEN** the cache is empty
- **WHEN** `get("someKey")` is called with the current locale being `Locale("hu")`
- **THEN** a new `ClassBasedPropertiesFileMessageResolver` is created for Hungarian, cached, and the key is resolved from it

#### Scenario: Subsequent request for same locale
- **GIVEN** a resolver for `Locale("hu")` is already cached
- **WHEN** `get("anotherKey")` is called with the same locale
- **THEN** the cached resolver is reused without creating a new one

### Requirement: Properties file locale fallback chain

`LocalePropertiesFileLoader` SHALL search for the most specific properties file first and fall back to less specific ones.

#### Scenario: Full locale with variant
- **GIVEN** locale is `en_US_custom`
- **WHEN** properties are loaded for interface `com.example.Messages`
- **THEN** the loader tries in order: `com/example/Messages_en_US_custom.properties`, `com/example/Messages_en_US.properties`, `com/example/Messages_en.properties`, `com/example/Messages.properties`, and uses the first one found

#### Scenario: Language-only locale
- **GIVEN** locale is `hu` (no country, no variant)
- **WHEN** properties are loaded
- **THEN** the loader tries: `Messages_hu.properties`, then `Messages.properties`

### Requirement: Superinterface properties inheritance

`LocalePropertiesFileLoader.loadProperties()` SHALL recursively load properties from all superinterfaces before loading from the interface itself, so child interfaces inherit parent messages.

#### Scenario: Interface extends parent
- **GIVEN** `ChildMessages extends ParentMessages`
- **WHEN** properties are loaded for `ChildMessages`
- **THEN** `ParentMessages.properties` is loaded first, then `ChildMessages.properties` (child overrides parent)

### Requirement: Method-to-key resolution in InvocationHandler

`InterfaceMessageTemplaterInvocationHandler` SHALL resolve the property key from the method according to this priority:

1. `@MessageByKey` — use the first argument as the key
2. Method name as property key → look up in `MessageResolver`
3. `@Message(key=...)` — use the annotation's key if property lookup returned empty
4. `@Message(value=...)` — use inline template if key is also empty
5. Auto-generated template — `"methodName {0} {1} ..."` as last resort

#### Scenario: Method name as key
- **GIVEN** method `welcomeUser()` with no annotations
- **WHEN** the proxy method is invoked
- **THEN** the resolver looks up key `"welcomeUser"`

#### Scenario: @MessageByKey annotation
- **GIVEN** method annotated with `@MessageByKey` called as `resolve("error.code", new Object[]{"404"})`
- **WHEN** the proxy method is invoked
- **THEN** the resolver looks up key `"error.code"` with params `["404"]`

#### Scenario: Auto-generated template fallback
- **GIVEN** method `unknownMethod("a", "b")` with no matching property
- **WHEN** the proxy method is invoked
- **THEN** the result is `"unknownMethod a b"` (auto-generated from method name and arguments)

### Requirement: Simple parameter templating

`SimpleParameterTemplater.templateMessage()` SHALL replace `{0}`, `{1}`, ... placeholders in a template string with the corresponding argument values using `toString()`.

#### Scenario: Replace multiple parameters
- **GIVEN** template `"Hello {0}, you have {1} items"`
- **WHEN** `templateMessage(template, "Alice", 5)` is called
- **THEN** the result is `"Hello Alice, you have 5 items"`

#### Scenario: Null parameter
- **GIVEN** template `"Value: {0}"`
- **WHEN** `templateMessage(template, (Object) null)` is called
- **THEN** the result is `"Value: null"`

### Requirement: ClassLoader fallback chain for resource loading

`ClassLoaderBasedMessageStreamLoader` SHALL try multiple classloaders to find a properties file resource:

1. The classloader provided at construction time
2. Thread context classloader
3. `ClassLoaderBasedMessageStreamLoader`'s own classloader
4. System classloader

#### Scenario: Resource found in provided classloader
- **GIVEN** the provided classloader can load `"com/example/Messages.properties"`
- **WHEN** `load("com/example/Messages.properties")` is called
- **THEN** the `InputStream` from the provided classloader is returned

#### Scenario: Resource not in provided classloader but in thread context
- **GIVEN** the provided classloader returns `null` but the thread context classloader can load the resource
- **WHEN** `load(...)` is called
- **THEN** the `InputStream` from the thread context classloader is returned

### Requirement: Enum message resolution

`EnumI18nServiceImpl.getMessageForEnum()` SHALL resolve a message for an enum constant by looking up the constant's `name()` in the registered resolver. If no message is found, it SHALL return the constant name with appended `{0} {1} ...` placeholders formatted with the given arguments.

#### Scenario: Enum message found
- **GIVEN** `MyEnum.class` is registered and `MyEnum.properties` contains `VALUE_A=Value A: {0}`
- **WHEN** `getMessageForEnum(MyEnum.VALUE_A, "detail")` is called
- **THEN** the result is `"Value A: detail"`

#### Scenario: Enum message not found
- **GIVEN** `MyEnum.class` is registered but no property exists for `UNKNOWN`
- **WHEN** `getMessageForEnum(MyEnum.UNKNOWN, "x", "y")` is called
- **THEN** the result is `"UNKNOWN x y"`

### Requirement: Return type validation

`InterfaceMessageTemplaterInvocationHandler` SHALL only process methods that return `String`. Methods with other return types SHALL return an error message string describing the invalid return type.

#### Scenario: Non-String return type
- **GIVEN** a proxy interface has a method `int count()`
- **WHEN** the proxy method is invoked
- **THEN** the result is a string starting with `"Invalid return type of the method"`
