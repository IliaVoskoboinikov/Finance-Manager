---
name: new-module
description: Scaffold a new Gradle module in Finance-Manager following the project's module-rules — a feature (:api + :impl with FeatureApi wiring) or a core:* library. Use when the user asks to create/add a new module, feature, or core library.
---

# Scaffold a new module

Create a new module that conforms to `docs/agents/module-rules.md`. Never invent an
ad-hoc structure — mirror an existing sibling (`feature/account` for features,
any `core/*` for libraries).

## 1. Clarify inputs (ask only if missing)

- **Type:** `feature` (split into `:api` + `:impl`) or `core` (single library).
- **Name:** kebab-case for the directory (e.g. `budget`, `my-goals`). The Kotlin
  package strips dashes: `my-goals` → `soft.divan.financemanager.feature.mygoals`.
- For a feature: does it have a navigable screen (needs `FeatureApi` route wiring)?

## 2. Feature module layout

Dirs (replace `<name>` = kebab dir, `<pkg>` = dash-stripped package segment):

```
feature/<name>/api/build.gradle.kts
feature/<name>/api/README.md
feature/<name>/api/src/main/AndroidManifest.xml            (empty <manifest></manifest>)
feature/<name>/api/src/main/java/soft/divan/financemanager/feature/<pkg>/api/<Name>FeatureApi.kt
feature/<name>/api/src/main/java/soft/divan/financemanager/feature/<pkg>/api/<Name>Key.kt
feature/<name>/impl/build.gradle.kts
feature/<name>/impl/README.md
feature/<name>/impl/src/main/AndroidManifest.xml
feature/<name>/impl/src/main/java/soft/divan/financemanager/feature/<pkg>/impl/navigation/<Name>FeatureImpl.kt
feature/<name>/impl/src/main/java/soft/divan/financemanager/feature/<pkg>/impl/di/<Name>BinderModule.kt
```

**api/build.gradle.kts**
```kotlin
plugins {
    alias(libs.plugins.soft.divan.feature.api)
}

dependencies {
    api(projects.core.featureApi)
}
```

**`<Name>FeatureApi.kt`**
```kotlin
package soft.divan.financemanager.feature.<pkg>.api

import soft.divan.financemanager.core.featureapi.FeatureApi

interface <Name>FeatureApi : FeatureApi
```

**`<Name>Key.kt`** — публичный контракт навигации фичи (Navigation 3). Аргументы экрана —
поля ключа, а не строки маршрута:
```kotlin
package soft.divan.financemanager.feature.<pkg>.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** KDoc: что за экран. */
@Serializable
data object <Name>Key : NavKey     // или data class <Name>Key(val id: String) : NavKey
```
`@Serializable` обязателен: по нему back stack переживает смерть процесса.

**impl/build.gradle.kts** (add only the `core:*` / other-feature `:api` deps actually used)
```kotlin
plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.<accessor>.api)
    implementation(projects.core.domain)
    implementation(projects.core.uikit)
}
```
Note: type-safe accessors camel-case dashes — `my-goals` → `projects.feature.myGoals.api`.

**`<Name>FeatureImpl.kt`** — implements `<Name>FeatureApi`, `@Inject constructor()`, and
`registerEntries(scope, navigator, modifier) { scope.entry<<Name>Key> { ... } }`
(see `feature/account/impl/.../navigation/AccountFeatureImpl.kt`).
Регистрируй только свои ключи: переход к соседней фиче — `navigator.goTo(OtherKey)`,
возврат — `navigator::back`. Один и тот же ключ нельзя зарегистрировать дважды.

**`<Name>BinderModule.kt`**
```kotlin
package soft.divan.financemanager.feature.<pkg>.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.<pkg>.api.<Name>FeatureApi
import soft.divan.financemanager.feature.<pkg>.impl.navigation.<Name>FeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface <Name>BinderModule {
    @Binds
    fun bind<Name>Router(impl: <Name>FeatureImpl): <Name>FeatureApi
}
```

If the feature owns a screen wired into the app graph, add a binding to
`app/.../di/FeatureNavigationModule.kt`:
```kotlin
    @Binds
    @IntoSet
    fun bind<Name>Feature(impl: <Name>FeatureApi): FeatureApi
```
Без этой привязки экраны фичи не попадут в `entryProvider`, и переход на её ключ упадёт
в рантайме. Вкладка нижней навигации дополнительно добавляется в
`app/.../presenter/navigation/ScreenBottom.kt`.

## 3. Core module layout

```
core/<name>/build.gradle.kts        # plugins { alias(libs.plugins.soft.divan.android.library) }
core/<name>/README.md
core/<name>/src/main/AndroidManifest.xml
core/<name>/src/main/java/soft/divan/financemanager/core/<name>/...
```
Remember the dependency rule: `core:*` must NEVER depend on `feature:*` or `app`.

## 4. Register the module

Add the include(s) to `settings.gradle.kts`, grouped with siblings:
```kotlin
include(":feature:<name>:api")
include(":feature:<name>:impl")
```

## 5. README.md (mandatory per module)

Every module needs one. Minimum:
```markdown
# `:feature:<name>:api`

## Responsibility

<one line: what this module is for>

## Module dependency graph

<!--region graph-->
<!--endregion-->
```
The graph block is regenerated by tooling — leave the markers.

## 6. Verify

```bash
./gradlew :app:assertModuleGraph
./gradlew :feature:<name>:impl:check   # or :core:<name>:check
./gradlew ktlintCheck detekt
```
Fix any graph or style violations before reporting done. Report which module(s)
were created and their place in the dependency graph.
