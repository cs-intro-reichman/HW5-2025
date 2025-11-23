import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Tester class for Wordle.java
 * Usage: java TesterWordle [testName]
 * Supported testNames: readDictionary, containsChar, computeFeedback, isAllGreen, testGameWin, testGameLose, testGameInvalid
 */
public class TesterWordle {

    public static void main(String[] args) {
        if (args.length == 0) {
            runAll();
        } else {
            switch (args[0]) {
                case "readDictionary": testReadDictionary(); break;
                case "containsChar": testContainsChar(); break;
                case "computeFeedback": testComputeFeedback(); break;
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
        testContainsChar();
        testComputeFeedback();
        testIsAllGreen();
        // Game tests are not run here because they close System.in and must be run separately
    }

    // --- Game Scenario Tests ---

    public static void testGameWin() {
        // Scenario: Secret is APPLE. User guesses HELPS then APPLE.
        // Expectation: Game prints "Congratulations"
        String input = "HELPS\nAPPLE\n";
        runGameWithInput("APPLE", input, "Win Scenario");
    }

    public static void testGameLose() {
        // Scenario: Secret is APPLE. User guesses WRONG 6 times.
        // Expectation: Game prints "The secret word was"
        String input = "WRONG\nWRONG\nWRONG\nWRONG\nWRONG\nWRONG\n";
        runGameWithInput("APPLE", input, "Lose Scenario");
    }

    public static void testGameInvalid() {
        // Scenario: Secret is APPLE. User enters invalid words first.
        // 1. "ABC" (Too short)
        // 2. "ABCDEF" (Too long)
        // 3. "APPLE" (Valid)
        // Expectation: Game prints "Invalid word" and eventually "Congratulations"
        String input = "ABC\nABCDEF\nAPPLE\n";
        runGameWithInput("APPLE", input, "Invalid Input Scenario");
    }

    /**
     * Helper to run the main game with a specific secret word and simulated input.
     */
    private static void runGameWithInput(String secret, String inputData, String testName) {
        System.out.println("Testing " + testName + ":");
        
        File original = new File("dictionary.txt");
        File backup = new File("dictionary_backup.tmp");
        boolean backedUp = false;

        try {
            // 1. Backup the original dictionary
            if (original.exists()) {
                if (original.renameTo(backup)) {
                    backedUp = true;
                } else {
                    System.out.println("Failed to backup dictionary. Skipping test.");
                    return;
                }
            }

            // 2. Create a temporary dictionary with the fixed secret word
            try (FileWriter writer = new FileWriter("dictionary.txt")) {
                writer.write(secret);
            }

            // 3. Inject the simulated input
            System.setIn(new ByteArrayInputStream(inputData.getBytes()));

            // 4. Run the student's main method
            Wordle.main(new String[]{});

        } catch (Exception e) {
            System.out.println("Game crashed during " + testName + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. Restore original dictionary
            File temp = new File("dictionary.txt");
            if (temp.exists()) {
                temp.delete();
            }
            if (backedUp && backup.exists()) {
                backup.renameTo(original);
            }
        }
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
}