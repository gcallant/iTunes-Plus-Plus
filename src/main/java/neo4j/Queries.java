package neo4j;

import org.neo4j.driver.v1.Session;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Executes queries
 *
 * @author Josh Cotes
 */
public class Queries {

    /**
     * Creates a reciprocal relationship between two nodes given properties.
     *
     * @param label1 - first node label
     * @param property1 - first node subject
     * @param value1 - first node subject value
     * @param relationship - relationship to second node
     * @param label2 - second node label
     * @param property2 - second node subject
     * @param value2 - second node value
     */
    public static void createRelationshipReciprocal(Session session, String label1, String property1, String value1, String relationship,
                                             String label2, String property2, String value2, String relation2) {

        createRelationship(session, label1, property1, value1, relationship, label2, property2, value2);
        createRelationship(session, label2, property2, value2, relation2, label1, property1, value1);
    }

    /**
     * Creates a relationship from the first node to the second.
     *
     * @param label1 - first node label
     * @param property1 - first node subject
     * @param value1 - first node subject value
     * @param relationship - relationship to second node
     * @param label2 - second node label
     * @param property2 - second node subject
     * @param value2 - second node value
     */
    public static void createRelationship(Session session, String label1, String property1, String value1, String relationship,
                                          String label2, String property2, String value2) {

        session.run("MATCH  (one:" + label1 + " {" + property1 + ":\"" + value1 + "\"} )" +
                "MATCH  (two:" + label2 + " {" + property2 + ":\"" + value2 + "\"} )" +
                "CREATE (one)-[" + relationship + ":" + relationship + "]" +
                "->(two)");
    }

    /**
     * Returns true if there exists a node with the given parameters
     * @param label - the node label
     * @param subject - the subject
     * @param value - the subject value
     * @return - true if node exists
     */
    public static boolean nodeExists(Session session, String label, String subject, String value) {

        return session.run("MATCH (a:" + label + ") WHERE a." + subject + " = {" + subject + "} " +
                        "RETURN a." + subject + " AS " + subject,
                parameters(subject, value)).hasNext();
    }

    /**
     * Returns true if there exists a specific relationship between two nodes.
     *
     * @param label1 - first node label
     * @param property1 - first node subject
     * @param value1 - first node subject value
     * @param relationship - relationship to second node
     * @param label2 - second node label
     * @param property2 - second node subject
     * @param value2 - second node value
     * @return - true if that relationship exists
     */
    public static boolean relationshipExists(Session session, String label1, String property1, String value1, String relationship,
                                      String label2, String property2, String value2) {

        String query = "MATCH (" + label1 + ":" + label1 + ")-[:" + relationship + "]->(" + label2 + ":" + label2 + ") " +
                "WHERE " + label1 + "." + property1 + " = \"" + value1 + "\" " +
                "AND " + label2 + "." + property2 + " = \"" + value2 + "\" " +
                "RETURN " + label1 + "." + property1 + ", " + label2 + "." + property2;

        return session.run(query).hasNext();
    }
}
