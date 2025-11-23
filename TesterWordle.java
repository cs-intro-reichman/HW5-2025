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
                case "printBoard": testPrintBoard(); break;
                case "isAllGreen": testIsAllGreen(); break;
                
                // Robust Game Flow tests
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
        testPrintBoard();
        testIsAllGreen();
    }

    // --- Unit Tests ---

    public static void testPrintBoard() {
        System.out.println("Testing printBoard:");
        
        char[][] guesses = new char[6][5];
        char[][] results = new char[6][5];
        
        // Setup dummy data for Row 0
        Wordle.storeGuess("ABCDE", guesses, 0);
        results[0] = new char[]{'G', '_', 'Y', '_', 'G'};
        
        // Capture output
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        try {
            Wordle.printBoard(guesses, results, 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.setOut(originalOut);
        }
        
        String output = baos.toString();
        
        // Check for key components
        boolean hasGuess = output.contains("Guess 1: ABCDE");
        boolean hasResult = output.contains("Result: G_Y_G"); // Checks simplified format
        
        if (hasGuess && hasResult) {
            System.out.println("Passed: Correctly printed board format (Passed)");
        } else {
            System.out.println("Failed: Output format incorrect.");
            if (!hasGuess) System.out.println(" - Missing 'Guess 1: ABCDE'");
            if (!hasResult) System.out.println(" - Missing 'Result: G_Y_G'");
            System.out.println("Actual Output:\n" + output + "\n[End of Output]");
        }
    }

    public static void testStoreGuess() {
        System.out.println("Testing storeGuess:");
        char[][] guesses = new char[6][5];
        try {
            Wordle.storeGuess("HELLO", guesses, 1);
            String stored = new String(guesses[1]);
            
            if (stored.equals("HELLO")) {
                System.out.println("Passed: Correctly stored guess in 2D array (Passed)");
            } else {
                // Determine what exactly is wrong
                String safeStored = stored.replace('\0', '*'); // visualize nulls
                System.out.println("Failed: Expected guesses[1] to be \"HELLO\".");
                System.out.println("Actual guesses[1]: \"" + safeStored + "\"");
            }
        } catch (Exception e) {
            System.out.println("Failed: Exception during storeGuess: " + e.getMessage());
        }
    }

    public static void testReadDictionary() {
        System.out.println("Testing readDictionary:");
        try {
            String[] dict = Wordle.readDictionary("dictionary.txt");
            if (dict == null) { System.out.println("Failed: Returned null dictionary array"); return; }
            if (dict.length < 10) { System.out.println("Failed: Dictionary seems too small (length=" + dict.length + ")"); return; }
            
            boolean found = false;
            for (String s : dict) {
                if ("APPLE".equals(s)) { found = true; break; }
            }
            
            if (found) System.out.println("Passed: Dictionary read successfully (Passed)");
            else System.out.println("Failed: 'APPLE' not found in dictionary");
            
        } catch (Exception e) { System.out.println("Failed: Exception: " + e.getMessage()); }
    }

    public static void testChooseSecretWord() {
        System.out.println("Testing chooseSecretWord:");
        String[] mockDict = {"ONE", "TWO", "THREE"};
        try {
            String secret = Wordle.chooseSecretWord(mockDict);
            if (secret != null && (secret.equals("ONE") || secret.equals("TWO") || secret.equals("THREE"))) {
                System.out.println("Passed: Selected valid words from dictionary (Passed)");
            } else {
                System.out.println("Failed: Returned word '" + secret + "' which was not in the provided dictionary.");
            }
        } catch (Exception e) { System.out.println("Failed: Exception: " + e.getMessage()); }
    }

    public static void testContainsChar() {
        System.out.println("Testing containsChar:");
        boolean failed = false;
        
        if (!Wordle.containsChar("HELLO", 'H')) { System.out.println("Failed: containsChar(\"HELLO\", 'H') returned false."); failed = true; }
        if (!Wordle.containsChar("HELLO", 'O')) { System.out.println("Failed: containsChar(\"HELLO\", 'O') returned false."); failed = true; }
        if (Wordle.containsChar("HELLO", 'A')) { System.out.println("Failed: containsChar(\"HELLO\", 'A') returned true."); failed = true; }
        if (Wordle.containsChar("WORLD", 'X')) { System.out.println("Failed: containsChar(\"WORLD\", 'X') returned true."); failed = true; }
        
        if (!failed) System.out.println("Passed 4/4 tests (Passed)");
    }

    public static void testComputeFeedback() {
        System.out.println("Testing computeFeedback:");
        boolean failed = false;
        
        // Test 1: Exact Match
        char[] res1 = new char[5];
        Wordle.computeFeedback("ABCDE", "ABCDE", res1);
        String s1 = new String(res1);
        if (!s1.equals("GGGGG")) {
            System.out.println("Failed (Exact Match): Secret='ABCDE', Guess='ABCDE'.");
            System.out.println("Expected: GGGGG");
            System.out.println("Actual:   " + s1.replace('\0', '_'));
            failed = true;
        }

        // Test 2: No Match
        char[] res2 = new char[5];
        Wordle.computeFeedback("ABCDE", "VWXYZ", res2);
        String s2 = new String(res2).replace('\0', '_'); // normalize nulls to underscore for display
        if (!s2.equals("_____")) {
            System.out.println("Failed (No Match): Secret='ABCDE', Guess='VWXYZ'.");
            System.out.println("Expected: _____");
            System.out.println("Actual:   " + s2);
            failed = true;
        }

        // Test 3: Partial/Duplicate Logic
        char[] res3 = new char[5];
        Wordle.computeFeedback("APPLE", "PAPAL", res3);
        String s3 = new String(res3);
        if (!s3.equals("YYGYY")) {
            System.out.println("Failed (Complex Logic): Secret='APPLE', Guess='PAPAL'.");
            System.out.println("Expected: YYGYY");
            System.out.println("Actual:   " + s3.replace('\0', '_'));
            failed = true;
        }

        if (!failed) System.out.println("Passed 3/3 tests (Passed)");
    }

    public static void testIsAllGreen() {
        System.out.println("Testing isAllGreen:");
        char[] allG = {'G','G','G','G','G'};
        char[] mixed = {'G','Y','G','G','G'};
        boolean failed = false;
        
        if (!Wordle.isAllGreen(allG)) {
            System.out.println("Failed: isAllGreen returned false for 'GGGGG'");
            failed = true;
        }
        if (Wordle.isAllGreen(mixed)) {
            System.out.println("Failed: isAllGreen returned true for 'GYGGG'");
            failed = true;
        }
        
        if (!failed) System.out.println("Passed 2/2 tests (Passed)");
    }

    // --- Robust Game Tests ---

    public static void testGameWinRobust() {
        System.out.println("Testing Game Flow (Win) - Robust:");
        String input = "HELPS\nAPPLE\n";
        String output = captureGameOutput("APPLE", input);

        boolean passed = true;
        // Check 1
        if (!output.contains("Guess 1: HELPS")) { 
            System.out.println("Failed: Output missing 'Guess 1: HELPS'. Did the game loop start?");
            passed = false; 
        }
        // Check 2
        if (!output.contains("_YYY_")) { 
            System.out.println("Failed: Feedback for 'HELPS' incorrect. Expected '_YYY_' (or visual equivalent)."); 
            passed = false; 
        }
        // Check 3
        if (!output.contains("Guess 2: APPLE")) { 
            System.out.println("Failed: Output missing 'Guess 2: APPLE'. Did the game accept the second guess?");
            passed = false; 
        }
        // Check 4
        if (!output.contains("Congratulations")) { 
            System.out.println("Failed: Output missing 'Congratulations'. Did the game detect the win?");
            passed = false; 
        }

        if (passed) System.out.println("Passed: Game flow correct for winning scenario (Passed)");
        else System.out.println("DEBUG: Full output was:\n" + output + "\n[End Output]");
    }

    public static void testGameLoseRobust() {
        System.out.println("Testing Game Flow (Lose) - Robust:");
        String input = "ZZZZZ\nZZZZZ\nZZZZZ\nZZZZZ\nZZZZZ\nZZZZZ\n";
        String output = captureGameOutput("APPLE", input);

        boolean passed = true;
        if (!output.contains("Guess 6:")) { 
            System.out.println("Failed: Output missing 'Guess 6'. Did loop run 6 times?"); 
            passed = false; 
        }
        if (!output.contains("secret word was")) { 
            System.out.println("Failed: Did not reveal secret word after losing."); 
            passed = false; 
        }
        if (output.contains("Congratulations")) { 
            System.out.println("Failed: Printed 'Congratulations' on a losing game."); 
            passed = false; 
        }

        if (passed) System.out.println("Passed: Game flow correct for losing scenario (Passed)");
    }

    public static void testGameInvalidRobust() {
        System.out.println("Testing Game Flow (Invalid Input) - Robust:");
        String input = "ABC\nAPPLE\n";
        String output = captureGameOutput("APPLE", input);

        boolean passed = true;
        if (!output.contains("Invalid")) { 
            System.out.println("Failed: Did not print 'Invalid' error message for 'ABC'"); 
            passed = false; 
        }
        if (output.contains("Guess 1: ABC")) { 
            System.out.println("Failed: 'ABC' was printed as a valid guess."); 
            passed = false; 
        }
        if (!output.contains("Guess 1: APPLE")) { 
            System.out.println("Failed: The valid guess 'APPLE' should be 'Guess 1', but it wasn't found or wasn't labelled 'Guess 1'."); 
            passed = false; 
        }

        if (passed) System.out.println("Passed: Invalid input handling correct (Passed)");
    }
    
    // --- Helper ---
    private static String captureGameOutput(String secret, String inputData) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(baos);
        File original = new File("dictionary.txt");
        File backup = new File("dictionary_backup.tmp");
        boolean backedUp = false;
        try {
            if (original.exists()) { if (original.renameTo(backup)) backedUp = true; }
            try (FileWriter writer = new FileWriter("dictionary.txt")) { writer.write(secret); }
            System.setIn(new ByteArrayInputStream(inputData.getBytes()));
            System.setOut(newOut);
            Wordle.main(new String[]{});
        } catch (Exception e) { e.printStackTrace(); } 
        finally {
            System.setOut(originalOut);
            File temp = new File("dictionary.txt");
            if (temp.exists()) temp.delete();
            if (backedUp && backup.exists()) backup.renameTo(original);
        }
        return baos.toString();
    }
    
    // Legacy test
    public static void testGameWin() { testGameWinRobust(); }
}