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
 * The DefaultRestartDecider implements a simple policy where a service must
 * survive a given number of milliseconds to be restartable.  The policy 
 * implements a throttle by delaying a certain number of milliseconds before
 * letting the restart proceed.
 * 
 * @author scrawley
 */
public class DefaultRestartDecider implements RestartDecider {
    private static final Logger LOG = 
            LoggerFactory.getLogger(DefaultRestartDecider.class);
    
    /** Default number of milliseconds to wait before restarting */
    public static final int DEFAULT_THROTTLE = 1000;
    
    /** Default number of milliseconds that a service must survive after restart */
    public static final int DEFAULT_THRESHOLD = 100;
    
    /** Default number of milliseconds that a service must survive after first start */
    public static final int DEFAULT_INITIAL_THRESHOLD = 1000;
    
    private boolean firstTime = true;
    private long timestamp = System.currentTimeMillis();
    private int throttleTime;
    private int initialThreshold;
    private int threshold;
    
    /**
     * Instantiate using default settings.
     */
    public DefaultRestartDecider() {
        this(DEFAULT_THRESHOLD, DEFAULT_INITIAL_THRESHOLD, DEFAULT_THROTTLE);
    }

    /**
     * Instantiate with caller supplied settings.
     * 
     * @param threshold the number of milliseconds that the service must
     *   survive after a restart.
     * @param initialThreshold the number of milliseconds that the service must
     *   survive the first time it is started.
     * @param throttleTime the number of milliseconds to delay before restarting.
     */
    public DefaultRestartDecider(int threshold, int initialThreshold,
            int throttleTime) {
        if (threshold < 0 || initialThreshold < 0 || throttleTime < 0) {
            throw new IllegalArgumentException("Arguments must all be >= 0");
        }
        this.throttleTime = throttleTime;
        this.initialThreshold = initialThreshold;
        this.threshold = threshold;
    }

    public boolean isRestartable(Throwable ex) {
        if (ex == null) {
            LOG.debug("Service thread has returned");
            return false;
        }
        long now = System.currentTimeMillis();
        if (firstTime) {
            if (now - timestamp < initialThreshold) {
                LOG.warn("Service thread died (first time) in less than " + initialThreshold + " milliseconds");
                return false;
            }
        } else {
            if (now - timestamp < threshold) {
                LOG.warn("Service thread died in less than " + threshold + " milliseconds");
                return false;
            }
        }
        firstTime = false;
        timestamp = now;
        try {
            if (throttleTime > 0) {
                LOG.debug("Delaying restart for " + throttleTime + " milliseconds");
                Thread.sleep(throttleTime);
            }
        } catch (InterruptedException iex) {
            LOG.debug("Interrupted ...");
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

}
