package examples.pathLevelParameters.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.QueryValue
import io.micronaut.validation.Validated
import kotlin.String
import kotlin.Unit

@Controller
@Validated
public class ExampleController(
    public val getDelegate: GetDelegate,
) {
    /**
     *
     *
     * @param a
     * @param b
     */
    @Get(uri = "/example")
    public fun `get`(@QueryValue(value = "a") a: String, @QueryValue(value = "b") b: String): HttpResponse<Unit> = getDelegate.get(a, b)

    public interface GetDelegate {
        public fun `get`(a: String, b: String): HttpResponse<Unit>
    }
}
