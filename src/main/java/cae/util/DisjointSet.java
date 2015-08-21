/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

import java.util.ArrayList;

/**
 * Manages a disjoint set (union-find) data structure using lists of arrays.
 * A disjoint set is a method of representing the connectivity of a family of
 * values. Each new value is initialized as the parent of new set. Sets are
 * combined by union, which redirects the parent of the lesser set.
 * <p>
 * In this case, the sets search is expedited by two methods: union by rank
 * and path compression. Rank refers to the length of the set. Implementing
 * union by rank will collapse smaller sets into larger sets, makeing the
 * algorithm more efficient in memory.  Path compression refers to redirecting
 * the path to the parent node.  By doing this, each method within the set
 * will point directly to the parent, instead of trailing through values to
 * reach the parent.  This becomes much more efficient than a linked list and
 * operates on an order of O(a(n)), where a(n) is the inverse of A(n,n), which
 * is the rapidly-growing Ackermann function.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2009.10.14
 */
public class DisjointSet {

  /**
   * Constructs a new disjoint set.
   */
  public DisjointSet() {
    sets = new ArrayList<Set>();
    criticalPoints = new ArrayList<Tuple3>();
  }

  /**
   * Makes a new set with a given value.
   * @param v a value.
   */
  public void makeSet(double v) {
    if (ns>0) {
      boolean adding = true;
      for (Set s:sets)
        if (s.contains(v))
          adding = false;
      if (adding) {
        sets.add(new Set(v));
        ns++;
      }
    } else {
      sets.add(new Set(v));
      ns++;
    }
  }

  /**
   * Unifies two sets contaning two specified values.
   * The union process involves merging the smaller set into the larger set
   * (union by rank).
   * @param a a value.
   * @param b a value.
   */
  public void union(double a, double[][] b, int x, int y) {
    Set aSet = findSet(a);
    Set bSet = findSet(b[x][y]);
    if (aSet!=null && bSet!=null && aSet!=bSet) {
      if (aSet.rank<bSet.rank) {
        bSet.merge(aSet,x,y);
        sets.remove(aSet);
        ns--;
      } else if (aSet.rank>bSet.rank) {
        aSet.merge(bSet,x,y);
        sets.remove(bSet);
        ns--;
      } else {
        if (aSet.parent>bSet.parent) {
          aSet.merge(bSet,x,y);
          sets.remove(bSet);
          ns--;
        } else {
          bSet.merge(aSet,x,y);
          sets.remove(aSet);
          ns--;
        }
      }
    }
  }

  /**
   * Gets the sets.
   * @return the sets.
   */
  public ArrayList<Set> getSets() {
    return sets;
  }

  /**
   * Gets the critical points.
   * @return the critical points.
   */
  public ArrayList<Tuple3> getCriticalPoints() {
    return criticalPoints;
  }

  /**
   * Returns the set associated with a given value.
   * @param v a set value.
   * @return the set that contains v; null if v isn't stored.
   */
  public Set findSet(double v) {
    for (int i=0; i<ns; ++i) {
      if (sets.get(i).contains(v))
        return sets.get(i);
    }
    return null;
  }

  /**
   * Gets the rank.
   * @param v the set number.
   * @return the rank of set v.
   */
  public int getRank(double v) {
    return (findSet(v).rank);
  }


  ////////////////////////////////////////////////////////////////////////////
  // private

  private int ns = 0;
  private ArrayList<Set> sets;
  private ArrayList<Tuple3> criticalPoints;

  /**
   * Handles individual sets.
   * Each set contains a parent, rank, and all the nodes.
   */
  public class Set {

    double parent;  // The parent node of this set.
    double[] nodes; // The child nodes of this set.
    int rank;       // The rank of this set.
    double persistence;

    /**
     * Constructs a new set with a given value.
     * The input value is subsequently established as the parent node.
     * @param v a value.
     */
    private Set(double v) {
      parent = v;
      rank = 0;
      nodes = new double[] {v};
    }

    /**
     * Determines whether this set contains a specified value.
     * @param v a value to search for within this set.
     * @return yes, if it exists; no, otherwise.
     */
    private boolean contains(double v) {
      for (int i=0; i<nodes.length; ++i)
        if (nodes[i]==v) return true;
      return false;
    }
    
    /**
     * Merges a set into this set.
     * The nodes from the input set are appended to the end of this set,
     * causing each of the nodes to contain this parent.  This also gives
     * the path from each node to the parent to be 1, making each path
     * compressed.  If both sets have the same rank, the rank of this set is
     * therefore incremented by 1.
     * @param b a set to be merged into this set.
     */
    private void merge(Set b, int x, int y) {
      double[] t = new double[b.nodes.length+nodes.length];
      for (int i=0; i<t.length; ++i) {
        if (i<nodes.length) t[i] = nodes[i];
        else t[i] = b.nodes[i-nodes.length];
      }
      nodes = t;
      if (rank==b.rank) {
        if (rank==0) {
          criticalPoints.add(new Tuple3(x,y,0));
        } else {
          criticalPoints.add(new Tuple3(x,y,this.parent-b.parent));
        }
        rank++;
      }
    }

    public int size() {
      return nodes.length;
    }
  }


  public void getInfo() {
    System.out.println("\n"+sets.size()+" set(s)");
    for (int i=0; i<sets.size(); ++i) {
      Set s = sets.get(i);
      System.out.println();
      System.out.println("Set #"+(i+1));
      System.out.println("Parent: "+s.parent);
      System.out.println("Rank: "+s.rank);
      System.out.print("Nodes: ");
      for (int j=0; j<s.nodes.length; ++j)
        System.out.print(s.nodes[j]+" ");
      System.out.println();
    }
  }
}
