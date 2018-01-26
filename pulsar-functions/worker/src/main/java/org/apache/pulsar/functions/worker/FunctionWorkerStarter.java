/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.functions.worker;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.functions.fs.LimitsConfig;

/**
 * A starter to start function worker.
 */
@Slf4j
public class FunctionWorkerStarter {

    private static class WorkerArguments {
        @Parameter(
            names = { "-c", "--conf" },
            description = "Configuration File for Function Worker")
        private String configFile;

        @Parameter(names = {"-h", "--help"}, description = "Show this help message")
        private boolean help = false;
    }

    public static void main(String[] args) throws Exception {
        WorkerArguments workerArguments = new WorkerArguments();
        JCommander commander = new JCommander(workerArguments);
        commander.setProgramName("FunctionWorkerStarter");

        // parse args by commander
        commander.parse(args);

        if (workerArguments.help) {
            commander.usage();
            System.exit(-1);
            return;
        }

        WorkerConfig workerConfig;
        if (isBlank(workerArguments.configFile)) {
            workerConfig = new WorkerConfig();
        } else {
            workerConfig = WorkerConfig.load(workerArguments.configFile);
        }
        if (null != workerConfig.getProcessContainerFactory()
            && null == workerConfig.getProcessContainerFactory().getJavaInstanceJarLocation()) {

            String envJavaInstanceJarLocation = System.getProperty("pulsar.functions.java.instance.jar");
            if (null != envJavaInstanceJarLocation) {
                log.info("Java instance jar location is not defined in worker config yml."
                    + " Use the location defined in system environment : {}", envJavaInstanceJarLocation);
                workerConfig.getProcessContainerFactory().setJavaInstanceJarLocation(envJavaInstanceJarLocation);
            }
        }

        if (null == workerConfig.getLimitsConfig()) {
            workerConfig.setLimitsConfig(new LimitsConfig(-1, -1, -1, 1024));
        }

        final Worker worker = new Worker(workerConfig);
        worker.startAsync();
    }
}
