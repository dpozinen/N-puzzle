package npuzzle.io;

import npuzzle.logic.State;

import java.util.List;

/**
 * @author dpozinen
 * @author ollevche
 * <p>
 * used to write program output
 */

public class Writer {

	public static void write(State state) {
		StringBuilder rows = new StringBuilder();
		int col = 0;

		for (Integer tile : state.getTiles()) {
			rows.append(String.format("%5s", String.valueOf(tile)));
			if (++col < Input.getInstance().getN()) {
				rows.append(" ");
			} else {
				rows.append("\n");
				col = 0;
			}
		}
		System.out.println(rows);
	}

	// TODO: count proper offset
	public static void write(List<State> states) {
		int i = states.size();
		int offset = 15;
		String format = "%" + offset + "s\n";
		String arrowFormat = "%" + (offset + 1) + "s\n";

		for (State state : states) {
			write(state);
			if (--i > 0) {
				System.out.printf(format, "|");
				System.out.printf(format, "|");
				System.out.printf(arrowFormat, "\\ /");
			}
		}
	}
}
