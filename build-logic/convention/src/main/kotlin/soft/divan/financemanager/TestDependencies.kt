package soft.divan.financemanager

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Adds the project's default unit-test stack (JUnit, MockK, AssertJ, coroutines-test — see the
 * `unit-test` bundle in the version catalog) as `testImplementation` for the module.
 *
 * Applied automatically by the `soft.divan.jvm.library`, `soft.divan.core` and
 * `soft.divan.feature.impl` convention plugins so individual modules do not re-declare the same
 * test dependencies. Module-specific extras (e.g. Robolectric, `androidTestImplementation`) still
 * live in the module's own `build.gradle.kts`.
 */
fun Project.addDefaultUnitTestDependencies() {
    dependencies {
        add(Conf.TEST_IMPLEMENTATION, bundle("unit-test"))
    }
}
