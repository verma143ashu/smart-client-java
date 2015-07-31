/*
 * Copyright (c) 2015, EMC Corporation.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     + Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     + Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     + The name of EMC Corporation may not be used to endorse or promote
 *       products derived from this software without specific prior written
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.emc.rest.smart;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 * Polling thread that will terminate automatically when the application exits
 */
public class PollingDaemon extends Thread {
    public static final String PROPERTY_KEY = "com.emc.rest.smart.pollingDaemon";

    private static final Logger l4j = Logger.getLogger(PollingDaemon.class);

    private SmartConfig smartConfig;
    private boolean running = true;

    public PollingDaemon(SmartConfig smartConfig) {
        this.smartConfig = smartConfig;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            long start = System.currentTimeMillis();
            l4j.debug("polling daemon running");

            LoadBalancer loadBalancer = smartConfig.getLoadBalancer();
            HostListProvider hostListProvider = smartConfig.getHostListProvider();

            if (!smartConfig.isHostUpdateEnabled()) {
                l4j.info("host update is disabled; not updating hosts");
            } else if (hostListProvider == null) {
                l4j.info("no host list provider; not updating hosts");
            } else {
                try {
                    loadBalancer.updateHosts(hostListProvider.getHostList());
                } catch (Throwable t) {
                    l4j.warn("unable to enumerate servers", t);
                }
            }

            if (!smartConfig.isHealthCheckEnabled()) {
                l4j.info("health check is disabled; not checking hosts");
            } else if (hostListProvider == null) {
                l4j.info("no host list provider; not checking hosts");
            } else {
                for (Host host : loadBalancer.getAllHosts()) {
                    try {
                        hostListProvider.runHealthCheck(host);
                        host.setHealthy(true);
                        LogMF.debug(l4j, "health check successful for {0}; host is marked healthy", host.getName());
                    } catch (Throwable t) {
                        host.setHealthy(false);
                        l4j.warn("health check failed for " + host.getName() + "; host is marked unhealthy", t);
                    }
                }
            }

            long callTime = System.currentTimeMillis() - start;
            try {
                long sleepTime = smartConfig.getPollInterval() * 1000 - callTime;
                if (sleepTime < 0) sleepTime = 0;
                LogMF.debug(l4j, "polling daemon finished; will poll again in {0}ms..", sleepTime);
                if (sleepTime > 0) Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                l4j.warn("interrupted while sleeping", e);
            }
        }
    }

    @SuppressWarnings("unused")
    public void terminate() {
        running = false;
    }
}