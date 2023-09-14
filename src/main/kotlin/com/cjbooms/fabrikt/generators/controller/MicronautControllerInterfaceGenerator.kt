package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.toIncomingParameters
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKdoc
import com.cjbooms.fabrikt.generators.ValidationAnnotations
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.SecuritySupport
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.happyPathResponse
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.methodName
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.securitySupport
import com.cjbooms.fabrikt.generators.controller.metadata.MicronautImports
import com.cjbooms.fabrikt.generators.controller.metadata.MicronautImports.SECURITY_RULE_IS_ANONYMOUS
import com.cjbooms.fabrikt.generators.controller.metadata.MicronautImports.SECURITY_RULE_IS_AUTHENTICATED
import com.cjbooms.fabrikt.model.BodyParameter
import com.cjbooms.fabrikt.model.ControllerType
import com.cjbooms.fabrikt.model.HeaderParam
import com.cjbooms.fabrikt.model.KotlinTypes
import com.cjbooms.fabrikt.model.PathParam
import com.cjbooms.fabrikt.model.QueryParam
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.KaizenParserExtensions.basePath
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSingleResource
import com.cjbooms.fabrikt.util.KaizenParserExtensions.routeToPaths
import com.cjbooms.fabrikt.util.toUpperCase
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class MicronautControllerInterfaceGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val validationAnnotations: ValidationAnnotations,
    private val options: Set<ControllerCodeGenOptionType> = emptySet(),
) : ControllerInterfaceGenerator(packages, api, validationAnnotations) {

    private val useSuspendModifier: Boolean
        get() = options.any { it == ControllerCodeGenOptionType.SUSPEND_MODIFIER }

    private val addAuthenticationParameter: Boolean
        get() = options.any { it == ControllerCodeGenOptionType.AUTHENTICATION }

    override fun generate(): MicronautControllers =
        MicronautControllers(
            api.openApi3.routeToPaths().map { (resourceName, paths) ->
                buildControllerWithDelegate(resourceName, paths.values)
            }.toSet(),
            addAuthenticationParameter,
        )

    override fun buildFunction(path: Path, op: Operation, verb: String): FunSpec {
        TODO("Not yet implemented")
    }

    fun buildControllerWithDelegate(resourceName: String, paths: Collection<Path>): ControllerType {
        val controllerBuilder: TypeSpec.Builder = controllerBuilder(
            className = ControllerGeneratorUtils.controllerName(resourceName),
            basePath = api.openApi3.basePath()
        )

        val constructorBuilder = FunSpec.constructorBuilder()
        paths.forEach { path ->
            path.operations
                .filter { it.key.toUpperCase() != "HEAD" }
                .forEach { op ->
                    val name = methodName(op.value, op.key, path.pathString.isSingleResource())
                    val delegateName = name.replaceFirstChar { it.uppercase() } + "Delegate"
                    val delegateClassName = ClassName("", delegateName)
                    val delegateParameterName = name + "Delegate"
                    val delegateBuilder = TypeSpec.interfaceBuilder(delegateName)
                    controllerBuilder.addFunction(
                        buildFunctionCallingDelegate(
                            path,
                            op.value,
                            op.key,
                            delegateParameterName
                        )
                    )
                    delegateBuilder.addFunction(buildFunctionForDelegate(path, op.value, op.key))
                    constructorBuilder.addParameter(delegateParameterName, delegateClassName)
                    controllerBuilder.addProperty(
                        PropertySpec.builder(delegateParameterName, delegateClassName)
                            .initializer(delegateParameterName)
                            .build()
                    )
                    controllerBuilder.addType(delegateBuilder.build())
                }
        }

        controllerBuilder.primaryConstructor(constructorBuilder.build())

        return ControllerType(
            controllerBuilder.build(),
            packages.base
        )
    }

    override fun controllerBuilder(
        className: String,
        basePath: String,
    ) =
        TypeSpec.classBuilder(className)
            .addAnnotation(
                AnnotationSpec
                    .builder(MicronautImports.CONTROLLER)
                    .build(),
            )
            .addAnnotation(
                AnnotationSpec
                    .builder(MicronautImports.VALIDATED)
                    .build(),
            )


    fun buildFunctionForDelegate(
        path: Path,
        op: Operation,
        verb: String,
    ): FunSpec {
        val methodName = methodName(op, verb, path.pathString.isSingleResource())
        val returnType = MicronautImports.RESPONSE.parameterizedBy(op.happyPathResponse(packages.base))
        val parameters = op.toIncomingParameters(packages.base, path.parameters, emptyList())
        val globalSecurity = this.api.openApi3.securityRequirements.securitySupport()

        // Main method builder
        val funcSpec = FunSpec
            .builder(methodName)
            .apply {
                if (useSuspendModifier) {
                    addModifiers(KModifier.SUSPEND)
                }
            }
            .addModifiers(KModifier.ABSTRACT)
            .returns(returnType)

        // Function parameters
        parameters
            .map { it.toParameterSpecBuilder().build() }
            .forEach { funcSpec.addParameter(it) }

        // Add authentication
        if (addAuthenticationParameter) {
            val securityOption = op.securitySupport(globalSecurity)

            if (securityOption.allowsAuthenticated) {
                val typeName =
                    MicronautImports.AUTHENTICATION
                        .copy(nullable = securityOption == SecuritySupport.AUTHENTICATION_OPTIONAL)
                funcSpec.addParameter(
                    ParameterSpec
                        .builder("authentication", typeName)
                        .build(),
                )
            }
        }

        return funcSpec.build()
    }


    fun buildFunctionCallingDelegate(
        path: Path,
        op: Operation,
        verb: String,
        delegateName: String
    ): FunSpec {
        val methodName = methodName(op, verb, path.pathString.isSingleResource())
        val returnType = MicronautImports.RESPONSE.parameterizedBy(op.happyPathResponse(packages.base))
        val parameters = op.toIncomingParameters(packages.base, path.parameters, emptyList())
        val globalSecurity = this.api.openApi3.securityRequirements.securitySupport()

        val mappedParameters = parameters
            .map {
                when (it) {
                    is BodyParameter ->
                        it
                            .toParameterSpecBuilder()
                            .addAnnotation(
                                AnnotationSpec
                                    .builder(MicronautImports.BODY).build(),
                            )
                            .addAnnotation(validationAnnotations.parameterValid())
                            .build()

                    is RequestParameter ->
                        it
                            .toParameterSpecBuilder()
                            .addValidationAnnotations(it)
                            .addMicronautParamAnnotation(it)
                            .build()
                }
            }.toMutableList()

        // Add authentication
        if (addAuthenticationParameter) {
            val securityOption = op.securitySupport(globalSecurity)

            if (securityOption.allowsAuthenticated) {
                val typeName =
                    MicronautImports.AUTHENTICATION
                        .copy(nullable = securityOption == SecuritySupport.AUTHENTICATION_OPTIONAL)
                mappedParameters.add(
                    ParameterSpec
                        .builder("authentication", typeName)
                        .build()
                )
            }
        }

        // Main method builder

        val builder = CodeBlock.builder()
        builder.add("return %L.%L(", delegateName, methodName)
        mappedParameters
            .forEachIndexed { index, param ->
                builder.add("%L", param.name)
                if (index < mappedParameters.size - 1) {
                    builder.add(", ")
                }
            }
        builder.add(")\n")

        val funcSpec = FunSpec
            .builder(methodName)
            .addKdoc(op.toKdoc(parameters))
            .addMicronautFunAnnotation(op, verb, path.pathString)
            .apply {
                if (useSuspendModifier) {
                    addModifiers(KModifier.SUSPEND)
                }
            }.addCode(builder.build())
            .returns(returnType)

        // Function parameters
        mappedParameters.forEach(funcSpec::addParameter)

        return funcSpec.build()
    }

    private fun FunSpec.Builder.addMicronautFunAnnotation(op: Operation, verb: String, path: String): FunSpec.Builder {
        val globalSecurity =
            api.openApi3.securityRequirements.securitySupport()

        val produces = op.responses
            .flatMap { it.value.contentMediaTypes.keys }
            .toTypedArray()

        val consumes = op.requestBody
            .contentMediaTypes.keys
            .toTypedArray()

        this.addAnnotation(
            AnnotationSpec
                .builder(MicronautImports.HttpMethods.byName(verb))
                .addMember("uri = %S", path).build(),
        )

        if (consumes.isNotEmpty()) {
            this.addAnnotation(
                AnnotationSpec
                    .builder(MicronautImports.CONSUMES)
                    .addMember(
                        "value = %L",
                        consumes.joinToString(
                            prefix = "[",
                            postfix = "]",
                            separator = ", ",
                            transform = { "\"$it\"" },
                        ),
                    )
                    .build(),
            )
        }

        if (produces.isNotEmpty()) {
            this.addAnnotation(
                AnnotationSpec
                    .builder(MicronautImports.PRODUCES)
                    .addMember(
                        "value = %L",
                        produces.joinToString(
                            prefix = "[",
                            postfix = "]",
                            separator = ", ",
                            transform = { "\"$it\"" },
                        ),
                    )
                    .build(),
            )
        }

        if (addAuthenticationParameter) {
            val securityRule = when (op.securitySupport(globalSecurity)) {
                SecuritySupport.AUTHENTICATION_REQUIRED -> SECURITY_RULE_IS_AUTHENTICATED
                SecuritySupport.AUTHENTICATION_PROHIBITED -> SECURITY_RULE_IS_ANONYMOUS
                SecuritySupport.AUTHENTICATION_OPTIONAL -> "$SECURITY_RULE_IS_AUTHENTICATED, $SECURITY_RULE_IS_ANONYMOUS"
                else -> ""
            }

            if (securityRule != "") {
                val systemRoleTypeName = ClassName("de.flipnext.core.usermanagement.users.domain","SystemRole")
                val requirements = op.securityRequirements.getOrNull(0)?.requirements?.values?.first()?.parameters
                val spec = AnnotationSpec
                    .builder(MicronautImports.SECURED)
                // **warning** this is a hack to get the system role names into the annotation
                // since this is already custom flip code we use the system role name directly
                requirements?.forEach { spec.addMember("%T.${it.toUpperCase()}.name", systemRoleTypeName) }
                if (requirements.isNullOrEmpty()) {
                    spec.addMember(securityRule)
                }

                this.addAnnotation(
                    spec.build()
                )
            }
        }

        return this
    }

    private fun ParameterSpec.Builder.addMicronautParamAnnotation(parameter: RequestParameter): ParameterSpec.Builder =
        when (parameter.parameterLocation) {
            QueryParam ->
                AnnotationSpec
                    .builder(MicronautImports.QUERY_VALUE)

            HeaderParam ->
                AnnotationSpec
                    .builder(MicronautImports.HEADER)

            PathParam ->
                AnnotationSpec
                    .builder(MicronautImports.PATH_VARIABLE)
        }.let {
            it.addMember("value = %S", parameter.oasName)

            if (parameter.defaultValue != null) {
                it.addMember("defaultValue = %S", parameter.defaultValue)
            }
            this.addAnnotation(it.build())
        }
}

data class MicronautControllers(val controllers: Collection<ControllerType>, val addAuthenticationParameter: Boolean) : KotlinTypes(controllers) {

    override val files: Collection<FileSpec> =
        if (addAuthenticationParameter) {
            super.files.map {
                it.toBuilder()
                    .addImport(MicronautImports.SECURITY_RULE.first, MicronautImports.SECURITY_RULE.second).build()
            }
        } else {
            super.files
        }
}
