package examples.singleAllOf.controllers

import examples.singleAllOf.models.Result
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.validation.Validated

@Controller
@Validated
class TestController(
    val testDelegate: TestDelegate
) {
    /**
     *
     */
    @Get(uri = "/test")
    @Produces(value = ["application/json"])
    fun test(): HttpResponse<Result> = testDelegate.test()

    interface TestDelegate {
        fun test(): HttpResponse<Result>
    }
}
