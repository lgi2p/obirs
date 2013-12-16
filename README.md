# Obirs

Obirs is a service that allows you to find resources indexed with concepts.


## Dependancies

mvn install:install-file -Dfile=slib-dist-0.0.5-all-jar.jar -DgroupId=fr.lgi2p.kid -DartifactId=slib -Dversion=0.0.5 -Dpackaging=jar


## Indexexing
The indexation file contains all the documents. The file must have one document
by line serialized in JSON :

{"id": "doc1","title": "document title","conceptIds": ["conceptId1", "conceptId2"],"href": "http://blablah/doc1/show"}
{"id": "doc2","title": "document title 2","conceptIds": ["conceptId2", "conceptId3"],"href": "http://blablah/doc2/show"}

## Query examples

### simple query

{"concepts": [{"id": "D015373", "weight": 0.5},{"id":"D006801", "weight": 0.5}]}

### refined query
{
    "query": {
        "concepts": [{"id": "D015373", "weight": 0.5},{"id":"D006801", "weight": 0.5}]
    },
    "selectedDocIds": ["42172", "42697", "42719"],
    "rejectedDocIds": ["42759"]
}

## Example

### query

String query = "{\"concepts\": [{\"id\": \"D015373\", \"weight\": 0.5},{\"id\":\"D006801\", \"weight\": 0.5}]}";
String results = new Obirs("/path/to/ontology.xml", "/path/to/index.json").query(query);

### refine query

String refinedQuery = "{\"query\": {\"concepts\": [{\"id\": \"D015373\", \"weight\": 0.5},{\"id\":\"D006801\", \"weight\": 0.5}]},\"selectedDocIds\": [\"42172\", \"42697\", \"42719\"],\"rejectedDocIds\": [\"42759\"]}";
String results = new Obirs("/path/to/ontology.xml", "/path/to/index.json").refineQuery(refinedQuery);

## Example with CURL

### query
java -jar obirs.jar -o ~/Documents/Projects/Obirs+UI/ontology/mesh/desc2013.xml -i ~/Documents/Projects/Obirs+UI/samples/index.json  -q '{"concepts": [{"id": "D015373", "weight": 0.5},{"id":"D006801", "weight": 0.5}], "defaultNameSpace": "http://obirs"}'

### fast query
java -jar obirs.jar -o ~/Documents/Projects/Obirs+UI/ontology/mesh/desc2013.xml -i ~/Documents/Projects/Obirs+UI/samples/index.json  -q '{"concepts": [{"id": "D006801", "weight": 0.17},{"id":"D002650", "weight": 0.17},{"id": "D000223", "weight": 0.17},{"id": "D006701", "weight": 0.17},{"id": "D003954", "weight": 0.17},{"id": "D006699", "weight": 0.17}, "defaultNameSpace": "http://obirs"}' -f true

### refine query
java -jar obirs.jar -o ~/Documents/Projects/Obirs+UI/ontology/mesh/desc2013.xml -i ~/Documents/Projects/Obirs+UI/samples/index.json  -r '{"query": {"concepts": [{"id": "D015373", "weight": 0.5},{"id":"D006801", "weight": 0.5}], "defaultNameSpace": "http://obirs"}, "selectedDocIds": ["42172", "42697", "42719"], "rejectedDocIds": ["42759"]}'
