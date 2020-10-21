/*
 * Copyright 2020 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.epirus.console.openapi.subcommands

import io.epirus.console.EpirusVersionProvider
import io.epirus.console.openapi.project.OpenApiProjectCreationUtils.buildProject
import io.epirus.console.openapi.project.OpenApiProjectCreationUtils.createProjectStructure
import io.epirus.console.openapi.project.OpenApiTemplateProvider
import io.epirus.console.openapi.utils.PrettyPrinter
import io.epirus.console.project.TemplateType
import io.epirus.console.project.utils.ProgressCounter
import io.epirus.console.token.erc777.ERC777Utils
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.io.File

@Command(
    name = "new",
    description = ["Create a new Web3j-OpenAPI project."],
    abbreviateSynopsis = true,
    showDefaultValues = true,
    mixinStandardHelpOptions = true,
    versionProvider = EpirusVersionProvider::class,
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    optionListHeading = "%nOptions:%n",
    footerHeading = "%n",
    footer = ["Epirus CLI is licensed under the Apache License 2.0"]
)
class NewOpenApiCommand : AbstractOpenApiCommand() {

    @Parameters(description = ["HelloWorld, ERC777"], defaultValue = "HelloWorld")
    var templateType = TemplateType.HelloWorld

    override fun generate(projectFolder: File) {
        val progressCounter = ProgressCounter(true)
        progressCounter.processing("Creating and Building ${projectOptions.projectName} project ... Subsequent builds will be faster")

        when (templateType) {
            TemplateType.HelloWorld -> {
                val projectStructure = createProjectStructure(
                    openApiTemplateProvider = OpenApiTemplateProvider(
                        solidityContract = "project/HelloWorld.sol",
                        pathToSolidityFolder = "",
                        gradleBuild = "project/build.gradleOpenApi.template",
                        packageName = projectOptions.packageName,
                        projectName = projectOptions.projectName,
                        contextPath = contextPath,
                        addressLength = (projectOptions.addressLength * 8).toString()
                    ), outputDir = projectOptions.outputDir
                )
                buildProject(projectStructure.projectRoot)
            }

            TemplateType.ERC777 -> {
                val projectStructure = createProjectStructure(
                    openApiTemplateProvider = OpenApiTemplateProvider(
                        solidityContract = "",
                        pathToSolidityFolder = "",
                        gradleBuild = "project/build.gradleOpenApiErc777.template",
                        packageName = projectOptions.packageName,
                        projectName = projectOptions.projectName,
                        contextPath = contextPath,
                        addressLength = (projectOptions.addressLength * 8).toString()
                    ), outputDir = projectOptions.outputDir
                )
                ERC777Utils.copy(projectStructure.solidityPath)
                buildProject(projectStructure.projectRoot)
            }
        }

        progressCounter.setLoading(false)
        PrettyPrinter.onOpenApiProjectSuccess()
    }
}
