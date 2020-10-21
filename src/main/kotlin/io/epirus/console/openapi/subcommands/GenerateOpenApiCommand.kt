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
import io.epirus.console.openapi.project.OpenApiProjectCreationUtils
import io.epirus.console.openapi.project.OpenApiTemplateProvider
import io.epirus.console.openapi.utils.PrettyPrinter
import io.epirus.console.project.utils.ProgressCounter
import io.epirus.console.project.utils.ProjectUtils.deleteFolder
import io.epirus.console.project.utils.ProjectUtils.exitIfNoContractFound
import org.apache.commons.io.FileUtils
import picocli.CommandLine.Option
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Visibility.ALWAYS
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Command(
        name = "generate",
        description = ["Generate REST endpoints from existing Solidity contracts."],
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider::class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = ["Epirus CLI is licensed under the Apache License 2.0"])
class GenerateOpenApiCommand : AbstractOpenApiCommand() {

    @Option(
        names = ["-s", "--solidity-path"],
        description = ["Path to Solidity file/folder"]
    )
    var solidityImportPath: String? = null

    @Option(
        names = ["--with-implementations"],
        description = ["Generate the interfaces implementations."],
        showDefaultValue = ALWAYS
    )
    var withImplementations: Boolean = true

    override fun generate(projectFolder: File) {
        if (solidityImportPath == null) {
            solidityImportPath = interactiveOptions.solidityProjectPath
        }
        exitIfNoContractFound(File(solidityImportPath!!))

        val progressCounter = ProgressCounter(true)
        progressCounter.processing("Generating REST endpoints ...")

        val projectFolderName = "GenerateEndpoints"
        val tempFolder = Files.createTempDirectory(Paths.get(projectOptions.outputDir), projectFolderName)

        val projectStructure = OpenApiProjectCreationUtils.createProjectStructure(
            OpenApiTemplateProvider(
                "",
                solidityImportPath!!,
                "project/build.gradleGenerateOpenApi.template",
                "project/settings.gradle.template",
                "project/gradlew-wrapper.properties.template",
                "project/gradlew.bat.template",
                "project/gradlew.template",
                "gradle-wrapper.jar",
                projectOptions.packageName,
                projectOptions.projectName,
                contextPath,
                (projectOptions.addressLength * 8).toString(),
                "project/README.openapi.md",
                withImplementations.toString()),
            tempFolder.toAbsolutePath().toString())

        OpenApiProjectCreationUtils.buildProject(
            projectStructure.projectRoot,
            withOpenApi = true,
            withSwaggerUi = false,
            withShadowJar = false)

        FileUtils.copyDirectory(Paths.get(tempFolder.toString(), projectOptions.projectName, "build", "generated", "sources", "web3j", "main").toFile(),
            Paths.get(projectOptions.outputDir, projectOptions.projectName).toFile())

        deleteFolder(tempFolder)
        progressCounter.setLoading(false)
        PrettyPrinter.onSuccess()
    }
}
