import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Tester class for Wordle.java
 * Usage: java TesterWordle [testName]
 * Supported testNames: readDictionary, chooseSecretWord, containsChar, computeFeedback, storeGuess, isAllGreen, testGameWin, testGameLose, testGameInvalid
 */
public class TesterWordle {

    public static void main(String[] args) {
        if (args.length == 0) {
            runAll();
        } else {
            switch (args[0]) {
                case "readDictionary": testReadDictionary(); break;
                case "chooseSecretWord": testChooseSecretWord(); break;
                case "containsChar": testContainsChar(); break;
                case "computeFeedback": testComputeFeedback(); break;
                case "storeGuess": testStoreGuess(); break;
                case "isAllGreen": testIsAllGreen(); break;
                case "testGameWin": testGameWin(); break;
                case "testGameLose": testGameLose(); break;
                case "testGameInvalid": testGameInvalid(); break;
                default: runAll();
            }
        }
    }

    private static void runAll() {
        testReadDictionary();
        testChooseSecretWord();
        testContainsChar();
        testComputeFeedback();
        testStoreGuess();
        testIsAllGreen();
        // Game tests are not run here because they close System.in and must be run separately
    }

    // --- Unit Tests ---

    public static void testReadDictionary() {
        System.out.println("Testing readDictionary:");
        try {
            String[] dict = Wordle.readDictionary("dictionary.txt");
            if (dict == null) {
                System.out.println("Failed: Dictionary is null");
                return;
            }
            if (dict.length < 10) { 
                System.out.println("Failed: Dictionary seems too small");
                return;
            }
            boolean found = false;
            for (String s : dict) {
                if ("APPLE".equals(s)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                System.out.println("Passed: Dictionary read successfully (Passed)");
            } else {
                System.out.println("Failed: 'APPLE' not found in dictionary");
            }
        } catch (Exception e) {
            System.out.println("Failed: Exception thrown - " + e.getMessage());
        }
    }

    public static void testChooseSecretWord() {
        System.out.println("Testing chooseSecretWord:");
        String[] mockDict = {"ONE", "TWO", "THREE"};
        boolean passed = true;
        
        for (int i = 0; i < 10; i++) {
            String secret = Wordle.chooseSecretWord(mockDict);
            if (secret == null) {
                System.out.println("Failed: Secret word is null");
                passed = false;
                break;
            }
            boolean found = false;
            for (String w : mockDict) {
                if (w.equals(secret)) found = true;
            }
            if (!found) {
                System.out.println("Failed: Returned word '" + secret + "' is not in the dictionary");
                passed = false;
                break;
            }
        }

        if (passed) {
            System.out.println("Passed: Selected valid words from dictionary (Passed)");
        }
    }

    public static void testContainsChar() {
        System.out.println("Testing containsChar:");
        int passed = 0;
        int total = 4;

        if (Wordle.containsChar("HELLO", 'H')) passed++;
        else System.out.println("Failed: 'HELLO' should contain 'H'");

        if (Wordle.containsChar("HELLO", 'O')) passed++;
        else System.out.println("Failed: 'HELLO' should contain 'O'");

        if (!Wordle.containsChar("HELLO", 'A')) passed++;
        else System.out.println("Failed: 'HELLO' should NOT contain 'A'");

        if (!Wordle.containsChar("WORLD", 'X')) passed++;
        else System.out.println("Failed: 'WORLD' should NOT contain 'X'");

        if (passed == total) {
            System.out.println("Passed " + passed + "/" + total + " tests (Passed)");
        } else {
            System.out.println("Passed " + passed + "/" + total + " tests");
        }
    }

    public static void testComputeFeedback() {
        System.out.println("Testing computeFeedback:");
        int passed = 0;
        int total = 3;

        // Test 1: Exact Match
        char[] res1 = new char[5];
        Wordle.computeFeedback("ABCDE", "ABCDE", res1);
        if (new String(res1).equals("GGGGG")) passed++;
        else System.out.println("Failed: ABCDE vs ABCDE. Expected GGGGG, got " + new String(res1));

        // Test 2: No Match
        char[] res2 = new char[5];
        Wordle.computeFeedback("ABCDE", "VWXYZ", res2);
        String s2 = new String(res2).replace('\0', '_'); 
        if (s2.equals("_____")) passed++;
        else System.out.println("Failed: ABCDE vs VWXYZ. Expected _____, got " + s2);

        // Test 3: The PDF Example (APPLE vs PAPAL)
        char[] res3 = new char[5];
        Wordle.computeFeedback("APPLE", "PAPAL", res3);
        if (new String(res3).equals("YYGYY")) passed++;
        else System.out.println("Failed: APPLE vs PAPAL. Expected YYGYY, got " + new String(res3));

        if (passed == total) {
            System.out.println("Passed " + passed + "/" + total + " tests (Passed)");
        } else {
            System.out.println("Passed " + passed + "/" + total + " tests");
        }
    }

    public static void testStoreGuess() {
        System.out.println("Testing storeGuess:");
        char[][] guesses = new char[6][5];
        String guess = "HELLO";
        
        Wordle.storeGuess(guess, guesses, 1); // Store at row 1 (second attempt)
        
        String stored = new String(guesses[1]);
        String emptyRow = new String(guesses[0]).replace('\0', '_');
        
        boolean correctRow = stored.equals("HELLO");
        boolean untouchedRow = emptyRow.equals("_____");
        
        if (correctRow && untouchedRow) {
            System.out.println("Passed: Correctly stored guess in 2D array (Passed)");
        } else {
            if (!correctRow) System.out.println("Failed: Row 1 should be HELLO, got " + stored);
            if (!untouchedRow) System.out.println("Failed: Row 0 should be empty, got " + emptyRow);
        }
    }

    public static void testIsAllGreen() {
        System.out.println("Testing isAllGreen:");
        int passed = 0;
        int total = 3;

        char[] allG = {'G','G','G','G','G'};
        char[] mixed = {'G','Y','G','G','G'};
        char[] incomplete = {'G','G','_','G','G'};

        if (Wordle.isAllGreen(allG)) passed++;
        else System.out.println("Failed: GGGGG should return true");

        if (!Wordle.isAllGreen(mixed)) passed++;
        else System.out.println("Failed: GYGGG should return false");

        if (!Wordle.isAllGreen(incomplete)) passed++;
        else System.out.println("Failed: GG_GG should return false");

        if (passed == total) {
            System.out.println("Passed " + passed + "/" + total + " tests (Passed)");
        } else {
            System.out.println("Passed " + passed + "/" + total + " tests");
        }
    }

    // --- Game Scenario Tests ---

    public static void testGameWin() {
        // Scenario: Secret is APPLE. User guesses HELPS then APPLE.
        String input = "HELPS\nAPPLE\n";
        runGameWithInput("APPLE", input, "Win Scenario");
    }

    public static void testGameLose() {
        // Scenario: Secret is APPLE. User guesses WRONG 6 times.
        String input = "WRONG\nWRONG\nWRONG\nWRONG\nWRONG\nWRONG\n";
        runGameWithInput("APPLE", input, "Lose Scenario");
    }

    public static void testGameInvalid() {
        // Scenario: Secret is APPLE. User enters invalid words first.
        String input = "ABC\nABCDEF\nAPPLE\n";
        runGameWithInput("APPLE", input, "Invalid Input Scenario");
    }

    private static void runGameWithInput(String secret, String inputData, String testName) {
        System.out.println("Testing " + testName + ":");
        File original = new File("dictionary.txt");
        File backup = new File("dictionary_backup.tmp");
        boolean backedUp = false;
        try {
            if (original.exists()) {
                if (original.renameTo(backup)) backedUp = true;
                else { System.out.println("Failed to backup dictionary."); return; }
            }
            try (FileWriter writer = new FileWriter("dictionary.txt")) { writer.write(secret); }
            System.setIn(new ByteArrayInputStream(inputData.getBytes()));
            Wordle.main(new String[]{});
        } catch (Exception e) {
            System.out.println("Game crashed: " + e.getMessage());
        } finally {
            File temp = new File("dictionary.txt");
            if (temp.exists()) temp.delete();
            if (backedUp && backup.exists()) backup.renameTo(original);
        }
    }
}