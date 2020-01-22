/*
 * Copyright 2019 Web3 Labs Ltd.
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
package org.web3j.console.project;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.web3j.codegen.Console;
import org.web3j.commons.JavaVersion;
import org.web3j.console.project.java.UnitTestCreator;
import org.web3j.console.project.templates.TemplateBuilder;
import org.web3j.console.project.templates.TemplateProvider;
import org.web3j.console.project.utils.ProgressCounter;
import org.web3j.console.project.utils.ProjectUtils;
import org.web3j.crypto.CipherException;

import static java.io.File.separator;

public class BaseProject {
    private final boolean withTests;
    private final boolean withFatJar;
    private final boolean withWallet;
    private final boolean withSampleCode;
    private final String command;
    private final String solidityImportPath;
    private final ProjectStructure projectStructure;
    private ProjectWallet projectWallet;
    private ProgressCounter progressCounter = new ProgressCounter(true);

    protected BaseProject(
            boolean withTests,
            boolean withFatJar,
            boolean withWallet,
            boolean withSampleCode,
            String command,
            String solidityImportPath,
            ProjectStructure projectStructure) {
        this.withTests = withTests;
        this.withFatJar = withFatJar;
        this.withWallet = withWallet;
        this.withSampleCode = withSampleCode;
        this.command = command;
        this.solidityImportPath = solidityImportPath;
        this.projectStructure = projectStructure;
    }

    public ProjectStructure getProjectStructure() {
        return this.projectStructure;
    }

    public ProjectWallet getProjectWallet() {
        return this.projectWallet;
    }

    private void buildGradleProject(final String pathToDirectory)
            throws IOException, InterruptedException {
        if (!isWindows()) {
            setExecutable(pathToDirectory, "gradlew");
            executeBuild(
                    new File(pathToDirectory), new String[] {"bash", "-c", "./gradlew build -q"});
        } else {
            setExecutable(pathToDirectory, "gradlew.bat");
            executeBuild(
                    new File(pathToDirectory),
                    new String[] {"cmd.exe", "/c", "gradlew.bat build -q"});
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    private void setExecutable(final String pathToDirectory, final String gradlew) {
        final File f = new File(pathToDirectory + File.separator + gradlew);
        final boolean isExecutable = f.setExecutable(true);
    }

    private void executeBuild(final File workingDir, final String[] command)
            throws InterruptedException, IOException {
        executeProcess(workingDir, command);
    }

    private void executeProcess(File workingDir, String[] command)
            throws InterruptedException, IOException {
        int exitCode =
                new ProcessBuilder(command)
                        .directory(workingDir)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .start()
                        .waitFor();
        if (exitCode != 0) {
            Console.exitError("Could not build project.");
        }
    }

    private void createFatJar(String pathToDirectory) throws IOException, InterruptedException {
        if (!isWindows()) {
            executeProcess(
                    new File(pathToDirectory),
                    new String[] {"bash", "./gradlew", "shadowJar", "-q"});
        } else {
            executeProcess(
                    new File(pathToDirectory),
                    new String[] {"cmd.exe", "/c", "./gradlew.bat shadowJar", "-q"});
        }
    }

    private void generateTopLevelDirectories(ProjectStructure projectStructure) {
        projectStructure.createMainDirectory();
        projectStructure.createTestDirectory();
        projectStructure.createSolidityDirectory();
        projectStructure.createWrapperDirectory();
    }

    private void generateTests(ProjectStructure projectStructure) throws IOException {
        String wrapperPath =
                String.join(
                        separator,
                        projectStructure.getRootDirectory(),
                        projectStructure.projectName,
                        "build",
                        "generated",
                        "source",
                        "web3j",
                        "main",
                        "java");
        String writePath =
                String.join(
                        separator,
                        projectStructure.getRootDirectory(),
                        projectStructure.projectName,
                        "src",
                        "test",
                        "java");
        new UnitTestCreator(wrapperPath, writePath).generate();
    }

    private void generateWallet()
            throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
                    NoSuchProviderException, IOException {
        projectStructure.createWalletDirectory();
        projectWallet =
                new ProjectWallet(
                        ProjectUtils.generateWalletPassword(), projectStructure.getWalletPath());
        ProjectWriter.writeResourceFile(
                projectWallet.getWalletPassword(),
                projectWallet.getPasswordFileName(),
                projectStructure.getWalletPath());
    }

    private TemplateProvider getTemplateProvider() {
        TemplateBuilder templateBuilder =
                new TemplateBuilder()
                        .withProjectNameReplacement(projectStructure.projectName)
                        .withPackageNameReplacement(projectStructure.packageName)
                        .withGradleBatScript("gradlew.bat.template")
                        .withGradleScript("gradlew.template");
        if (projectWallet != null) {

            templateBuilder.withWalletNameReplacement(projectWallet.getWalletName());
            templateBuilder.withPasswordFileName(projectWallet.getPasswordFileName());
        }
        if (command.equals("new")) {
            templateBuilder
                    .withGradleBuild(
                            JavaVersion.getJavaVersionAsDouble() < 11
                                    ? "build.gradle.template"
                                    : "build.gradleJava11.template")
                    .withSolidityProject("HelloWorld.sol");

        } else if (command.equals("import")) {
            templateBuilder
                    .withGradleBuild(
                            JavaVersion.getJavaVersionAsDouble() < 11
                                    ? "build.gradleImport.template"
                                    : "build.gradleImportJava11.template")
                    .withPathToSolidityFolder(solidityImportPath);
        }
        templateBuilder
                .withGradleSettings("settings.gradle.template")
                .withWrapperGradleSettings("gradlew-wrapper.properties.template")
                .withGradlewWrapperJar("gradle-wrapper.jar");
        if (withSampleCode) {
            templateBuilder.withMainJavaClass("Template.java");
        } else {
            templateBuilder.withMainJavaClass("EmptyTemplate.java");
        }

        return templateBuilder.build();
    }

    public void createProject()
            throws IOException, InterruptedException, NoSuchAlgorithmException,
                    NoSuchProviderException, InvalidAlgorithmParameterException, CipherException {
        generateTopLevelDirectories(projectStructure);
        if (withWallet) {
            generateWallet();
        }
        getTemplateProvider().generateFiles(projectStructure);
        progressCounter.processing("Creating " + projectStructure.projectName);
        buildGradleProject(projectStructure.getProjectRoot());

        if (withTests) {
            generateTests(projectStructure);
        }
        if (withFatJar) {
            createFatJar(projectStructure.getProjectRoot());
        }
        progressCounter.setLoading(false);
    }
}
