package examples.parameterNameClash.controllers

import examples.parameterNameClash.models.SomeObject
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Body
import io.micronaut.http.`annotation`.Consumes
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.PathVariable
import io.micronaut.http.`annotation`.Post
import io.micronaut.http.`annotation`.QueryValue
import io.micronaut.validation.Validated
import javax.validation.Valid
import kotlin.String
import kotlin.Unit

@Controller
@Validated
public class ExampleController(
    public val getByIdDelegate: GetByIdDelegate,
    public val postDelegate: PostDelegate,
) {
    /**
     *
     *
     * @param pathB
     * @param queryB
     */
    @Get(uri = "/example/{b}")
    public fun getById(
        @PathVariable(value = "pathB") pathB: String,
        @QueryValue(value = "queryB")
        queryB: String,
    ): HttpResponse<Unit> = getByIdDelegate.getById(pathB, queryB)

    /**
     *
     *
     * @param bodySomeObject example
     * @param querySomeObject
     */
    @Post(uri = "/example")
    @Consumes(value = ["application/json"])
    public fun post(
        @Body @Valid bodySomeObject: SomeObject,
        @QueryValue(value = "querySomeObject")
        querySomeObject: String,
    ): HttpResponse<Unit> = postDelegate.post(
        bodySomeObject,
        querySomeObject,
    )

    public interface GetByIdDelegate {
        public fun getById(pathB: String, queryB: String): HttpResponse<Unit>
    }

    public interface PostDelegate {
        public fun post(bodySomeObject: SomeObject, querySomeObject: String): HttpResponse<Unit>
    }
}
