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
