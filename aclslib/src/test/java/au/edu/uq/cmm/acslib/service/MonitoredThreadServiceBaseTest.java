package au.edu.uq.cmm.acslib.service;

import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import au.edu.uq.cmm.aclslib.service.MonitoredThreadServiceBase;
import au.edu.uq.cmm.aclslib.service.Service;

public class MonitoredThreadServiceBaseTest {
    
    private static class TestService extends MonitoredThreadServiceBase {
        private final Deque<String> status;
        private final AtomicBoolean killSwitch;
        
        public TestService(Deque<String> status, AtomicBoolean killSwitch) {
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
        Service service = new TestService(status, killSwitch);
        Assert.assertNull(status.pollFirst());
        service.startup();
        Assert.assertEquals("running", status.pollFirst(2, TimeUnit.SECONDS));
        service.shutdown();
        Assert.assertEquals("finished", status.pollFirst(2, TimeUnit.SECONDS));
    }
    
    @Test
    public void testStartupDieRestartShutdown() throws InterruptedException {
        BlockingDeque<String> status = new LinkedBlockingDeque<String>();
        AtomicBoolean killSwitch = new AtomicBoolean();
        Service service = new TestService(status, killSwitch);
        Assert.assertNull(status.pollFirst());
        service.startup();
        Assert.assertEquals("running", status.pollFirst(2, TimeUnit.SECONDS));
        killSwitch.set(true);
        Assert.assertEquals("arrggghhh", status.pollFirst(2, TimeUnit.SECONDS));
        Assert.assertEquals("running", status.pollFirst(2, TimeUnit.SECONDS));
        service.shutdown();
        Assert.assertEquals("finished", status.pollFirst(2, TimeUnit.SECONDS));
        
    }
}
