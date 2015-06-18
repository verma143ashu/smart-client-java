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
package com.emc.rest.smart.ecs;

import com.emc.rest.smart.Host;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Vdc implements Iterable<Host> {
    private String name;
    private List<Host> hosts;

    public Vdc(String... hostNames) {
        this.name = hostNames[0];
        hosts = new ArrayList<Host>();
        for (String hostName : hostNames) {
            hosts.add(new Host(hostName));
        }
    }

    public Vdc(List<Host> hosts) {
        this(hosts.get(0).getName(), hosts);
    }

    public Vdc(String name, List<Host> hosts) {
        this.name = name;
        this.hosts = hosts;
    }

    @Override
    public Iterator<Host> iterator() {
        return hosts.iterator();
    }

    public boolean isHealthy() {
        for (Host host : hosts) {
            if (!host.isHealthy()) return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public Vdc withName(String name) {
        setName(name);
        return this;
    }
}
