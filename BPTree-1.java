import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Implementation of a B+ tree to allow efficient access to many different
 * indexes of a large data set. BPTree objects are created for each type of
 * index needed by the program. BPTrees provide an efficient range search as
 * compared to other types of data structures due to the ability to perform
 * log_m N lookups and linear in-order traversals of the data items.
 * 
 * @author sapan (sapan@cs.wisc.edu)
 *
 * @param <K> key - expect a string that is the type of id for each item
 * @param <V> value - expect a user-defined type that stores all data for a food
 *            item
 */
public class BPTree<K extends Comparable<K>, V> implements BPTreeADT<K, V> {

	// Root of the tree
	private Node root;

	// Branching factor is the number of children nodes
	// for internal nodes of the tree
	private int branchingFactor;
	private int count;

	/**
	 * Public constructor
	 * 
	 * @param branchingFactor
	 */
	public BPTree(int branchingFactor) {
		if (branchingFactor <= 2) {
			throw new IllegalArgumentException("Illegal branching factor: " + branchingFactor);
		}
		this.branchingFactor = branchingFactor;
		root = new LeafNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see BPTreeADT#insert(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void insert(K key, V value) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		root.insert(key, value);
		count++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see BPTreeADT#rangeSearch(java.lang.Object, java.lang.String)
	 */
	@Override
	public List<V> rangeSearch(K key, String comparator) {
		if (!comparator.contentEquals(">=") && !comparator.contentEquals("==") && !comparator.contentEquals("<="))
			return new ArrayList<V>();
		return root.rangeSearch(key, comparator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see BPTreeADT#get(java.lang.Object)
	 */
	@Override
	public V get(K key) {
		return root.getValue(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see BPTreeADT#size()
	 */
	@Override
	public int size() {
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Queue<List<Node>> queue = new LinkedList<List<Node>>();
		queue.add(Arrays.asList(root));
		StringBuilder sb = new StringBuilder();
		while (!queue.isEmpty()) {
			Queue<List<Node>> nextQueue = new LinkedList<List<Node>>();
			while (!queue.isEmpty()) {
				List<Node> nodes = queue.remove();
				sb.append('{');
				Iterator<Node> it = nodes.iterator();
				while (it.hasNext()) {
					Node node = it.next();
					sb.append(node.toString());
					if (it.hasNext())
						sb.append(", ");
					if (node instanceof BPTree.InternalNode)
						nextQueue.add(((InternalNode) node).children);
				}
				sb.append('}');
				if (!queue.isEmpty())
					sb.append(", ");
				else {
					sb.append('\n');
				}
			}
			queue = nextQueue;
		}
		return sb.toString();
	}

	/**
	 * This abstract class represents any type of node in the tree This class is a
	 * super class of the LeafNode and InternalNode types.
	 * 
	 * @author sapan
	 */
	private abstract class Node {

		// List of keys
		List<K> keys;

		int keyNumber() {
			return keys.size();
		}

		/**
		 * Package constructor
		 */
		Node() {
			this.keys = new ArrayList<K>();

		}

		/**
		 * Inserts key and value in the appropriate leaf node and balances the tree if
		 * required by splitting
		 * 
		 * @param key
		 * @param value
		 */
		abstract void insert(K key, V value);

		/**
		 * Gets the first leaf key of the tree
		 * 
		 * @return key
		 */
		abstract K getFirstLeafKey();

		abstract V getValue(K key);

		/**
		 * Gets the new sibling created after splitting the node
		 * 
		 * @return Node
		 */
		abstract Node split();

		/*
		 * (non-Javadoc)
		 * 
		 * @see BPTree#rangeSearch(java.lang.Object, java.lang.String)
		 */
		abstract List<V> rangeSearch(K key, String comparator);

		/**
		 * 
		 * @return boolean
		 */
		abstract boolean isOverflow();

		public String toString() {
			return keys.toString();
		}

	} // End of abstract class Node

	/**
	 * This class represents an internal node of the tree. This class is a concrete
	 * sub class of the abstract Node class and provides implementation of the
	 * operations required for internal (non-leaf) nodes.
	 * 
	 * @author sapan
	 */
	private class InternalNode extends Node {

		// List of children nodes
		List<Node> children;

		/**
		 * Package constructor
		 */
		InternalNode() {
			this.keys = new ArrayList<K>();
			this.children = new ArrayList<Node>();
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#getFirstLeafKey()
		 */
		K getFirstLeafKey() {
			K firstKey = children.get(0).getFirstLeafKey();
			return firstKey;
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#isOverflow()
		 */
		boolean isOverflow() {
			boolean result = false;
			if (children.size() > branchingFactor) {
				result = true;
			}
			return result;
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#insert(java.lang.Comparable, java.lang.Object)
		 */
		void insert(K key, V value) {
			if (key == null) {
				throw new IllegalArgumentException();
			}
			Node child = getChildOfNode(key);
			child.insert(key, value);
			if (child.isOverflow()) {
				Node siblingofNode = child.split();
				insertChild(siblingofNode.getFirstLeafKey(), siblingofNode);
			}
			if (root.isOverflow()) {
				Node sibling = split();
				InternalNode newRoot = new InternalNode();
				newRoot.keys.add(sibling.getFirstLeafKey());
				newRoot.children.add(this);
				newRoot.children.add(sibling);
				root = newRoot;
			}
		}

		Node getChildOfNode(K key) {
			int index = 0;
			int pos = Collections.binarySearch(keys, key);
			if (pos >= 0) {
				index = pos + 1;
			} else {
				index = -pos - 1;
			}
			return children.get(index);
		}

		void insertChild(K key, Node child) {
			int index = 0;
			int pos = Collections.binarySearch(keys, key);
			if (pos >= 0) {
				index = pos + 1;
				children.add(index, child);
				keys.add(index, key);
			} else {
				index = -pos - 1;
				keys.add(index, key);
				children.add(index + 1, child);
			}
			;
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#split()
		 */
		Node split() {
			int start = keys.size() / 2 + 1;
			int end = keys.size();
			InternalNode sibling = new InternalNode();
			sibling.keys.addAll(keys.subList(start, end));
			sibling.children.addAll(children.subList(start, end + 1));

			keys.subList(start - 1, end).clear();
			children.subList(start, end + 1).clear();

			return sibling;
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#rangeSearch(java.lang.Comparable, java.lang.String)
		 */
		List<V> rangeSearch(K key, String comparator) {
			return getChildOfNode(key).rangeSearch(key, comparator);
		}

		@Override
		V getValue(K key) {
			return getChildOfNode(key).getValue(key);
		}

	} // End of class InternalNode

	/**
	 * This class represents a leaf node of the tree. This class is a concrete sub
	 * class of the abstract Node class and provides implementation of the
	 * operations that required for leaf nodes.
	 * 
	 * @author sapan
	 */
	private class LeafNode extends Node {

		// List of values
		List<V> values;

		// Reference to the next leaf node
		LeafNode next;

		// Reference to the previous leaf node
		LeafNode previous;

		/**
		 * Package constructor
		 */
		LeafNode() {
			keys = new ArrayList<K>();
			values = new ArrayList<V>();
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#getFirstLeafKey()
		 */
		K getFirstLeafKey() {
			return keys.get(0);
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#isOverflow()
		 */
		boolean isOverflow() {
			boolean result = false;
			if (values.size() > branchingFactor) {
				result = true;
			}
			return result;
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#insert(Comparable, Object)
		 */
		void insert(K key, V value) {
			int index = 0;
			if (value == null) {
				throw new IllegalArgumentException();
			}

			int pos = Collections.binarySearch(keys, key);
			if (pos >= 0) {
				index = pos;
				values.add(index, value);
				keys.add(index, key);
			} else {
				index = -pos - 1;
				keys.add(index, key);
				values.add(index, value);
			}

			if (root.isOverflow()) {
				Node sibling = split();
				InternalNode newRoot = new InternalNode();
				newRoot.keys.add(sibling.getFirstLeafKey());
				newRoot.children.add(this);
				newRoot.children.add(sibling);
				root = newRoot;
			}

		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#split()
		 */
		Node split() {
			LeafNode sibling = new LeafNode();
			int from = (keyNumber() + 1) / 2;
			int to = keyNumber();
			sibling.keys.addAll(keys.subList(from, to));
			sibling.values.addAll(values.subList(from, to));

			keys.subList(from, to).clear();
			values.subList(from, to).clear();
			sibling.previous=this;
			sibling.next = next;
			next = sibling;
			return sibling;
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see BPTree.Node#rangeSearch(Comparable, String)
		 */
		List<V> rangeSearch(K key, String comparator) {
			if (key == null) {
				throw new IllegalArgumentException();
			}

			List<V> result = new ArrayList<V>();
			LeafNode node = this;
			//System.out.println("node"+node.previous);
			while (node.previous != null) {
				node = node.previous;
			}
			if ((comparator.equals(">="))) {
				while (node != null) {
					Iterator<K> kIt = node.keys.iterator();
					Iterator<V> vIt = node.values.iterator();

					while (kIt.hasNext()) {

						K key1 = kIt.next();
						V value1 = vIt.next();
						int cmp1 = key1.compareTo(key);
						if (cmp1 >= 0) {
							result.add(value1);
						}
					}
					node = node.next;
				}

			} else if ((comparator.equals("<="))) {

				while (node != null) {
					Iterator<K> kIt = node.keys.iterator();
					Iterator<V> vIt = node.values.iterator();

					while (kIt.hasNext()) {
						K key1 = kIt.next();
						V value1 = vIt.next();
						int cmp1 = key1.compareTo(key);
						if (cmp1 <= 0) {
							result.add(value1);
						}
					}
					node = node.next;
				}
			} else {
				while (node != null) {
					Iterator<K> kIt = node.keys.iterator();
					Iterator<V> vIt = node.values.iterator();

					while (kIt.hasNext()) {

						K key1 = kIt.next();
						V value1 = vIt.next();

						int cmp1 = key1.compareTo(key);
						if (cmp1 == 0) {
							result.add(value1);
						}
					}
					node = node.next;
				}

			}
			return result;
		}

		@Override
		V getValue(K key) {
			int loc = Collections.binarySearch(keys, key);
			return loc >= 0 ? values.get(loc) : null;
		}

	} // End of class LeafNode

	/**
	 * Contains a basic test scenario for a BPTree instance. It shows a simple
	 * example of the use of this class and its related types.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// create empty BPTree with branching factor of 3
		BPTree<Double, Double> bpTree = new BPTree<>(3);

		// create a pseudo random number generator
		Random rnd1 = new Random();

		// some value to add to the BPTree
		Double[] dd = { 0.0d, 0.5d, 0.2d, 0.8d };

		// build an ArrayList of those value and add to BPTree also
		// allows for comparing the contents of the ArrayList
		// against the contents and functionality of the BPTree
		// does not ensure BPTree is implemented correctly
		// just that it functions as a data structure with
		// insert, rangeSearch, and toString() working.
		List<Double> list = new ArrayList<>();

		for (int i = 0; i < 400; i++) {
			Double j = dd[rnd1.nextInt(4)];
			list.add(j);
			bpTree.insert(j, j);

			System.out.println("\n\nTree structure:\n" + bpTree.toString());
		}
		//System.out.println("rangeSearch" + bpTree.rangeSearch(0.5, ">="));
		List<Double> filteredValues = bpTree.rangeSearch(0.2d, ">=");
		System.out.println("Filtered values: " + filteredValues.toString());
	}

} // End of class BPTree