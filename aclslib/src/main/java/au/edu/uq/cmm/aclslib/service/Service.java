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

/**
 * Light-weight API for component "services" supporting the notion
 * of startup and shutdown.
 * 
 * @author scrawley
 */
public interface Service {
    
    /**
     * This represents the possible states of a service.
     * 
     * @author scrawley
     */
    public enum State {
        /** The service has never been started */
        INITIAL, 
        /** The service is starting */
        STARTING,
        /** The service is currently running (as far as we can tell) */
        STARTED, 
        /** The service is stopping */
        STOPPING,
        /** The service has been stopped. */
        STOPPED, 
        /** The service has failed. */
        FAILED
    }
    
    

    /** 
     * Start the service.  If the service is already running, this is a no-op.
     * A service in shutdown or failed state can typically be (re-)started.
     * This method blocks until startup completes.
     */
    void startup() throws ServiceException, InterruptedException;
    
    /** 
     * Shutdown the service.  If the service is already shutdown or has never
     * been started, this is a no-op.  If the service is failed, this moves it
     * to the shutdown state. This method blocks until startup completes.
     * 
     * @throws ServiceException
     * @throws InterruptedException 
     */
    void shutdown() throws ServiceException, InterruptedException;
    
    /**
     * Initiate the startup of the service.
     * 
     * @throws ServiceException
     */
    void startStartup() throws ServiceException;
    
    /**
     * Initiate the shutdown of the service.
     * 
     * @throws ServiceException
     */
    void startShutdown() throws ServiceException;
    
    /**
     * Wait for the service to enter the shutdown state; e.g. in response to
     * a {@link #shutdown()} call made by a different thread.
     * 
     * @throws InterruptedException
     */
    void awaitShutdown() throws InterruptedException;
    
    /**
     * Get the service's current state.
     * 
     * @return a "best effort" version of the services state.  In some cases,
     * the FAILED state may not be detectable.
     */
    State getState();
}
