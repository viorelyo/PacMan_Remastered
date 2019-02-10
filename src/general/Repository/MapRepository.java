package general.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * MapRepository for Main Grid
 * Reads the data from CSV and saves in a Matrix
 */
public class MapRepository {
    public static List<int[]> labyrinth;

    /**
     * Constructor for repository.
     * Reads the CSV file
     */
    public MapRepository() {
        String path = "/Data/Labyrinth.csv";
        try {
            readFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the data from CSV File, Creates Objects and saves to list
     */
    private void readFile(String path) throws IOException {
        labyrinth = new ArrayList<>();

        try{
            InputStream inputFS = getClass().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));

            labyrinth = br.lines().map(mapToItem).collect(Collectors.toList());
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Splits a line by Comma and converts each string element to int
     * Returns the array of ints
     */
    private Function<String, int[]> mapToItem = (line) -> {
        String[] elements = line.split(",");
        int[] ints = Arrays.stream(elements).mapToInt(Integer::parseInt).toArray();

        return ints;
    };
}


