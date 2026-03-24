# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Eclipse Gemini Blueprint is the reference implementation of the OSGi Alliance Blueprint Service (OSGi 4.2 Compendium Specification). It integrates Spring Framework with OSGi, enabling Spring-based applications to run inside OSGi containers. Current version: 4.0.0.BUILD-SNAPSHOT, targeting Java 17+ (compiled with source/target 17), Spring 7.0.6, OSGi Core 8.0.0/Compendium 7.0.0.

## Build Commands

```bash
# Build and run unit tests (all modules except integration-tests)
./mvnw install

# Integration tests with a specific OSGi framework (run from project root)
# Must pick one profile: equinox or felix
./mvnw clean install -P equinox
./mvnw clean install -P felix

# Run integration tests only (after initial install)
cd integration-tests && ./mvnw clean install -P equinox

# Run a single unit test
./mvnw test -pl core -Dtest=ClassName

# Run a single integration test
cd integration-tests/tests && ./mvnw test -P equinox -Dtest=ClassName
```

Integration tests fork a fresh JVM per run with a 45-minute timeout. Test bundles and tests must be built together per profile — separating these stages causes OSGi dependency issues.

## Module Architecture

```
mock/           → Mock implementations of OSGi interfaces (BundleContext, ServiceReference, etc.)
io/             → Resource loading abstractions for OSGi (OsgiBundleResource, ResourcePatternResolver)
core/           → Main module: application context, service import/export, Blueprint container,
                  XML config namespace, ConfigurationAdmin integration
extender/       → OSGi extender pattern: listens for bundle events and bootstraps Blueprint
                  containers for bundles containing Spring/Blueprint configuration
extensions/     → Proprietary extensions beyond the OSGi Blueprint specification
test-support/   → JUnit-based framework for running integration tests inside live OSGi containers
integration-tests/
  bundles/      → 50+ test bundles exercising various Blueprint scenarios
  tests/        → Integration test runner module
```

**Dependency flow:** mock → io → core → extender → extensions; test-support depends on core + extender.

## Key Architectural Concepts

- **Service Import/Export** (`core/.../service/`): Proxies that dynamically track OSGi services. Importers create proxies for consumed services; exporters register Spring beans as OSGi services.
- **Blueprint Container** (`core/.../blueprint/container/`): Implementation of the OSGi Blueprint Container spec — manages component lifecycle, dependency injection, and service binding.
- **Extender Pattern** (`extender/.../internal/activator/`): The extender bundle watches for other bundles being installed/started and automatically creates Blueprint containers for them.
- **OSGi-Spring Context Bridge** (`core/.../context/`): Extends Spring's `ApplicationContext` to work within OSGi, handling classloader delegation and bundle-scoped contexts.

## OSGi Bundle Configuration

Each module uses a `bnd.bnd` file (processed by the `bnd-maven-plugin`) to generate OSGi manifest headers (Export-Package, Import-Package, Bundle-Activator, etc.). These are in each module's root directory.

## Maven Profiles

| Profile | Purpose |
|---------|---------|
| `equinox` | Run integration tests on Eclipse Equinox |
| `felix` | Run integration tests on Apache Felix |
| `security` | Enable Java 2 Security Manager for tests |
| `clover` | Enable Clover code coverage |
| `release` | Full release build with GPG signing and docs |
