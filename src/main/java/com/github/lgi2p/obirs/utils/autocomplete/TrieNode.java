package com.github.lgi2p.obirs.utils.autocomplete;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * @author http://sujitpal.blogspot.fr/2007/02/three-autocomplete-implementations.html
 */
// TrieNode.java
public class TrieNode {

  private Character character;
  private HashMap<Character,TrieNode> children;

  public TrieNode(char c) {
    super();
    this.character = new Character(c);
    children = new HashMap<Character,TrieNode>();
  }

  public char getNodeValue() {
    return character.charValue();
  }

  public Collection<TrieNode> getChildren() {
    return children.values();
  }

  public Set<Character> getChildrenNodeValues() {
    return children.keySet();
  }

  public void add(char c) {
    if (children.get(new Character(c)) == null) {
      // children does not contain c, add a TrieNode
      children.put(new Character(c), new TrieNode(c));
    }
  }

  public TrieNode getChildNode(char c) {
    return children.get(new Character(c));
  }

  public boolean contains(char c) {
    return (children.get(new Character(c)) != null);
  }

  public int hashCode() {
    return character.hashCode();
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof TrieNode)) {
      return false;
    }
    TrieNode that = (TrieNode) obj;
    return (this.getNodeValue() == that.getNodeValue());
  }

  public String toString() {
    return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
  }
}