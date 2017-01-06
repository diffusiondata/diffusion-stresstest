/*******************************************************************************
 * Copyright (C) 2016, 2017 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
* limitations under the License.
 *******************************************************************************/
package com.pushtechnology.diffusion.stresstest;

import com.pushtechnology.diffusion.DiffusionException;
import com.pushtechnology.diffusion.api.config.ConfigException;
import com.pushtechnology.diffusion.api.config.ConfigManager;
import com.pushtechnology.diffusion.api.config.ThreadPoolConfig;

/**
 * Set up the inbound pool.
 *
 * @author Push Technology Limited
 */
public final class InboundPoolConfigManager {

    private static final int DEFAULT_INBOUND_POOL_QUEUE_SIZE = 20;

    private static final int DEFAULT_INBOUND_POOL_CORE_SIZE = 3;

    private static final int DEFAULT_INBOUND_POOL_MAX_SIZE = 10;

    private static final String INBOUND_POOL_NAME = "UserDefinedPool";

    private static int theInboundPoolQueueSize;

    private static int theInboundThreadPoolCoreSize;

    private static int theInboundThreadPoolMaxSize;

    private InboundPoolConfigManager() {
        throw new AssertionError();
    }

    /**
     * Configure the inbound pool.
     *
     * @throws DiffusionException on error
     */
    public static void configInboundPool() throws DiffusionException {

        acquireConfigProperties();

        configureDefaultInboundPool();

        if (isConfigured(theInboundPoolQueueSize)) {
            setQueueSize(theInboundPoolQueueSize);
        }

        if (isConfigured(theInboundThreadPoolCoreSize)) {
            setCoreSize(theInboundThreadPoolCoreSize);
        }

        if (isConfigured(theInboundThreadPoolMaxSize)) {
            setMaxSize(theInboundThreadPoolMaxSize);
        }

        setInboundThreadPool();

    }

    private static void setInboundThreadPool() throws ConfigException {
        ConfigManager.getConfig().getThreads()
            .setInboundPool(INBOUND_POOL_NAME);

    }

    private static void acquireConfigProperties() throws DiffusionException {

        theInboundPoolQueueSize =
            StressTestProperties.getInboundThreadPoolQueueSize();

        theInboundThreadPoolCoreSize =
            StressTestProperties.getInboundThreadPoolCoreSize();

        theInboundThreadPoolMaxSize =
            StressTestProperties.getInboundThreadPoolMaxSize();
    }

    private static void configureDefaultInboundPool() throws ConfigException {

        final ThreadPoolConfig defaultPool =
            ConfigManager.getConfig().getThreads().addPool(INBOUND_POOL_NAME);

        defaultPool.setQueueSize(DEFAULT_INBOUND_POOL_QUEUE_SIZE);

        defaultPool.setCoreSize(DEFAULT_INBOUND_POOL_CORE_SIZE);

        defaultPool.setMaximumSize(DEFAULT_INBOUND_POOL_MAX_SIZE);
    }

    private static boolean isConfigured(int stressTestParam) {
        return stressTestParam != 0;
    }

    private static void setQueueSize(int queueSize) throws ConfigException {
        ConfigManager.getConfig().getThreads().getPool(INBOUND_POOL_NAME)
            .setQueueSize(queueSize);
    }

    private static void setCoreSize(int coreSize) throws ConfigException {
        ConfigManager.getConfig().getThreads().getPool(INBOUND_POOL_NAME)
            .setCoreSize(coreSize);
    }

    private static void setMaxSize(int maxSize) throws ConfigException {
        ConfigManager.getConfig().getThreads().getPool(INBOUND_POOL_NAME)
            .setMaximumSize(maxSize);
    }
}
