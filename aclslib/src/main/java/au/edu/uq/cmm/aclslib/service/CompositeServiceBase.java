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

package au.edu.uq.cmm.aclslib.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Service class that extends this class gets infrastructure for starting up and
 * shutting down a "composite" service, comprising other Service instances.  The
 * class needs to implement the {@link #doStartup()} and {@link #doShutdown()}
 * methods.  These typically just call {@link Service#startup()} and 
 * {@link Service#shutdown()} on the component services.
 * 
 * @author scrawley
 */
public abstract class CompositeServiceBase implements Service {
    private static final Logger LOG = 
            LoggerFactory.getLogger(CompositeServiceBase.class);
    private State state = State.INITIAL;
    private final Object lock = new Object();

    public void startup() {
        LOG.info("Startup called");
        synchronized (lock) {
            if (state == State.STARTING || state == State.STOPPING) {
                throw new IllegalStateException("State change already in progress");
            }
            if (state == State.STARTED) {
                LOG.info("Already started");
                return;
            }
            state = State.STARTING;
        }
        LOG.info("Starting up");
        try {
            doStartup();
            LOG.info("Startup completed");
            synchronized (lock) {
                state = State.STARTED;
                lock.notifyAll();
            }
        } catch (InterruptedException ex) {
            LOG.error("Startup interrupted");
            synchronized (lock) {
                state = State.FAILED;
                lock.notifyAll();
            }
        }
    }

    public void startStartup() throws ServiceException {
        LOG.info("StartStartup called");
        synchronized (lock) {
            if (state == State.STARTING || state == State.STOPPING) {
                throw new IllegalStateException("State change already in progress");
            }
            if (state == State.STARTED) {
                LOG.info("Already started");
                return;
            }
            state = State.STARTING;
        }
        new Thread(new Runnable(){
            public void run() {
                LOG.info("Starting up");
                try {
                    doStartup();
                    LOG.info("Startup completed");
                    synchronized (lock) {
                        state = State.STARTED;
                        lock.notifyAll();
                    }
                } catch (InterruptedException ex) {
                    LOG.error("Startup interrupted");
                    synchronized (lock) {
                        state = State.FAILED;
                        lock.notifyAll();
                    }
                }
            }
        }).start();
    }

    public void shutdown() throws InterruptedException {
        LOG.info("Shutdown called");
        synchronized (lock) {
            if (state == State.STARTING || state == State.STOPPING) {
                throw new IllegalStateException("State change already in progress");
            }
            if (state != State.STARTED) {
                LOG.info("Already stopped");
                return;
            }
            state = State.STOPPING;
        }
        LOG.info("Shutting down");
        doShutdown();
        LOG.info("Shutdown completed");
        synchronized (lock) {
            state = State.STOPPED;
            lock.notifyAll();
        }
    }
    
    public void startShutdown() throws ServiceException {
        LOG.info("StartShutdown called");
        synchronized (lock) {
            if (state == State.STARTING || state == State.STOPPING) {
                throw new IllegalStateException("State change already in progress");
            }
            if (state != State.STARTED) {
                LOG.info("Already stopped");
                return;
            }
            state = State.STOPPING;
        }
        new Thread(new Runnable(){
            public void run() {
                LOG.info("Shutting down");
                try {
                    doShutdown();
                    LOG.info("Shutdown completed");
                } catch (InterruptedException ex) {
                    LOG.error("Shutdown interrupted", ex);
                } finally {
                    synchronized (lock) {
                        state = State.STOPPED;
                        lock.notifyAll();
                    }
                }
            }}).start();
    }

    public void awaitShutdown() throws InterruptedException {
        synchronized (lock) {
            while (state != State.STOPPED) {
                lock.wait();
            }
        }
    }

    public final State getState() {
        synchronized (lock) {
            return state;
        }
    }

    protected abstract void doShutdown() throws InterruptedException;
    
    protected abstract void doStartup() throws ServiceException, InterruptedException;
}
