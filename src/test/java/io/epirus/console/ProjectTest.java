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
package io.epirus.console;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Paths;

import io.epirus.console.account.AccountManager;
import io.epirus.console.deploy.DeployRunner;
import io.epirus.console.project.utils.ClassExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Network;
import org.web3j.protocol.Web3j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectTest extends ClassExecutor {
    @TempDir protected static File workingDirectory;
    protected String absoluteWalletPath;

    @Test
    public void testAccountDeployment() throws Exception {
        AccountManager accountManager = mock(AccountManager.class);
        Web3j web3j = mock(Web3j.class);
        when(accountManager.pollForAccountBalance(
                        any(Credentials.class),
                        any(Network.class),
                        any(Web3j.class),
                        any(int.class)))
                .thenReturn(BigInteger.TEN);
        when(accountManager.checkIfAccountIsConfirmed(20)).thenReturn(true);
        DeployRunner deployRunner =
                spy(
                        new DeployRunner(
                                Network.RINKEBY,
                                accountManager,
                                web3j,
                                Paths.get(workingDirectory + File.separator + "Test"),
                                absoluteWalletPath));
        doNothing().when(deployRunner).run();
        deployRunner.run();
        verify(deployRunner, times(1)).run();
    }
}
