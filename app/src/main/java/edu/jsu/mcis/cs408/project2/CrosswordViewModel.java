package edu.jsu.mcis.cs408.project2;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CrosswordViewModel extends ViewModel {

    private static final int WORD_DATA_FIELDS = 6;
    private static final int WORD_HEADER_FIELDS = 2;
    public static final char BLOCK_CHAR = '*';
    public static final char BLANK_CHAR = ' ';
    public static final WordDirection ACROSS = WordDirection.ACROSS;
    public static final WordDirection DOWN = WordDirection.DOWN;

    private static final String TAG = "CrosswordViewModel";

    private DatabaseHandler db;

    private final MutableLiveData<HashMap<String, Word>> words = new MutableLiveData<>();

    private final MutableLiveData<char[][]> letters = new MutableLiveData<>();
    private final MutableLiveData<int[][]> numbers = new MutableLiveData<>();

    private final MutableLiveData<Integer> puzzleWidth = new MutableLiveData<>();
    private final MutableLiveData<Integer> puzzleHeight = new MutableLiveData<>();

    private final MutableLiveData<String> cluesAcross = new MutableLiveData<>();
    private final MutableLiveData<String> cluesDown = new MutableLiveData<>();

    // Initialize Shared Model

    public void init(Context context) {
        db = new DatabaseHandler(context.getApplicationContext(), null, null, 1);


        if (words.getValue() == null) {
            loadWords(context);
            try{
                ArrayList<String> wordkeys = db.getAllWords();
                for(String i : wordkeys){
                    int j = Integer.parseInt(i.split(" ")[1]);
                    String word = i.split(" ")[0];
                    guessWord(j, word);
                }
            }catch  (Exception e) {
            }
        }

    }

    // Add Word to Grid
    public void guessWord(int box, String guess){

        HashMap<String, Word> word = words.getValue();

        /*  try catch for null pointer exceptions  */

        try { // if there is a word at location "box" across add the word to the see-able grid
            if (word.get(box + "A").getWord().equals(guess)) {
                addWordToGrid(box+"A");
                db.addWordToStoredWords(word.get(box+"A"));
                //saveToDatabase(word.get(box+"A"));
            }else if (word.get(box + "D").getWord().equals(guess)) {// if there is a word at location "box" down then add the word to the grid
                addWordToGrid(box + "D");
                db.addWordToStoredWords(word.get(box+"D"));
                //saveToDatabase(word.get(box+"D"));
            }
        }catch(Exception a){
            try{ // if there is a word at location "box" down then add the word to the grid
                if (word.get(box + "D").getWord().equals(guess)) {
                    addWordToGrid(box + "D");
                    db.addWordToStoredWords(word.get(box+"D"));
                }
            }catch(Exception d){
                // do nothing
            }
        }

    }

    public boolean isFinished(){
        char[][] lets = letters.getValue();;
        try {
            for (char[] i : lets) {
                for (char j : i) {
                    if (j == BLANK_CHAR) {
                        return false;
                    }
                }
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }

    private void addWordToGrid(String key) {

        // Get word from collection (look up using the given key)
        Word word = Objects.requireNonNull(words.getValue()).get(key);

        // Was the word found in the collection?

        if (word != null) {

            // If so, get properties (row, column, and the word itself)

            int row = word.getRow();
            int column = word.getColumn();
            String w = word.getWord();

            char[][] lets = letters.getValue();
            // Add word to Letters array, one character at a time

            // letters is the variable for the boar

            for (int i = 0; i < w.length(); i++){
                assert lets != null;
                if (word.getDirection() == ACROSS){
                    lets[row][column+i] = w.charAt(i);
                }else{
                    lets[row+i][column] = w.charAt(i);
                }
            }
            letters.postValue(lets);

        }


    }

    // Add all words to grid (for testing purposes only!)
    private void addAllWordsToGrid() {
        for (Map.Entry<String, Word> e : Objects.requireNonNull(words.getValue()).entrySet()) {
            addWordToGrid( e.getKey() );
        }
    }

    // Load game data from puzzle file ("puzzle.csv")

    private void loadWords(Context context) {

        HashMap<String, Word> map = new HashMap<>();
        StringBuilder clueAcrossBuffer = new StringBuilder();
        StringBuilder clueDownBuffer = new StringBuilder();

        // Open puzzle file

        int id = R.raw.puzzle;
        BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id)));

        try {

            String line = br.readLine();
            String[] fields = line.trim().split("\t");

            // Is first row of puzzle file a valid header?

            if (fields.length == WORD_HEADER_FIELDS) {

                // If so, get puzzle height and width from header

                int height = Integer.parseInt(fields[0]);
                int width = Integer.parseInt(fields[1]);

                // Initialize letter and number arrays

                char[][] lArray = new char[height][width];
                int[][] nArray = new int[height][width];

                for (int i = 0; i < height; ++i) {
                    for (int j = 0; j < width; ++j) {
                        lArray[i][j] = BLOCK_CHAR;
                        nArray[i][j] = 0;
                    }
                }

                // Read game data (remainder of puzzle file)

                while ((line = br.readLine()) != null) {

                    // Get word fields from next row

                    fields = line.trim().split("\t");

                    // Is this a valid word?

                    if (fields.length == WORD_DATA_FIELDS) {

                        // If so, initialize new word

                        Word word = new Word(fields);

                        // Get row and column

                        int row = word.getRow();
                        int column = word.getColumn();

                        // Add box number

                        nArray[row][column] = word.getBox();

                        // Clear grid squares
                        String w = word.getWord();
                        for (int i = 0; i < w.length(); i++) {
                            try {
                                if (word.getDirection() == ACROSS) {
                                    lArray[row][column + i] = BLANK_CHAR;
                                } else {
                                    lArray[row + i][column] = BLANK_CHAR;
                                }
                            } catch (Exception e) {
                                // nothing
                            }
                        }
                        // Append Clue to StringBuilder (either clueAcrossBuffer or clueDownBuffer)

                        if(word.getDirection() == ACROSS){

                            clueAcrossBuffer.append(word.getBox() + " " + word.getClue()+"\n");
                        }
                        else{
                            clueDownBuffer.append(word.getBox() + " " + word.getClue()+"\n");
                        }

                        // Create unique key; add word to collection

                        String key = word.getBox() + word.getDirection().toString();
                        map.put(key, word);

                    }

                }

                // Initialize MutableLiveData Members

                words.setValue(map);

                puzzleHeight.setValue(height);
                puzzleWidth.setValue(width);

                letters.setValue(lArray);
                numbers.setValue(nArray);

                cluesAcross.setValue(clueAcrossBuffer.toString());
                cluesDown.setValue(clueDownBuffer.toString());

            }

            br.close();

        }
        catch (Exception e) { Log.e(TAG, e.toString()); }

    }

    // Getter Methods

    public LiveData<char[][]> getLetters() { return letters; }

    public LiveData<int[][]> getNumbers() { return numbers; }

    public LiveData<String> getCluesAcross() { return cluesAcross; }

    public LiveData<String> getCluesDown() { return cluesDown; }

    public LiveData<Integer> getPuzzleWidth() { return puzzleWidth; }

    public LiveData<Integer> getPuzzleHeight() { return puzzleHeight; }

    public int getBoxNumber(int row, int column) {
        return Objects.requireNonNull(numbers.getValue())[row][column];
    }

}