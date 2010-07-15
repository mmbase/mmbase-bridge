/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.storage.search;


/**
 * Defines methods for an object that handles search query requests.
 *
 * @author Rob van Maris
 * @version $Id$
 * @since MMBase-1.7
 */
public interface SearchQueryHandler {
    /**
     * Support level for features that are not supported.
     */
    public final static int SUPPORT_NONE = 0;

    /**
     * Support level for features that are supported, but not recommended when performance is  critical.
     */
    public final static int SUPPORT_WEAK = 1;

    /**
     * Support level for features that are available for use under normal circumstances.
     */
    public final static int SUPPORT_NORMAL = 2;

    /**
     * Support level for features that are optimally supported. This applies also to features that are supported without a significant impact on performance penalty.
     */
    public final static int SUPPORT_OPTIMAL = 3;

    /**
     * Feature that allows specifying the maximum number of results to be returned.
     * @see SearchQuery#getMaxNumber
     */
    public final static int FEATURE_MAX_NUMBER = 1;

    /**
     * Feature that allows specifying an index in the list of results, as a starting popublic final static int for results to be returned.
     * @see SearchQuery#getOffset
     */
    public final static int FEATURE_OFFSET = 2;


    /**
     * Feature that allows to search on string by a regular expression.
     * @see SearchQuery#getOffset
     */
    public final static int FEATURE_REGEXP = 3;

    /**
     * Gets the level at which a feature is supported for a query
     * by this handler. This is one of either:
     * <ul>
     * <li>{@link #SUPPORT_NONE SUPPORT_NONE}
     * <li>{@link #SUPPORT_WEAK SUPPORT_WEAK}
     * <li>{@link #SUPPORT_NORMAL SUPPORT_NORMAL}
     * <li>{@link #SUPPORT_OPTIMAL SUPPORT_OPTIMAL}
     * </ul>
     * Given the choice, the query handler with the highest level of support is prefered.
     */
    public int getSupportLevel(int feature, SearchQuery query) throws SearchQueryException;

    /**
     * Gets the level at which a constraint is supported for a query
     * by this handler. This is one of either:
     * <ul>
     * <li>{@link #SUPPORT_NONE SUPPORT_NONE}
     * <li>{@link #SUPPORT_WEAK SUPPORT_WEAK}
     * <li>{@link #SUPPORT_NORMAL SUPPORT_NORMAL}
     * <li>{@link #SUPPORT_OPTIMAL SUPPORT_OPTIMAL}
     * </ul>
     * Given the choice, the query handler with the highest level of support is prefered.
     */
    public int getSupportLevel(Constraint constraint, SearchQuery query) throws SearchQueryException;


    /**
     * Makes a String of a query, taking into consideration if the database supports offset and
     * maxnumber features. The resulting String is an SQL query which can be fed to the database.
     * @param query the query to convert to sql
     * @return the sql string
     * @throws SearchQueryException when error occurs while making the string
     * @since MMBase-1.8.5
     */
    public String createSqlString(SearchQuery query) throws SearchQueryException;

}
