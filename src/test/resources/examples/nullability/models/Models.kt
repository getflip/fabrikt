package examples.nullability.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import org.openapitools.jackson.nullable.JsonNullable
import javax.validation.constraints.NotNull
import kotlin.String

public data class MergePatchNullabilityCheck(
    @param:JsonProperty("not-null-no-default")
    @get:JsonProperty("not-null-no-default")
    @get:NotNull
    public val notNullNoDefault: JsonNullable<String> = JsonNullable.undefined(),
    @param:JsonProperty("nullable-no-default")
    @get:JsonProperty("nullable-no-default")
    public val nullableNoDefault: JsonNullable<String?> = JsonNullable.undefined(),
    @param:JsonProperty("not-null-with-default")
    @get:JsonProperty("not-null-with-default")
    @get:NotNull
    public val notNullWithDefault: JsonNullable<String> = JsonNullable.of(""),
    @param:JsonProperty("nullable-with-default")
    @get:JsonProperty("nullable-with-default")
    public val nullableWithDefault: JsonNullable<String?> = JsonNullable.of(""),
)

public data class NullabilityCheck(
    @param:JsonProperty("not-null-not-required")
    @get:JsonProperty("not-null-not-required")
    public val notNullNotRequired: String? = null,
    @param:JsonProperty("nullable-not-required")
    @get:JsonProperty("nullable-not-required")
    public val nullableNotRequired: String? = null,
    @param:JsonProperty("not-null-required")
    @get:JsonProperty("not-null-required")
    @get:NotNull
    public val notNullRequired: String,
    @param:JsonProperty("nullable-required")
    @get:JsonProperty("nullable-required")
    public val nullableRequired: String?,
)
