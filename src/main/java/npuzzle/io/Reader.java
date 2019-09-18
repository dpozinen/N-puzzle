package npuzzle.io;

import npuzzle.logic.State;
import npuzzle.utils.Error;
import npuzzle.utils.InvalidInputException;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static npuzzle.utils.Constants.*;

/**
 * @author dpozinen
 * @author ollevche
 * <p>
 * used to read program input
 */

public class Reader {

    private final Input input;
    private final Validator validator;
    private static final Options options = prepareOptions();

    private Reader(Input input) {
        this.input = input;
        validator = new Validator();
    }

    public static Reader createWith(Input input) {
        return new Reader(input);
    }

    private static Options prepareOptions() {
        Options options = new Options();

        options.addRequiredOption("a", ALGORITHM, true, ALGORITHM_DESCRIPTION);
        options.addOption("h", HEURISTIC, true, HEURISTIC_DESCRIPTION);
        options.addOption("f", FILE, true, FILE_DESCRIPTION);
        options.addOption("r", RANDOM, true, RANDOM_DESCRIPTION);

        return options;
    }

    public boolean fillInput() {
        try {
            if (input.getArgs() == null) {
                if (isManualInput()) {
                    input.setInitialState(State.createFrom(input.getTiles(), input.getHeuristic()));
                    return true;
                } else return false;
            }
            parseArgs(input.getArgs());
            if (!input.isRandom())
                readTiles();
            input.setInitialState(State.createFrom(input.getTiles(), input.getHeuristic()));
            return true;
        } catch (IOException e) {
            System.err.println("Cannot read input: " + e.getMessage());
        } catch (ParseException e) {
            System.err.println("Invalid argument: " + e.getMessage());
            new HelpFormatter().printHelp("N-puzzle", options);
        } catch (InvalidInputException e) {
            System.err.println(e.getMessage());
        }

        return false;
    }

    private boolean isManualInput() {
        return Objects.nonNull(input.getAlgorithm()) && Objects.nonNull(input.getHeuristic())
                && Objects.nonNull(input.getTiles()) && input.getN() != 0;
    }

    private void parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args);

        validator.saveValidAlgorithm(line.getOptionValue(ALGORITHM));
        validator.saveValidHeuristic(line.getOptionValue(HEURISTIC));
        validator.saveValidatedFile(line.getOptionValue(FILE));
        if (line.hasOption(RANDOM))
            validator.saveValidRandomArg(line.getOptionValue(RANDOM));
    }

    public void readTiles() throws IOException {

        if (input.hasFile()) {
            Files.readAllLines(Paths.get(input.getFile())).forEach(validator::validateLine);
            finalCheck();
        } else {
            synchronized (System.in) {
                synchronized (System.out) {
                    readFromStdIn();
                    finalCheck();
                }
            }
        }

    }

    private void readFromStdIn() throws IOException {
        String line;
        try {
            // this is added because if there was an error during reading, the error message will print after
            // the 'Enter input' message, which is confusing. This is a work around, admittedly not the best one
            Thread.sleep(300);
        } catch (InterruptedException e) {
            System.err.println("PANIC PANIC PANIC THREAD WAS INTERRUPTED");
            Thread.currentThread().interrupt(); // should not happen ever so if does - God save us
            return;
        }
        System.out.printf("Enter input:%n");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (StringUtils.isNotBlank(line = br.readLine())) validator.validateLine(line);
    }

    private void finalCheck() {
        if (validator.n == 0)
            throw new InvalidInputException(Error.NO_SIZE);
        validator.checkEnoughTiles();
        validator.saveValidatedTiles(input);

        if (State.createFrom(input.getTiles(), MANHATTAN).isNotSolvable())
            throw new InvalidInputException(Error.UNSOLVABLE);
    }

    public static List<Input> splitArgs(String[] args) {
        List<Input> inputList = new ArrayList<>();
        List<String> argParts = Arrays.asList(StringUtils.join(args, " ").split("\\|"));

        for (String arg : argParts)
            inputList.add(Input.fromArgs(arg.trim().split(" ")));

        return inputList;
    }

    private class Validator {

        boolean isNSet;
        final List<Integer> tiles;
        int n;

        Validator() {
            tiles = new ArrayList<>();
        }

        void validateLine(String line) {
            List<String> elements = splitLineAndRemoveComments(line);

            if (elements.isEmpty())
                return;

            checkNonNumeric(elements);

            List<Integer> intValues = extractIntValues(elements);

            if (trySetN(intValues))
                return;

            checkMaxSizeAndValue(intValues);
            checkDuplicates(intValues);
            checkDuplicates(tiles);
            tiles.addAll(intValues);
        }

        void checkDuplicates(List<Integer> values) {
            if (!values.stream().allMatch(new HashSet<Integer>()::add))
                throw new InvalidInputException(Error.DUPLICATES);
        }

        void checkMaxSizeAndValue(List<Integer> intValues) {
            if (intValues.stream().anyMatch(i -> i > n * n - 1))
                throw new InvalidInputException(Error.OVER_MAX); // TODO: fix 3\n 012 -> 0 1 2 | NOTE: seems to work on mac

            if (intValues.size() != n)
                throw new InvalidInputException(Error.WRONG_AMOUNT, String.valueOf(n - intValues.size()));
        }

        void checkEnoughTiles() {
            int diff = (int) Math.pow(n, 2) - tiles.size();

            if (diff != 0)
                throw new InvalidInputException(Error.NOT_ENOUGH_TILES, String.valueOf(diff));
        }

        boolean trySetN(List<Integer> intValues) {
            if (!isNSet) {
                if (intValues.size() == 1) {
                    n = intValues.get(NO_TILE);
                    return isNSet = true;
                } else
                    throw new InvalidInputException(Error.NO_SIZE);
            }
            return false;
        }

        List<String> splitLineAndRemoveComments(String line) {
            List<String> elements = Arrays.asList(line.trim().split("\\s+"));
            return extractPartsBeforeComment(elements);
        }

        void checkNonNumeric(List<String> elements) {
            if (elements.stream().anyMatch(s -> !s.matches("\\d+")))
                throw new InvalidInputException(Error.NON_NUMERIC);
        }

        List<String> extractPartsBeforeComment(List<String> elements) {
            List<String> beforeComment = new ArrayList<>();
            boolean isComment = false;

            for (String element : elements) {
                if (element.startsWith("#"))
                    isComment = true;
                if (!isComment)
                    beforeComment.add(element);
            }
            return beforeComment;
        }

        List<Integer> extractIntValues(List<String> elements) {
            return elements.stream().map(Integer::valueOf).collect(Collectors.toList());
        }

        void saveValidRandomArg(String undef) {
            undef = undef.trim();

            if (!undef.matches("\\d+"))
                throw new InvalidInputException(Error.NON_NUMERIC, undef);

            int randomN = Integer.valueOf(undef);
            if (randomN < 2)
                throw new InvalidInputException(Error.RANDOM_TOO_SMALL, undef);

            input.generateRandomTiles(randomN);
        }

        void saveValidAlgorithm(String undef) {
            String algorithm;

            switch (undef.trim().toLowerCase()) {
                case GREEDY:
                    algorithm = GREEDY;
                    break;
                case UNIFORM:
                    algorithm = UNIFORM;
                    break;
                case ASTAR:
                    algorithm = ASTAR;
                    break;
                default:
                    throw new InvalidInputException(Error.ARG_NOT_FOUND, undef);
            }

            input.setAlgorithm(algorithm);
        }

        void saveValidHeuristic(String undef) {
            String heuristic;
            undef = undef == null ? "" : undef;

            switch (undef.trim().toLowerCase()) {
                case MANHATTAN:
                    heuristic = MANHATTAN;
                    break;
                case HAMMING:
                    heuristic = HAMMING;
                    break;
                case EUCLIDEAN:
                    heuristic = EUCLIDEAN;
                    break;
                default:
                    heuristic = StringUtils.EMPTY;
            }

            if (heuristic.isEmpty() && !input.getAlgorithm().equals(UNIFORM))
                throw new InvalidInputException(Error.ARG_NOT_FOUND, undef);

            input.setHeuristic(heuristic);
        }

        void saveValidatedFile(String absolutePath) {
            input.setFile(absolutePath);
        }

        void saveValidatedTiles(Input input) {
            input.setTilesAndN(tiles.stream().mapToInt(i -> i).toArray(), n);
        }

    }
}
