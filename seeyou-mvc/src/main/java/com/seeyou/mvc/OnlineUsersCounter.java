package com.seeyou.mvc;

import com.seeyou.logging.MyLogger;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-12-08
 */

@WebListener
public class OnlineUsersCounter implements HttpSessionListener {

    private static int numberOfUserOnline;

    public OnlineUsersCounter() {
        numberOfUserOnline = 0;
    }

    public static int getNumberOfUserOnline() {
        return numberOfUserOnline;
    }

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        synchronized (this) {
            ++numberOfUserOnline;
        }

        MyLogger.info("Session created by Id : " + httpSessionEvent.getSession().getId() + ", online   : " +numberOfUserOnline + ", timeout : " + httpSessionEvent.getSession().getMaxInactiveInterval());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        synchronized (this) {
            ++numberOfUserOnline;
        }

        MyLogger.info("Session destroyed by Id : " + httpSessionEvent.getSession().getId() + ", online  : " + numberOfUserOnline);
    }
}
