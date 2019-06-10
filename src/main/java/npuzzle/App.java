package npuzzle;

import npuzzle.io.Reader;
import npuzzle.io.Writer;
import npuzzle.logic.State;

import java.util.ArrayList;
import java.util.List;

public class App {
	public static void main(String[] args) {
		State startingState = new Reader("C:\\Users\\User\\Desktop\\Code\\Reports\\STATE.txt").read();
		List<State> states = new ArrayList<>();
		states.add(startingState);

		for (int i = 0; i < 10; i++)
			states.add(new State(startingState));
		new Writer().write(states);
	}
}
