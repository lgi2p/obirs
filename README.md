# OBIRS – Ontology Based Information Retrieval System

To take advantages of knowledge models (ontologies) information retrieval systems may use the relationships between concepts to extend or reformulate queries. 
Our ontology based information retrieval system (OBIRS) relies on a domain ontology and on resources that are indexed using its concepts. 
As an example, these resources can be genes annotated by concepts of the Gene Ontology or PubMed articles annotated using the MeSH (Medical Subject Headings).
 
Considering a set of weighted concepts - the weight defines the importance to give to a concept - OBIRS estimates the overall relevance of each resource w.r.t. a given query. 
The retrieved resources are ordered according to their overall scores, so that the most relevant resources (indexed with the exact query concepts) are ranked higher than the least relevant ones (indexed with hypernyms or hyponyms of query concepts). 

This project proposes a in-memory JAVA implementation of OBIRS. 
In order to provide a real-world example of use, the implementation considers specific input and output data.
We therefore consider that the aim is to query a set of documents annotated by concepts defined into an ontology.
Nevertheless, note that the core of OBIRS is generic enough in order to be adapted to any collection of item annotated by concepts defined into an ontology. 
Do not hesitate to contact us if you want to adapt the example.

More details about the theoretical contributions implemented into OBIRS can be found in the following reference:
`
User Centered and Ontology Based Information Retrieval System for Life Sciences 
Mohameth-François Sy, Sylvie Ranwez, Jacky Montmain, Armelle Regnault, Michel Crampes, Vincent Ranwez. 
In BMC Bioinformatics, 13(Suppl 1):S4, 2012
`

## Installation

Please use Maven to compile, build the project and generate the jar that can be used using a command-line interface. 
You can then use the generated JAR (target/obirs.jar).
Then use 
`java -jar target/obirs.jar`
```
usage: obirs
 -c,--concept index <arg>   concept labels file path URI<TAB>label
 -f,--fast-query <arg>      use fast query
 -g,--groupwise <arg>       use groupwise calculation
 -i,--doc index <arg>       index file path
 -o,--ontology <arg>        ontology file path (RDF/XML Format)
 -q,--query-file <arg>      query file
 -r,--refined-query <arg>   refined query
```

## Format

### Collection to query (argument -i)

OBIRS search relevant items into a collection. 
These items are expected to be defined into a input JSON file (one instance description per line).
The JSON syntax used to define an item is the following: 

```
{"uri":"1","label":"f1544.json","annots":["http://www.cea.fr/ontotoxnuc#Tritium"],"href":"/data/toxnuc/toxnuc_annots_5_11_14/annots/f1544.json"}
{"uri":"2","label":"f1064.json","annots":["http://www.cea.fr/ontotoxnuc#Fluorescence","http://www.cea.fr/ontotoxnuc#Urine","http://www.cea.fr/ontotoxnuc#Uranium","http://www.cea.fr/ontotoxnuc#Kina
se","http://www.cea.fr/ontotoxnuc#InVivo","http://www.cea.fr/ontotoxnuc#Phosphorylation"],"href":"/data/toxnuc/toxnuc_annots_5_11_14/annots/f1064.json"}
```

### Ontology (argument -o)

The ontology must be specified into RDF/XML format. 
Adaptations of the source code can easily been made in order to consider other formats.  

### Concept labels (argument -c)

This implementation requires a file that specifies a label associated to each concept. This is required to generate the output (and is not mandatory for the core of Obirs).
Format: ` URI<TAB>label `

```
http://www.cea.fr/AlIII	Al (III)
http://www.cea.fr/DeriveDelArsenic	arsenic derivative
http://www.cea.fr/ontoto#DisciplineMedicale	medical discipline
http://www.cea.fr/ontoto#Immunoprecipitation	immunoprecipitation
http://www.cea.fr/ontoto#Proteomique	proteomic
http://www.cea.fr/ontotoxnu#CP_FMS	CP/MS
http://www.cea.fr/ontotoxnu#CsI	Cs (I)
http://www.cea.fr/ontotoxnuc#13C	13C
http://www.cea.fr/ontotoxnuc#14C	14C
http://www.cea.fr/ontotoxnuc#79Se	79Se

```

### Query examples 

#### simple query (content of file argument -q)

```
{"concepts": [{"uri": "http://www.cea.fr/ontotoxnuc#AnalyseStatistique", "weight": 0.5},{"uri":"http://www.cea.fr/ontotoxnuc#Uranium", "weight": 0.5}]}
```

#### refined query (content of file argument -r)
```
{
	"selectedItemURIs":["http://www.mines-ales.fr/obirs/items/1132","http://www.mines-ales.fr/obirs/items/1133"],
	"rejectedItemURIs":["http://www.mines-ales.fr/obirs/items/1131"],
	"query": {
		"concepts": [
			{"uri": "http://www.cea.fr/ontotoxnuc#AnalyseStatistique", "weight": 0.5},
			{"uri":"http://www.cea.fr/ontotoxnuc#Uranium", "weight": 0.5}
			]
	}
}
```


### Usage examples

#### simple query

Query the collection of items considering a specific set of concepts associated to weights.


command line 
`
java -jar target/obirs.jar -o /data/toxnuc/ontologie_toxnuc_intd_aclp.owl -i /data/toxnuc/toxnuc_annots_5_11_14.json -c /data/toxnuc/ontologie_toxnuc_intd_aclp.owl.labels.index.tsv -q /data/toxnuc/query
`

query 
```
{"concepts": [{"uri": "http://www.cea.fr/ontotoxnuc#AnalyseStatistique", "weight": 0.5},{"uri":"http://www.cea.fr/ontotoxnuc#Uranium", "weight": 0.5}]}
```


result
```
{
    "results": [
            {
                "itemURI":"ns0:1298",
                "score":1.0,
                "itemId":"1298",
                "concepts":[{"relationType":"ns2:EXACT","score":1.0,"queryConceptURI":"ns1:AnalyseStatistique","matchingConceptURI":"ns1:AnalyseStatistique"},{"relationType":"ns2:EXACT","score":1.0,"queryConceptURI":"ns1:Uranium","matchingConceptURI":"ns1:Uranium"}],"href":"\/data\/toxnuc\/toxnuc_annots_5_11_14\/annots\/f1129.json","itemTitle":"f1129.json"},
            {"itemURI":"ns0:379","score":1.0,"itemId":"379","concepts":[{"relationType":"ns2:EXACT","score":1.0,"queryConceptURI":"ns1:AnalyseStatistique","matchingConceptURI":"ns1:AnalyseStatistique"},{"relationType":"ns2:EXACT","score":1.0,"queryConceptURI":"ns1:Uranium","matchingConceptURI":"ns1:Uranium"}],"href":"\/data\/toxnuc\/toxnuc_annots_5_11_14\/annots\/f672.json","itemTitle":"f672.json"},
    ]
    "infoConcepts":[
        {"label":"statistical analysis","uri":"ns1:AnalyseStatistique"},
        {"label":"actinide","uri":"ns1:Actinide"},
        {"label":"uranium","uri":"ns1:Uranium"},
        {"label":"plutonium","uri":"ns1:Plutonium"},
        ...
    ],
    "prefixes":[
        {"ns":"http:\/\/www.cea.fr\/ontotoxnuc#","prefix":"ns1"},
        {"ns":"http:\/\/www.mines-ales.fr\/obirs\/match_type\/","prefix":"ns2"},
        {"ns":"http:\/\/www.mines-ales.fr\/obirs\/items\/","prefix":"ns0"}
    ]}
}
```


#### Refine query

Refine a query considering an expression of interested on the results of a query.

command line 
`
java -jar target/obirs.jar -o /data/toxnuc/ontologie_toxnuc_intd_aclp.owl -i /data/toxnuc/toxnuc_annots_5_11_14.json -c  /data/toxnuc/ontologie_toxnuc_intd_aclp.owl.labels.index.tsv -r /data/toxnuc/queryToRefine
`

refine query 
```
{
	"selectedItemURIs":["http://www.mines-ales.fr/obirs/items/1132","http://www.mines-ales.fr/obirs/items/1133"],
	"rejectedItemURIs":["http://www.mines-ales.fr/obirs/items/1131"],
	"query": {
		"concepts": [
			{"uri": "http://www.cea.fr/ontotoxnuc#AnalyseStatistique", "weight": 0.5},
			{"uri":"http://www.cea.fr/ontotoxnuc#Uranium", "weight": 0.5}
			]
	}
}
```


result

```
{"aggregator":MAX,"numberOfResults":30,"similarityMeasure":LIN,"concepts":[{"weight":1.0,"uri":"http:\/\/www.cea.fr\/ontotoxnuc#InVitro"}],"scoreThreshold":0.0,"aggregatorParameter":2.0}
```


# Contributors

Sébastien Harispe

past : Nicolas Clairon
