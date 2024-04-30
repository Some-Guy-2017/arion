package unit_test;

import arion.*;
import callback.*;

import java.util.Random;
import java.util.ArrayList;
import java.io.*;

public class ArionDisplayTests 
{
    static final int randomWordCount = 2;
    static Random rand = new Random();
    static ArrayList<String> nounList;
    static String nounFilepath = "./top-1000-nouns.txt";

    public static void main(String[] args)
    {
        parseNouns();
        
        //testConstructor();
        testDisplayMenuBar();
    }
    
    private static void testConstructor()
    {
        constructorTestCase("100x100 Resolution",    100,  100); // valid case
        constructorTestCase("-100x-100 Resolution", -100, -100); // invalid case
        constructorTestCase("0x0 Resolution",          0,    0); // valid case
        constructorTestCase("",             100,  100); // valid case
        constructorTestCase(null,           100,  100); // invalid case
    }

    private static void testDisplayMenuBar()
    {
        // valid test case
        String[] menuTitles = generateDebugMenus(3);
        String[][] actions = generateDebugActions(3, 2);
        Runnable[][] callbacks = generateDebugCallbacks(3, 2, actions);
        menuBarTestCase("3x2 Menus", menuTitles, actions, callbacks);

        // invalid test case (arrays are null)
        menuBarTestCase("Null Menus", null, null, null);

        // invalid test case (menus of size 0 will not render)
        menuTitles = generateDebugMenus(0);
        actions = generateDebugActions(0, 0);
        callbacks = generateDebugCallbacks(0, 0, actions);
        menuBarTestCase("Empty Menus", menuTitles, actions, callbacks);
        
        // invalid test case (incorrectly sized)
        menuTitles = generateDebugMenus(1);
        actions = generateDebugActions(0, 0);
        callbacks = generateDebugCallbacks(2, 0, actions);
        menuBarTestCase("Incorrectly Sized Menus", menuTitles, actions, callbacks);

    }

    private static String[] generateDebugMenus(int menuCount)
    {
        String[] menus = new String[menuCount];
        for (int i = 0; i < menuCount; i++) menus[i] = generateRandomString();
        return menus;
    }

    private static String[][] generateDebugActions(int menuCount, int actionCount)
    {
        String[][] actions = new String[menuCount][actionCount];
        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++)
        for (int actionIdx = 0; actionIdx < actionCount; actionIdx++)
        {
            actions[menuIdx][actionIdx] = generateRandomString();
        }

        return actions;
    }
    
    private static Runnable[][] generateDebugCallbacks(int menuCount, int actionCount, String[][] actions)
    {
        Runnable[][] callbacks = new Runnable[menuCount][actionCount];
        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++)
        for (int actionIdx = 0; actionIdx < actionCount; actionIdx++)
        {
            String action = actions[menuIdx][actionIdx];

            // declare them final for the anonymous class
            final int finalMenuIdx = menuIdx;
            final int finalActionIdx = actionIdx;
            
            callbacks[menuIdx][actionIdx] = () -> {
                System.out.println("Action " + action + " called. This is action " + finalMenuIdx + ":" + finalActionIdx + ".");
            };
        }

        return callbacks;

    }

    private static String generateRandomString()
    {
        String randomString = "";
        
        int lastIdx = -1;
        for (int i = 0; i < randomWordCount; i++)
        {
            int randomIdx;
            do {
                randomIdx = rand.nextInt(nounList.size());
            } while (randomIdx == lastIdx);

            String noun = nounList.get(randomIdx);
            noun = noun.substring(0, 1).toUpperCase() + noun.substring(1).toLowerCase();
            randomString += noun;
            lastIdx = randomIdx;
        }

        return randomString;
    }

    private static void constructorTestCase(String title, int width, int height)
    {
        ArionDisplay display = new ArionDisplay(title, width, height);
        display._setVisible();
    }

    private static void menuBarTestCase(String[] menuTitles, String[][] actions, Runnable[][] callbacks)
    {
        menuBarTestCase("displayMenuBar test", menuTitles, actions, callbacks);
    }
    
    private static void menuBarTestCase(String title, String[] menuTitles, String[][] actions, Runnable[][] callbacks)
    {
        ArionDisplay display = new ArionDisplay(title, 800, 600);
        display.displayMenuBar(menuTitles, actions, callbacks);
        display._setVisible();
    }

    private static void parseNouns()
    {
        nounList = new ArrayList<String>();
            
        BufferedReader nounReader;
        try {
            nounReader = new BufferedReader(new FileReader(nounFilepath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        
        String noun = "";
        while (true)
        {
            try {
                noun = nounReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            if (noun == null) break;
            nounList.add(noun);
        }
    }
}
