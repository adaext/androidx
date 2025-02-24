/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.privacysandbox.tools.apigenerator

import androidx.privacysandbox.tools.core.AnnotatedInterface
import androidx.privacysandbox.tools.core.Method
import androidx.privacysandbox.tools.core.Parameter
import androidx.privacysandbox.tools.core.poet.build
import androidx.privacysandbox.tools.core.poet.poetSpec
import androidx.privacysandbox.tools.core.poet.primaryConstructor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal class ClientProxyTypeGenerator(private val service: AnnotatedInterface) {
    private val className =
        ClassName(service.packageName, "${service.name}ClientProxy")
    private val remoteBinderClassName =
        ClassName(service.packageName, "I${service.name}")

    fun generate(): TypeSpec = TypeSpec.classBuilder(className).build {
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(ClassName(service.packageName, service.name))
        primaryConstructor(
            listOf(
                PropertySpec.builder("remote", remoteBinderClassName)
                    .addModifiers(KModifier.PRIVATE).build()
            ), KModifier.PRIVATE
        )
        addFunctions(service.methods.map(::generateProxyMethodImplementation))
    }

    private fun generateProxyMethodImplementation(method: Method) =
        FunSpec.builder(method.name).build {
            addParameters(method.parameters.map { it.poetSpec() })
            addModifiers(KModifier.OVERRIDE)

            val parameterList = buildList {
                addAll(method.parameters.map(Parameter::name))
                add("null")
            }
            val returnsUnit = method.returnType.name == Unit::class.qualifiedName
            if (returnsUnit) {
                addStatement(
                    "remote.${method.name}(${parameterList.joinToString()})"
                )
            } else {
                addStatement(
                    "return remote.${method.name}(${parameterList.joinToString()})!!"
                )
            }
        }
}