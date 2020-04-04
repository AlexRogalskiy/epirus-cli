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
package io.epirus.console.config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import com.google.gson.Gson;

public class CliConfig {
    private String clientId;
    private String latestVersion;
    private String updatePrompt;
    private String loginToken;
    private boolean telemetryDisabled;

    private boolean isPersistent = true;

    public CliConfig(
            String clientId,
            String latestVersion,
            String updatePrompt,
            String loginToken,
            boolean telemetryDisabled) {
        this.clientId = clientId;
        this.latestVersion = latestVersion;
        this.updatePrompt = updatePrompt;
        this.loginToken = loginToken;
        this.telemetryDisabled = telemetryDisabled;
    }

    public String getClientId() {
        return clientId;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getUpdatePrompt() {
        return updatePrompt;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public boolean isTelemetryDisabled() {
        return telemetryDisabled;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
        save();
    }

    public void setUpdatePrompt(String updatePrompt) {
        this.updatePrompt = updatePrompt;
        save();
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
        save();
    }

    public void save() {
        if (!isPersistent) return;

        String jsonToWrite = new Gson().toJson(this);
        try {
            Files.write(
                    ConfigManager.DEFAULT_EPIRUS_CONFIG_PATH,
                    jsonToWrite.getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new ConfigException(e);
        }
    }

    public void setPersistent(boolean persistent) {
        isPersistent = persistent;
    }
}
