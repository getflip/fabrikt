package examples.authentication.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import kotlin.String
import kotlin.Unit

@Controller
@Validated
class RequiredController(
    val testPathDelegate: TestPathDelegate
) {
    /**
     *
     *
     * @param testString
     */
    @Get(uri = "/required")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    fun testPath(
        @QueryValue(value = "testString") testString: String,
        authentication: Authentication
    ): HttpResponse<Unit> = testPathDelegate.testPath(
        testString,
        authentication
    )

    interface TestPathDelegate {
        fun testPath(testString: String, authentication: Authentication): HttpResponse<Unit>
    }
}

@Controller
@Validated
class ProhibitedController(
    val testPathDelegate: TestPathDelegate
) {
    /**
     *
     *
     * @param testString
     */
    @Get(uri = "/prohibited")
    @Secured(SecurityRule.IS_ANONYMOUS)
    fun testPath(@QueryValue(value = "testString") testString: String): HttpResponse<Unit> =
        testPathDelegate.testPath(testString)

    interface TestPathDelegate {
        fun testPath(testString: String): HttpResponse<Unit>
    }
}

@Controller
@Validated
class OptionalController(
    val testPathDelegate: TestPathDelegate
) {
    /**
     *
     *
     * @param testString
     */
    @Get(uri = "/optional")
    @Secured(SecurityRule.IS_AUTHENTICATED, SecurityRule.IS_ANONYMOUS)
    fun testPath(
        @QueryValue(value = "testString") testString: String,
        authentication: Authentication?
    ): HttpResponse<Unit> = testPathDelegate.testPath(
        testString,
        authentication
    )

    interface TestPathDelegate {
        fun testPath(testString: String, authentication: Authentication?): HttpResponse<Unit>
    }
}

@Controller
@Validated
class NoneController(
    val testPathDelegate: TestPathDelegate
) {
    /**
     *
     *
     * @param testString
     */
    @Get(uri = "/none")
    fun testPath(@QueryValue(value = "testString") testString: String): HttpResponse<Unit> =
        testPathDelegate.testPath(testString)

    interface TestPathDelegate {
        fun testPath(testString: String): HttpResponse<Unit>
    }
}

@Controller
@Validated
class DefaultController(
    val testPathDelegate: TestPathDelegate
) {
    /**
     *
     *
     * @param testString
     */
    @Get(uri = "/default")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    fun testPath(
        @QueryValue(value = "testString") testString: String,
        authentication: Authentication
    ): HttpResponse<Unit> = testPathDelegate.testPath(
        testString,
        authentication
    )

    interface TestPathDelegate {
        fun testPath(testString: String, authentication: Authentication): HttpResponse<Unit>
    }
}
