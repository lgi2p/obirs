package com.github.lgi2p.obirs.utils.autocomplete;

import com.github.lgi2p.obirs.utils.JSONConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author adapted from
 * http://sujitpal.blogspot.fr/2007/02/three-autocomplete-implementations.html
 */
public class Autocompletion_Trie {

    private Map<String, Set<AutocompleteElement>> indexLabelsToIDs;
    private TrieNode rootNode;

    public Autocompletion_Trie() {
        super();
        indexLabelsToIDs = new HashMap();
        rootNode = new TrieNode(' ');
    }

    public void load(String id, String label) {

        String labelLowerCase = label.toLowerCase();

        if (!indexLabelsToIDs.containsKey(labelLowerCase)) {
            indexLabelsToIDs.put(labelLowerCase, new HashSet<AutocompleteElement>());
        }
        indexLabelsToIDs.get(labelLowerCase).add(new AutocompleteElement(id, label));
        loadRecursive(rootNode, labelLowerCase + "$");
    }

    private void loadRecursive(TrieNode node, String phrase) {
        if (StringUtils.isBlank(phrase)) {
            return;
        }
        char firstChar = phrase.charAt(0);
        node.add(firstChar);
        TrieNode childNode = node.getChildNode(firstChar);
        if (childNode != null) {
            loadRecursive(childNode, phrase.substring(1));
        }
    }

    private boolean matchPrefix(String prefix) {

        TrieNode matchedNode = matchPrefixRecursive(rootNode, prefix);
        return (matchedNode != null);
    }

    private TrieNode matchPrefixRecursive(TrieNode node, String prefix) {
        if (StringUtils.isBlank(prefix)) {
            return node;
        }
        char firstChar = prefix.charAt(0);
        TrieNode childNode = node.getChildNode(firstChar);
        if (childNode == null) {
            // no match at this char, exit
            return null;
        } else {
            // go deeper
            return matchPrefixRecursive(childNode, prefix.substring(1));
        }
    }

    public Set<AutocompleteElement> findCompletions(String prefix) {
        String prefixLowerCase = prefix.toLowerCase();
        TrieNode matchedNode = matchPrefixRecursive(rootNode, prefixLowerCase);
        List<String> matchedLabels = new ArrayList<String>();
        findCompletionsRecursive(matchedNode, prefixLowerCase, matchedLabels);

        Set<AutocompleteElement> matchedElements = new HashSet<AutocompleteElement>();
        for (String match : matchedLabels) {
            matchedElements.addAll(indexLabelsToIDs.get(match));
        }

        return matchedElements;
    }

    private void findCompletionsRecursive(TrieNode node, String prefix, List<String> completions) {
        if (node == null) {
            // our prefix did not match anything, just return
            return;
        }
        if (node.getNodeValue() == '$') {
            // end reached, append prefix into completions list. Do not append
            // the trailing $, that is only to distinguish words like ann and anne
            // into separate branches of the tree.
            completions.add(prefix.substring(0, prefix.length() - 1));
            return;
        }
        Collection<TrieNode> childNodes = node.getChildren();
        for (TrieNode childNode : childNodes) {
            char childChar = childNode.getNodeValue();
            findCompletionsRecursive(childNode, prefix + childChar, completions);
        }
    }

    @Override
    public String toString() {
        return "Trie:" + rootNode.toString();
    }

    public class AutocompleteElement {

        public String label;
        public String id;

        public AutocompleteElement(String id, String label) {
            this.label = label;
            this.id = id;
        }

        @Override
        public String toString() {
            return "id:" + id + " label:" + label;
        }

    }

    public static void main(String[] args) throws IOException {

        Autocompletion_Trie t = new Autocompletion_Trie();
        t.load("nttp://this/is/an:URI", "This is a test");
        t.load("nttp://this/is/an:URI2", "This is another");
        t.load("nttp://this/is/an:URI3", "Hoops");

        Set<AutocompleteElement> autocomplete = t.findCompletions("t");
        System.out.println(autocomplete);
        String json = JSONConverter.buildJSONString(autocomplete);
        System.out.println(json);

    }
}
