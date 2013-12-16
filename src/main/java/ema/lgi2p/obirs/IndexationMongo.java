/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.jongo.Jongo;
import org.jongo.MongoCollection;


public class IndexationMongo { //implements Indexation {

    private MongoCollection documentsCollection;
    private MongoCollection ontologyConceptsCollection;

    public IndexationMongo(String mongoDbName, String mongoHost, int mongoPort) throws UnknownHostException {
        DB db = new MongoClient(mongoHost, mongoPort).getDB(mongoDbName);
        Jongo jongo = new Jongo(db);
        documentsCollection = jongo.getCollection("documents");
        ontologyConceptsCollection = jongo.getCollection("ontology.concepts");
    }

    public Iterator<Document> iterator() {
        return documentsCollection.find().as(Document.class).iterator();
    }
    
    public Iterable<Document> findByIds(List<String> documentIds) {
        return documentsCollection.find("{_id: {$in: #}}", documentIds).as(Document.class); 
    }

    public Iterable<Concept> findConceptByIds(List<String> conceptIds) {
        return ontologyConceptsCollection.find("{_id: {$in: #}}", conceptIds).as(Concept.class);
    }
    
    public HashSet<Document> getDocumentByConceptId(String conceptId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
