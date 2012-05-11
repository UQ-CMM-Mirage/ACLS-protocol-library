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
 * A RestartDecider instance decides whether a monitored service thread 
 * should be restarted after dying.  An instance may keep track of
 * how many times the service has crashed and the time since the last crash,
 * and use this to decide whether to restart.  It may also go to sleep for
 * a bit to throttle the restart rate.
 * 
 * @author scrawley
 */
public interface RestartDecider {

    /**
     * Decide whether to restart a crashed service based on the exception
     * that caused the thread to die, and/or past history.
     * @param ex the exception that caused the thread to die, or null.
     * @return {@literal true) to restart, otherwise {@literal false}.
     */
    boolean isRestartable(Throwable ex);

}
