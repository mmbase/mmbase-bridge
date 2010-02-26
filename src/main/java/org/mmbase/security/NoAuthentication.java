/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.security;

import org.mmbase.util.functions.*;
import java.util.Map;

/**
 * This class is used when no authentication is configured. Every credential is rewarded with a
 * UserContext object. So every attempt to log in will succeed.
 *
 * @author Eduard Witteveen
 * @version $Id$
 * @see UserContext
 */
final public class NoAuthentication extends Authentication {

    static final String TYPE = "no authentication";

    private static final class NoAuthenticationUser extends BasicUser {
        private static final long serialVersionUID = -232773890397204840L;

        NoAuthenticationUser() {
            super(new NoAuthentication(), TYPE, "anonymous");
        }
        NoAuthenticationUser(NoAuthentication auth, String un) {
            super(auth, TYPE, un);
        }
    }

    static final UserContext userContext = new NoAuthenticationUser();
    // package because NoAuthorization uses it to get the one 'possible context' (which is of course the 'getOwnerField' of the only possible user)
    // (this is assuming that NoAuthentication is used too, but if not so, that does not matter)

    /**
     *	This method does nothing
     */
    protected void load() {
    }

    /**
     * Returns always the same object (an user 'anonymous'with rank 'administrator'')
     * @see UserContext
     */
    public UserContext login(String application, Map loginInfo, Object[] parameters) throws SecurityException {
        String userName = loginInfo == null ? null : (String) loginInfo.get(AuthenticationData.PARAMETER_USERNAME.getName());
        if (userName != null) {
            return new NoAuthenticationUser(this, userName);
        } else {
            return userContext;
        }
    }

    /**
     * Users remain valid always.
     * @return true
     */
    public boolean isValid(UserContext usercontext) throws SecurityException {
        return usercontext instanceof NoAuthenticationUser;
    }

    /**
     * {@inheritDoc}
     * @since MMBase-1.8
     */
    @Override
    public int getDefaultMethod(String protocol) {
        return METHOD_DELEGATE;
    }
    @Override
    public String[] getTypes(int method) {
        return new String[] {TYPE};
    }

    @Override
    public Parameters createParameters(String application) {
        return new AutodefiningParameters();
    }

}
