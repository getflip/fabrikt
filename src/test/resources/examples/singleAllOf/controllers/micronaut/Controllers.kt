package examples.singleAllOf.controllers

import examples.singleAllOf.models.Result
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.Produces
import io.micronaut.validation.Validated

@Controller
@Validated
public class TestController(
    public val testDelegate: TestDelegate,
) {
    /**
     *
     */
    @Get(uri = "/test")
    @Produces(value = ["application/json"])
    public fun test(): HttpResponse<Result> = testDelegate.test()

    public interface TestDelegate {
        public fun test(): HttpResponse<Result>
    }
}
