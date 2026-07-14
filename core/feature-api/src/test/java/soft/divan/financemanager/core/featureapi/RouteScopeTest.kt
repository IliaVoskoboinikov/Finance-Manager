package soft.divan.financemanager.core.featureapi

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RouteScopeTest {

    @Test
    fun `route with empty scope returns path as is`() {
        assertThat(RouteScope("").route("settings")).isEqualTo("settings")
    }

    @Test
    fun `route with empty path returns scope value`() {
        assertThat(RouteScope("root").route()).isEqualTo("root")
        assertThat(RouteScope("root").route("")).isEqualTo("root")
    }

    @Test
    fun `route joins scope and path with slash`() {
        assertThat(RouteScope("root").route("settings")).isEqualTo("root/settings")
    }

    @Test
    fun `route of empty scope and empty path is empty`() {
        assertThat(RouteScope("").route()).isEmpty()
    }

    @Test
    fun `child creates nested scope`() {
        val child = RouteScope("root").child("feature")

        assertThat(child.route("screen")).isEqualTo("root/feature/screen")
    }

    @Test
    fun `child of empty scope starts from segment`() {
        val child = RouteScope("").child("feature")

        assertThat(child.route()).isEqualTo("feature")
    }

    @Test
    fun `nested children build hierarchical routes`() {
        val scope = RouteScope("app").child("settings").child("security")

        assertThat(scope.route("pin")).isEqualTo("app/settings/security/pin")
    }
}
