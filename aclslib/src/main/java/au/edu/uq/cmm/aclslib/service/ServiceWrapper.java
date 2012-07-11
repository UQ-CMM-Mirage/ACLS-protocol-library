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
 * This provides a simple way to implement a service that cannot extend
 * one of our base classes.  Just implement it as a SimpleService and
 * use this wrapper class to provide the state management and asynchronous
 * startup / shutdown support.
 * 
 * @author scrawley
 */
public class ServiceWrapper extends ServiceBase {
    private final SimpleService service;
    
    public ServiceWrapper(SimpleService service) {
        this.service = service;
    }

    @Override
    protected void doShutdown() throws InterruptedException {
        service.shutdown();
    }

    @Override
    protected void doStartup() throws ServiceException, InterruptedException {
        service.startup();
    }
}
