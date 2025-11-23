import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;

/**
 * Tester class for Wordle.java
 * Usage: java TesterWordle [testName]
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
                // Old simple tests (still useful for quick checks)
                case "testGameWin": testGameWin(); break; 
                // New Robust tests
                case "testGameWinRobust": testGameWinRobust(); break;
                case "testGameLoseRobust": testGameLoseRobust(); break;
                case "testGameInvalidRobust": testGameInvalidRobust(); break;
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
    }

    // --- Robust Game Tests (Captures Output) ---

    public static void testGameWinRobust() {
        System.out.println("Testing Game Flow (Win) - Robust:");
        
        // Scenario: Secret "APPLE". Guess "HELPS" (_YYY_), then "APPLE" (GGGGG)
        String input = "HELPS\nAPPLE\n";
        String output = captureGameOutput("APPLE", input);

        boolean passed = true;

        // Check 1: Did it process the first guess?
        if (!output.contains("Guess 1: HELPS")) {
            System.out.println("Failed: Output missing 'Guess 1: HELPS'");
            passed = false;
        }

        // Check 2: Did it calculate feedback for HELPS correctly?
        // H(_), E(Y), L(Y), P(Y), S(_) -> _YYY_
        if (!output.contains("_YYY_")) {
            System.out.println("Failed: Feedback for HELPS incorrect. Expected '_YYY_' (or similar formatting)");
            passed = false;
        }

        // Check 3: Did it process the second guess?
        if (!output.contains("Guess 2: APPLE")) {
            System.out.println("Failed: Output missing 'Guess 2: APPLE'");
            passed = false;
        }

        // Check 4: Did it detect the win?
        if (!output.contains("Congratulations")) {
            System.out.println("Failed: Output missing 'Congratulations'");
            passed = false;
        }

        if (passed) System.out.println("Passed: Game flow correct for winning scenario (Passed)");
    }

    public static void testGameLoseRobust() {
        System.out.println("Testing Game Flow (Lose) - Robust:");

        // Scenario: Secret "APPLE". 6 wrong guesses.
        String input = "ZZZZZ\nZZZZZ\nZZZZZ\nZZZZZ\nZZZZZ\nZZZZZ\n";
        String output = captureGameOutput("APPLE", input);

        boolean passed = true;

        // Check 1: Did it run 6 times?
        if (!output.contains("Guess 6:")) {
            System.out.println("Failed: Game ended too early or didn't print 'Guess 6'");
            passed = false;
        }

        // Check 2: Did it print the secret at the end?
        if (!output.contains("secret word was")) {
            System.out.println("Failed: Did not reveal secret word after losing");
            passed = false;
        }

        // Check 3: Should NOT say Congratulations
        if (output.contains("Congratulations")) {
            System.out.println("Failed: Printed 'Congratulations' on a losing game");
            passed = false;
        }

        if (passed) System.out.println("Passed: Game flow correct for losing scenario (Passed)");
    }

    public static void testGameInvalidRobust() {
        System.out.println("Testing Game Flow (Invalid Input) - Robust:");

        // Scenario: Input "ABC" (invalid), then "APPLE" (valid)
        // Should NOT print "Guess 1: ABC". Should print "Guess 1: APPLE".
        String input = "ABC\nAPPLE\n";
        String output = captureGameOutput("APPLE", input);

        boolean passed = true;

        // Check 1: Handles invalid input message
        if (!output.contains("Invalid")) {
            System.out.println("Failed: Did not print 'Invalid' error message");
            passed = false;
        }

        // Check 2: Ensure invalid guess didn't take up a turn slot
        if (output.contains("Guess 1: ABC")) {
            System.out.println("Failed: Invalid guess 'ABC' was printed to the board");
            passed = false;
        }

        // Check 3: Ensure next valid guess became Guess 1
        if (!output.contains("Guess 1: APPLE")) {
            System.out.println("Failed: The valid guess after the invalid one should be 'Guess 1'");
            passed = false;
        }

        if (passed) System.out.println("Passed: Invalid input handling correct (Passed)");
    }

    // --- Helper: Captures System.out while running game ---
    
    private static String captureGameOutput(String secret, String inputData) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(baos);

        File original = new File("dictionary.txt");
        File backup = new File("dictionary_backup.tmp");
        boolean backedUp = false;

        try {
            // Setup Dictionary
            if (original.exists()) {
                if (original.renameTo(backup)) backedUp = true;
            }
            try (FileWriter writer = new FileWriter("dictionary.txt")) { writer.write(secret); }

            // Setup I/O
            System.setIn(new ByteArrayInputStream(inputData.getBytes()));
            System.setOut(newOut); // Redirect output

            // Run Game
            Wordle.main(new String[]{});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Restore everything
            System.setOut(originalOut);
            File temp = new File("dictionary.txt");
            if (temp.exists()) temp.delete();
            if (backedUp && backup.exists()) backup.renameTo(original);
        }
        
        return baos.toString();
    }

    // --- Unit Tests (Standard) ---

    public static void testReadDictionary() {
        System.out.println("Testing readDictionary:");
        try {
            String[] dict = Wordle.readDictionary("dictionary.txt");
            if (dict == null) { System.out.println("Failed: Dictionary is null"); return; }
            if (dict.length < 10) { System.out.println("Failed: Dictionary seems too small"); return; }
            for (String s : dict) {
                if ("APPLE".equals(s)) {
                    System.out.println("Passed: Dictionary read successfully (Passed)");
                    return;
                }
            }
            System.out.println("Failed: 'APPLE' not found in dictionary");
        } catch (Exception e) { System.out.println("Failed: " + e.getMessage()); }
    }

    public static void testChooseSecretWord() {
        System.out.println("Testing chooseSecretWord:");
        String[] mockDict = {"ONE", "TWO", "THREE"};
        String secret = Wordle.chooseSecretWord(mockDict);
        if (secret != null && (secret.equals("ONE") || secret.equals("TWO") || secret.equals("THREE"))) {
            System.out.println("Passed: Selected valid words from dictionary (Passed)");
        } else {
            System.out.println("Failed: returned invalid word");
        }
    }

    public static void testContainsChar() {
        System.out.println("Testing containsChar:");
        int passed = 0;
        if (Wordle.containsChar("HELLO", 'H')) passed++;
        if (Wordle.containsChar("HELLO", 'O')) passed++;
        if (!Wordle.containsChar("HELLO", 'A')) passed++;
        if (!Wordle.containsChar("WORLD", 'X')) passed++;
        System.out.println("Passed " + passed + "/4 tests" + (passed==4 ? " (Passed)" : ""));
    }

    public static void testComputeFeedback() {
        System.out.println("Testing computeFeedback:");
        int passed = 0;
        
        char[] res1 = new char[5];
        Wordle.computeFeedback("ABCDE", "ABCDE", res1);
        if (new String(res1).equals("GGGGG")) passed++;

        char[] res2 = new char[5];
        Wordle.computeFeedback("ABCDE", "VWXYZ", res2);
        if (new String(res2).replace('\0', '_').equals("_____")) passed++;

        char[] res3 = new char[5];
        Wordle.computeFeedback("APPLE", "PAPAL", res3);
        if (new String(res3).equals("YYGYY")) passed++;

        System.out.println("Passed " + passed + "/3 tests" + (passed==3 ? " (Passed)" : ""));
    }

    public static void testStoreGuess() {
        System.out.println("Testing storeGuess:");
        char[][] guesses = new char[6][5];
        Wordle.storeGuess("HELLO", guesses, 1);
        if (new String(guesses[1]).equals("HELLO")) System.out.println("Passed: Correctly stored guess in 2D array (Passed)");
        else System.out.println("Failed: Store guess incorrect");
    }

    public static void testIsAllGreen() {
        System.out.println("Testing isAllGreen:");
        char[] allG = {'G','G','G','G','G'};
        char[] mixed = {'G','Y','G','G','G'};
        if (Wordle.isAllGreen(allG) && !Wordle.isAllGreen(mixed)) System.out.println("Passed 2/2 tests (Passed)");
        else System.out.println("Failed isAllGreen tests");
    }

    // Keep the old testGameWin for backwards compatibility or simple checks if needed
    public static void testGameWin() {
        String input = "HELPS\nAPPLE\n";
        String output = captureGameOutput("APPLE", input);
        if (output.contains("Congratulations")) System.out.println("Congratulations");
    }
}