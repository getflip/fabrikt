package examples.jsonMergePatch.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import org.openapitools.jackson.nullable.JsonNullable
import javax.validation.Valid
import javax.validation.constraints.NotNull
import kotlin.String

public data class InnerMergePatch(
    @param:JsonProperty("p")
    @get:JsonProperty("p")
    @get:NotNull
    public val p: JsonNullable<String> = JsonNullable.undefined(),
)

public data class InnerNotMergePatch(
    @param:JsonProperty("p")
    @get:JsonProperty("p")
    public val p: String? = null,
)

public data class InnerOnlyMergePatchInline(
    @param:JsonProperty("inner")
    @get:JsonProperty("inner")
    @get:Valid
    public val `inner`: InnerOnlyMergePatchInlineInner? = null,
)

public data class InnerOnlyMergePatchInlineInner(
    @param:JsonProperty("p")
    @get:JsonProperty("p")
    @get:NotNull
    public val p: JsonNullable<String> = JsonNullable.undefined(),
)

public data class InnerOnlyMergePatchRef(
    @param:JsonProperty("inner")
    @get:JsonProperty("inner")
    @get:Valid
    public val `inner`: InnerMergePatch? = null,
)

public data class NestedMergePatchInline(
    @param:JsonProperty("inner")
    @get:JsonProperty("inner")
    @get:NotNull
    @get:Valid
    public val `inner`: JsonNullable<NestedMergePatchInlineInner> = JsonNullable.undefined(),
)

public data class NestedMergePatchInlineInner(
    @param:JsonProperty("p")
    @get:JsonProperty("p")
    @get:NotNull
    public val p: JsonNullable<String> = JsonNullable.undefined(),
)

public data class NestedMergePatchRef(
    @param:JsonProperty("inner")
    @get:JsonProperty("inner")
    @get:NotNull
    @get:Valid
    public val `inner`: JsonNullable<InnerMergePatch> = JsonNullable.undefined(),
)

public data class NoMergePatchInline(
    @param:JsonProperty("inner")
    @get:JsonProperty("inner")
    @get:Valid
    public val `inner`: NoMergePatchInlineInner? = null,
)

public data class NoMergePatchInlineInner(
    @param:JsonProperty("p")
    @get:JsonProperty("p")
    public val p: String? = null,
)

public data class NoMergePatchRef(
    @param:JsonProperty("inner")
    @get:JsonProperty("inner")
    @get:Valid
    public val `inner`: InnerNotMergePatch? = null,
)

public data class TopLevelLevelMergePatchInline(
    @param:JsonProperty("inner")
    @get:JsonProperty("inner")
    @get:NotNull
    @get:Valid
    public val `inner`: JsonNullable<TopLevelLevelMergePatchInlineInner> = JsonNullable.undefined(),
)

public data class TopLevelLevelMergePatchInlineInner(
    @param:JsonProperty("p")
    @get:JsonProperty("p")
    public val p: String? = null,
)

public data class TopLevelLevelMergePatchRef(
    @param:JsonProperty("inner")
    @get:JsonProperty("inner")
    @get:NotNull
    @get:Valid
    public val `inner`: JsonNullable<InnerNotMergePatch> = JsonNullable.undefined(),
)
