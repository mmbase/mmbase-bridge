/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.security;

/**
 * Thrown by the security authentication implementation to indicate an unknown login authentication
 * 'method/application'.
 * 
 * @author Michiel Meeuwissen
 * @version $Id$
 * @since  MMBase-1.8
 */
public class UnknownAuthenticationMethodException extends org.mmbase.security.SecurityException {
    private static final long serialVersionUID = -1270388603134616140L;

    public UnknownAuthenticationMethodException() {
        super();
    }

    public UnknownAuthenticationMethodException(String message) {
        super(message);
    }

    public UnknownAuthenticationMethodException(Throwable cause) {
        super(cause.getClass().getName() + ": " + cause.getMessage());
        initCause(cause);
    }

    public UnknownAuthenticationMethodException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

}
