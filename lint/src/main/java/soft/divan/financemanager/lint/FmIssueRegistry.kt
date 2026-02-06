package soft.divan.financemanager.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

/*
 * The list of issues that will be checked when running <code>lint</code>.
 */

private const val MIN_SUPPORTED_API = 8

class FmIssueRegistry : IssueRegistry() {
    override val issues = listOf(
        AvoidDateDetector.ISSUE
    )

    override val api: Int
        get() = CURRENT_API

    override val minApi: Int
        get() = MIN_SUPPORTED_API // works with Studio 4.1 or later; see com.android.tools.lint.detector.api.Api / ApiKt

    // Requires lint API 30.0+; if you're still building for something
    // older, just remove this property.
    override val vendor: Vendor =
        Vendor(
            vendorName = "soft-divan",
            feedbackUrl = "https://github.com/googlesamples/android-custom-lint-rules/issues",
            contact = "https://github.com/googlesamples/android-custom-lint-rules"
        )
}
// Revue me>>
