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
package io.epirus.console.token.subcommand.erc777

import io.epirus.console.EpirusVersionProvider
import picocli.CommandLine

@CommandLine.Command(
        name = "erc777",
        description = ["Create a new ERC777 token contract"],
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider::class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = ["Epirus CLI is licensed under the Apache License 2.0"])
class ERC777TokenCommand : Runnable {
    @CommandLine.Option(names = ["--o, --output"], description = ["Output Directory."])
    var outputDir = "."

    @CommandLine.Option(
            names = ["-n", "--name"],
            description = ["specify the project name."],
            defaultValue = "ERC777Token"
    )
    var projectName: String = "ERC777Token"

    override fun run() {
        ERC777GeneratorService(projectName, outputDir).generate()
    }
}
