package npuzzle.io;

import static npuzzle.utils.Constants.MANHATTAN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import npuzzle.logic.State;

public class Input {

	private final String[] args;

	private String file, algorithm, heuristic;
	private int n;
	private boolean isRandom;
	private int[] tiles;
	private State initialState;

	public Input(String[] args) {
		this.args = args;
	}

	private Input(int[] tiles, int n, String algorithm, String heuristic, State initialState) {
		this.tiles = tiles;
		this.n = n;
		this.algorithm = algorithm;
		this.heuristic = heuristic;
		this.initialState = initialState;
		this.args = null;
	}

	static Input fromArgs(String[] args) {
		return new Input(args);
	}

	public static Input create(int[] tiles, int n, String algorithm, String heuristic) {
		return new Input(tiles, n, algorithm, heuristic, State.createFrom(tiles, heuristic));
	}

	boolean hasFile() {
		return file != null;
	}

	String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getHeuristic() {
		return heuristic;
	}

	void setHeuristic(String heuristic) {
		this.heuristic = heuristic;
	}

	void setTilesAndN(int[] tiles, int n) {
		this.tiles = tiles;
		this.n = n;
	}

	int[] getTiles() {
		return tiles.clone();
	}

	void generateRandomTiles(int n) {
		this.n = n;
		isRandom = true;
		int nByN = n * n;

		List<Integer> tiles = new ArrayList<>(nByN);
		while (--nByN >= 0)
			tiles.add(nByN);

		do Collections.shuffle(tiles);
			while (State.createFrom(tiles.stream().mapToInt(i -> i).toArray(), MANHATTAN).isNotSolvable());
		this.tiles = tiles.stream().mapToInt(i -> i).toArray();
	}

	boolean isRandom() {
		return isRandom;
	}

	String[] getArgs() {
		return args;
	}

	public State getInitialState() {
		return initialState;
	}

	void setInitialState(State initial) {
		this.initialState = initial;
	}

	@Override
	public String toString() {
		return String.format("Algorithm = %s; heuristic = %s; n = %d; isRandom = %b.", algorithm, heuristic, n, isRandom);
	}

	public int getN() {
		return n;
	}

}
