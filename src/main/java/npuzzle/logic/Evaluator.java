package npuzzle.logic;

import static npuzzle.utils.Constants.*;

import java.util.*;

class Evaluator {

	private static final Map<Integer, List<Point>> xyListMap     = new HashMap<>();
    private static final Map<Integer, State>       finalStateMap = new HashMap<>();

	@FunctionalInterface
	public interface Heuristic {
		int evaluate(State state, int n);
	}

	private static int manhattan(State state, int n) {
		List<Point> xyList = xyListMap.get(n);
		int[] tiles = state.getTiles();
		int tile;
		int x, y;
		int stateEval = 0;

		for (int index = 0; index < tiles.length; index++) {
			x = index / n; y = index % n;
			tile = tiles[index];
			stateEval += Math.abs(x - xyList.get(tile).x) + Math.abs(y - xyList.get(tile).y);
		}
		return stateEval;
	}

	private static int euclidean(State state, int n) {
		List<Point> xyList = xyListMap.get(n);
		int[] tiles = state.getTiles();
		int tile;
		int x, y;
		int stateEval = 0;

		for ( int index = 0; index < tiles.length; index++) {
			x = index / n; y = index % n;
			tile = tiles[index];
			stateEval += Math.sqrt(Math.pow(x - xyList.get(tile).x, 2) + Math.pow(y - xyList.get(tile).y, 2));
		}
		return stateEval;
	}

//	Counts how many tiles are not in the correct place
	private static int hamming(State state, int n) {
		int[] target = finalStateMap.get(n).getTiles();
		int diff = 0;
		for (int i = 0; i < state.getTiles().length; i++)
			if (state.getTiles()[i] != target[i])
				diff++;
		return diff;
	}

	static Heuristic getHeuristic(String heuristic) {
		switch (heuristic) {
			case MANHATTAN:
				return Evaluator::manhattan;
			case HAMMING:
				return Evaluator::hamming;
			case EUCLIDEAN:
				return Evaluator::euclidean;
			default:
				return null;
		}
	}

	static void addReferenceList(int n) {
		if (xyListMap.containsKey(n)) return;
		State finalState = State.createFinal(n);
		finalStateMap.put(n, finalState);
		int[] finalOrder = finalState.getTiles();
		List<Point> xyList = new ArrayList<>(Collections.nCopies(finalOrder.length, null));

		for ( int i = 0; i < finalOrder.length; i++)
			xyList.set(finalOrder[i], new Point(i / n, i % n));

		xyListMap.put(n, Collections.unmodifiableList(xyList));
	}

	static State getFinal(int n) {
	    return finalStateMap.get(n);
    }

	private static class Point {
		final int x;
		final int y;

		Point(int x, int y) {
			this.x = x; this.y = y;
		}
	}

}
