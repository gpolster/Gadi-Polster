package edu.yu.registrar.query;

/**
 * Query objects are used to search our data. The only required field in any query is the ID of the user making the query.
 * Because all subclasses must allow for any combination of their fields to be set or left null, no other field
 * is required to be non-null even in many of the subclasses. However, any fields in subclasses that are primitive
 * types must be initialized to a default "empty" value (e.g. -1 for int) to avoid the appearance of being set
 * when in fact they are not.
 *
 * If all fields in a query subclass are null, that means select ALL instances of the type of data the given query
 * searches for.
 *
 * Query instances can't simply take an "example" model instance to match (as opposed to a bunch of individual fields)
 * because:
 * 1) we want to allow combinations that would be illogical for a single instance of model classes
 * 1a) we would not be able to do the assertions etc. that we should do on model classes if they are to also be used for queries
 *
 *
 * 1) use the code given to you as you starting point - the data model in the model package, and the queries in the query package.
 * 2) define a public API that defines how users submit a query and gets results.
 *    -You have been given subclasses of Query that a user can use to capture what he is searching for.
 *    -If a field is left blank/null in a Query object, that indicates that the user doesn't care about that field for the search.
 *    -If a user searches for something that he may view but not update,
 *       the public API should return an instance of one of the Immutable subclasses you were given
 *    -If a user searches for something that he may only view PART of,
 *       the public API should return an instance of one of the Immutable subclasses with only the data he is allowed to use actually present;
 *       the data he can't view should be null.
 *    -If a user searches for something he may not view at all, the public API should return null
 *    -You don't have to deal with the permutation of a user being able to only write/change part of the object he searched for -
 *       we assume he can either write all of what he was given or nothing of what he was given.
 * 3) pursuant to what we discussed in our lecture on testing, once the public API has been defined,
 *  write JUnit tests that will check if an eventual implementation of the public API meets the requirements listed above.
 */
public class Query {
    private final int userID;

    /**
     * Create a query object to specify to the system what information you want
     * @param userID the user submitting the query
     */
    public Query(int userID) {
        this.userID = userID;
    }

    /**
     * Identify the user making the query so we can determine if he has permission to read/write the data in question
     * @return the ID of the user who is making this query
     */
    public int getUserID(){
        return this.userID;
    }
}
