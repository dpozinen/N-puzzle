package npuzzle.logic;

import java.util.*;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import npuzzle.utils.Constants;

public class Executor {

	@FunctionalInterface
	public interface Algorithm {
		/**
		 *
		 * @param initial - the Starting/Initial State from input
		 * @return all parents chained together -> form a list of all moves made to achieve the final state
		 */
		List<State> execute(State initial);
	}

	/**
	 * Flow:
	 * 1. create closed set
	 * 2. current = initial
	 * 3. while current != final
	 * 4. add current to closed set
	 * 5. get StateMap with possible moves state.createChildren()
	 * 6. remove children which are in closed set
	 * 7. if no children left -> break
	 * 8. current = best child
	 *
	 * @see Algorithm#execute(State)
	 */
	private static List<State> executeGreedy(State initial) {
		Set<State> closedSet = new HashSet<>();
		State current = initial;
		TreeMultiset<State> children;

		while (!current.isFinal()) {
			closedSet.add(current);
			children = current.createChildren();
			children.removeAll(closedSet);
			if (children.isEmpty()) {
				System.out.println("unsuccessful"); // TODO: DEL
				break;
			}
			current = children.firstEntry().getElement();
		}

		return current.collectPath();
	}

//	TODO: add g(x)
//	TODO: test
	private static List<State> executeAstar(State initial) {
		HashSet<State> closedSet = new HashSet<>();
		TreeSet<State> openSet = new TreeSet<>();
		State current = initial;
		Multiset<State> children;

		while (!current.isFinal()) {
			closedSet.add(current);
			openSet.remove(current);
			children = current.createChildren();
			children.removeAll(closedSet);
			openSet.addAll(children);
			if (openSet.isEmpty())
				break;
			current = openSet.first();
		}

		return current.collectPath();
	}

	private static List<State> executeUniform(State initial) {
		return Collections.emptyList();
	}

	static Algorithm getAlgorithm(String algorithm) {
		switch (algorithm) {
			case Constants.ASTAR:
				return Executor::executeAstar;
			case Constants.GREEDY:
				return Executor::executeGreedy;
			case Constants.UNIFORM:
				return Executor::executeUniform;
			default:
				return null;
		}
	}

}
