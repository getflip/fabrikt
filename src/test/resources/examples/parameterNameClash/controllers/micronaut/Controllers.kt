package examples.parameterNameClash.controllers

import examples.parameterNameClash.models.SomeObject
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
import javax.validation.Valid
import kotlin.String
import kotlin.Unit

@Controller
@Validated
class ExampleController(
    val getByIdDelegate: GetByIdDelegate,
    val postDelegate: PostDelegate
) {
    /**
     *
     *
     * @param pathB
     * @param queryB
     */
    @Get(uri = "/example/{b}")
    fun getById(
        @PathVariable(value = "pathB") pathB: String,
        @QueryValue(value = "queryB")
        queryB: String
    ): HttpResponse<Unit> = getByIdDelegate.getById(pathB, queryB)

    /**
     *
     *
     * @param bodySomeObject example
     * @param querySomeObject
     */
    @Post(uri = "/example")
    @Consumes(value = ["application/json"])
    fun post(
        @Body @Valid
        bodySomeObject: SomeObject,
        @QueryValue(value = "querySomeObject")
        querySomeObject: String
    ): HttpResponse<Unit> = postDelegate.post(
        bodySomeObject,
        querySomeObject
    )

    interface GetByIdDelegate {
        fun getById(pathB: String, queryB: String): HttpResponse<Unit>
    }

    interface PostDelegate {
        fun post(bodySomeObject: SomeObject, querySomeObject: String): HttpResponse<Unit>
    }
}
