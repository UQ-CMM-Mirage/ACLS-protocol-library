/*
* Copyright 2012, CMM, University of Queensland.
*
* This file is part of AclsLib.
*
* AclsLib is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* AclsLib is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.uq.cmm.acslib.service;

import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import au.edu.uq.cmm.aclslib.service.DefaultRestartDecider;
import au.edu.uq.cmm.aclslib.service.MonitoredThreadServiceBase;
import au.edu.uq.cmm.aclslib.service.Service;
import au.edu.uq.cmm.aclslib.service.Service.State;

public class MonitoredThreadServiceBaseTest {
    
    private class MTSBTestService extends MonitoredThreadServiceBase {
        private final Deque<String> status;
        private final AtomicBoolean killSwitch;
        
        public MTSBTestService(Deque<String> status, AtomicBoolean killSwitch,
                int threshold, int initialThreshold, int throttleTime) {
            super(new DefaultRestartDecider(threshold, initialThreshold, throttleTime));
            this.status = status;
            this.killSwitch = killSwitch;
        }
        
        public void run() {
            status.add("running");
            while (true) {
                try {
                    if (killSwitch.get()) {
                        killSwitch.set(false);
                        status.add("arrggghhh");
                        throw new NullPointerException();
                    }
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    break;
                }
            }
            status.add("finished");
        }
    }
    
    @Test
    public void testStartupShutdown() throws InterruptedException {
        BlockingDeque<String> status = new LinkedBlockingDeque<String>();
        AtomicBoolean killSwitch = new AtomicBoolean();
        Service service = new MTSBTestService(status, killSwitch, 1, 1, 500);
        Assert.assertNull(status.pollFirst());
        Assert.assertEquals(State.INITIAL, service.getState());
        service.startup();
        Assert.assertEquals(State.STARTED, service.getState());
        Assert.assertEquals("running", status.pollFirst(2, TimeUnit.SECONDS));
        service.shutdown();
        Assert.assertEquals(State.STOPPED, service.getState());
        Assert.assertEquals("finished", status.pollFirst(2, TimeUnit.SECONDS));
    }
    
    @Test
    public void testStartupDieRestartShutdown() throws InterruptedException {
        BlockingDeque<String> status = new LinkedBlockingDeque<String>();
        AtomicBoolean killSwitch = new AtomicBoolean();
        Service service = new MTSBTestService(status, killSwitch, 1, 1, 500);
        Assert.assertNull(status.pollFirst());
        Assert.assertEquals(State.INITIAL, service.getState());
        service.startup();
        Assert.assertEquals("running", status.pollFirst(2, TimeUnit.SECONDS));
        Assert.assertEquals(State.STARTED, service.getState());
        killSwitch.set(true);
        Assert.assertEquals("arrggghhh", status.pollFirst(2, TimeUnit.SECONDS));
        Assert.assertEquals("running", status.pollFirst(2, TimeUnit.SECONDS));
        Assert.assertEquals(State.STARTED, service.getState());
        service.shutdown();
        Assert.assertEquals(State.STOPPED, service.getState());
        Assert.assertEquals("finished", status.pollFirst(2, TimeUnit.SECONDS));
    }
    
    @Test
    public void testStartupDieRestartShutdown2() throws InterruptedException {
        BlockingDeque<String> status = new LinkedBlockingDeque<String>();
        AtomicBoolean killSwitch = new AtomicBoolean();
        Service service = new MTSBTestService(status, killSwitch, 1000, 1000, 500);
        Assert.assertNull(status.pollFirst());
        Assert.assertEquals(State.INITIAL, service.getState());
        service.startup();
        Assert.assertEquals("running", status.pollFirst(2, TimeUnit.SECONDS));
        Assert.assertEquals(State.STARTED, service.getState());
        killSwitch.set(true);
        Assert.assertEquals("arrggghhh", status.pollFirst(2, TimeUnit.SECONDS));
        
        // (Deal with the fact that it takes time to get to the FAILED state.)
        for (int i = 0; i < 10; i++) {
            if (service.getState() == State.STARTED) {
                Thread.sleep(1);
            }
        }
        Assert.assertEquals(State.FAILED, service.getState());
        Assert.assertEquals(null, status.pollFirst());
        
        service.shutdown();
        Assert.assertEquals(State.STOPPED, service.getState());
        Assert.assertEquals(null, status.pollFirst());
    }
}
