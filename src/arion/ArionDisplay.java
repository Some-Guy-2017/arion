// Written by - Joseph Hare
// Thursday April 25 2024

/*
 * ArionDisplay contains all the functionality for the Arion GUI.
 */

package arion;

import arion.Flashcard;
import callback.*;
import exception.GuideDisplayException;
import exception.MainPanelDisplayException;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicIconFactory;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.text.Format;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class ArionDisplay {
    JFrame frame;

    private Runnable addCallback;
    private Runnable studyCallback;
    private EditCallback editCallback;
    private DeleteCallback deleteCallback;
    private ArrayList<Flashcard> browseFlashcards;

    private final static String[] IMAGE_DIRECTORIES = { "./", "./img/" };
    private final static String GUIDE_FILEPATH = "./guide.xml";
    private final static String ABOUT_FILEPATH = "./about.txt";
    private final static String BACK_BUTTON_FILE = "back-button.png";
    final static Dimension MAX_DIMENSION = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    private static Optional<Node[]> guidePages = parseGuidePages(GUIDE_FILEPATH);

    static class Style {
        public int fontStyle;
        public float fontSize;
        public int padding;

        public Style(int fontStyle, float fontSize, int padding) {
            this.fontStyle = fontStyle;
            this.fontSize = fontSize;
            this.padding = padding;
        }
    }

    static class Format {
        final static Style H1 = new Style(Font.BOLD, 25f, 20);
        final static Style H2 = new Style(Font.BOLD, 16f, 15);
        final static Style COMPONENT = new Style(Font.PLAIN, 14f, 10);
        final static Style STUDY_TEXT = new Style(Font.PLAIN, 18f, 10);
        final static Style IMAGE = new Style(Font.PLAIN, 0f, 10);
        final static Style POPUP = new Style(Font.PLAIN, 17f, 10);
        final static Style NESTED_PANEL = new Style(Font.PLAIN, 0f, 10);

        final static int MARGIN_SIZE = 20;
        final static int TEXT_BOX_WIDTH = 50;
        final static int TEXT_AREA_ROWS = 20;
        final static int TABLE_CELL_PADDING = 2;
        final static Dimension BUTTON_SIZE = new Dimension(300, 40);
        final static double POPUP_WINDOW_RATIO = 1.5;
        final static Dimension POPUP_BUTTON_SIZE = new Dimension(125, 25);
        final static int IMAGE_BORDER_THICKNESS = 1;
        final static int BACK_BUTTON_WIDTH = 20;
    }

    public class _TestHooks {
        ArionDisplay parent;

        public _TestHooks(ArionDisplay parent) {
            this.parent = parent;
        }
        
        public static void setGuideFilepath(String guideFilepath) {
            guidePages = parseGuidePages(guideFilepath);
        }

        public static String getGuideFilepath() {
            return GUIDE_FILEPATH;
        }

        public void displayGuidePageRoutine(int pageNum) throws GuideDisplayException {
            parent.displayGuidePageRoutine(pageNum);
        }
    }

    public _TestHooks _testHooks = new _TestHooks(this);

    /*
     * The ArionDisplay constructor initializes the JFrame containing the GUI.
     * This includes validating the title, width, and height.
     *
     * Input: window title, width, and height.
     * Output: ArionDisplay class.
     */
    public ArionDisplay(String title, int width, int height) {

        if (width < 0 || height < 0) {
            String message = "Cannot construct JFrame with invalid width and height values." + "\n"
                    + "Setting width and height values to 0.";
            warningAlert(message);
        }

        if (title == null) {
            warningAlert("Title not provided; setting title to empty string.");
            title = "";
        }

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /*
     * displayMenuBar adds a menu bar to the window, given the titles, action names, and callbacks of the menu options.
     *
     * Input: menu titles, action names, and callbacks.
     * Output: no return value, adds menu bar to window.
     */
    public void displayMenuBar(String[] menuTitles, String[][] actions, Runnable[][] callbacks) {

        if (menuTitles == null || actions == null || callbacks == null)
            throw new NullPointerException("Cannot construct menu because received a null parameter.");
        if (menuTitles.length != actions.length || menuTitles.length != callbacks.length)
            throw new IllegalArgumentException(
                    "Cannot construct menu because menu titles, actions, and callbacks are not equally sized.");

        int menuCount = menuTitles.length;
        if (menuCount == 0) {
            System.out.println("WARNING: Menu is empty, so it will not render.");
        }

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++) {
            String menuActions[] = actions[menuIdx];
            Runnable[] menuCallbacks = callbacks[menuIdx];

            if (menuActions.length != menuCallbacks.length)
                throw new IllegalArgumentException(
                        "Cannot construct menu because menu actions and callbacks are different lengths at index "
                                + menuIdx + ".");

            JMenu menu = new JMenu(menuTitles[menuIdx]);
            menuBar.add(menu);

            int actionCount = menuActions.length;
            for (int actionIdx = 0; actionIdx < actionCount; actionIdx++) {
                String action = menuActions[actionIdx];
                JMenuItem menuItem = new JMenuItem(action);
                menu.add(menuItem);

                Runnable callback = menuCallbacks[actionIdx];
                menuItem.addActionListener(generateActionListener(callback));
            }
        }

    }

    /*
     * displayMainScreen displays the main screen.
     * The main screen contains a Study button, which calls a passed callback, and an Add button, which calls another callback.
     *
     * Input: Add button and Study button callbacks.
     * Output: no return value, displays main screen.
     */
    public void displayMainScreen(Runnable addCallback, Runnable studyCallback) {
        if (addCallback == null || studyCallback == null)
            throw new NullPointerException("Could not display main screen because passed callbacks are null.");
        this.addCallback = addCallback;
        this.studyCallback = studyCallback;

        JPanel panel;
        try {
            panel = generateMainPanel("Main Screen", false);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }

        JButton studyButton = generateButton("Study");
        studyButton.addActionListener(generateActionListener(studyCallback));
        addPanelComponent(panel, studyButton, Format.COMPONENT, true);

        JButton addButton = generateButton("Add");
        addButton.addActionListener(generateActionListener(addCallback));
        addPanelComponent(panel, addButton, Format.COMPONENT, false);

        setFrameContent(panel);
    }

    /*
     * displayBrowseScreen set flashcards, edit callback, and delete callback for the edit screen.
     * It does not directly render the browse so that other internal methods can render the previous browse screen,
     * without passing flashcards or callbacks.
     * The description of the browse screen is described above the renderBrowseScreen method.
     *
     * Input: list of flashcards to display in the browse screen, and an edit and delete callback for the browse screen.
     * Output: no return value, displays the browse screen.
     */
    public void displayBrowseScreen(ArrayList<Flashcard> flashcards, EditCallback editCallback,
            DeleteCallback deleteCallback) {
        if (flashcards == null || editCallback == null || deleteCallback == null)
            throw new NullPointerException("Could not display browse screen because passed callbacks are null.");

        this.browseFlashcards = flashcards;
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
        renderBrowseScreen();
    }

    /*
     * displayAddScreen displays the add screen.
     * The add screen consists of a small text field for the user to input the front of the flashcard,
     * and a large text area for the user to input the back of the card.
     * It also has an "Add Flashcard" button for the user to confirm their inputs and add a new flashcard
     * with the provided fields to the deck.
     * This functionality is provided through a callback passed to the method.
     *
     * Input: "Add Flashcard" button callback.
     * Output: no return value, displays add screen.
     */
    public void displayAddScreen(AddCallback callback) {
        if (callback == null)
            throw new NullPointerException("Could not display add screen because passed callback is null.");

        JPanel panel;
        try {
            panel = generateMainPanel("Add", true);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }

        JLabel frontLabel = new JLabel("Front");
        addPanelComponent(panel, frontLabel, Format.H2, true);

        JTextField frontTextField = new JTextField(Format.TEXT_BOX_WIDTH);
        addPanelComponent(panel, frontTextField, Format.COMPONENT, true);

        JLabel backLabel = new JLabel("Back");
        addPanelComponent(panel, backLabel, Format.H2, true);

        JTextArea backTextArea = generateTextArea();
        backTextArea.setRows(Format.TEXT_AREA_ROWS);

        JScrollPane scrollPane = scrollWrap(backTextArea);
        setPreferredWidth(scrollPane, frontTextField.getPreferredSize().width);

        addPanelComponent(panel, scrollPane, Format.COMPONENT, true);

        JButton addButton = generateButton("Add Flashcard");
        addButton.addActionListener(generateActionListener(() -> {
            String[] fields = { frontTextField.getText(), backTextArea.getText() };
            callback.run(fields);
            reenterMainScreen();
        }));
        addPanelComponent(panel, addButton, Format.COMPONENT, false);

        setFrameContent(panel);
    }

    /*
     * displayStudyScreen displays the study screen for a flashcard.
     * The study screen displays the front or back of the flashcard, as is needed by the Study functionality.
     * It consists of the flashcard front or back, along with a "Flip" button for the front,
     * or a "Correct" button and an "Incorrect" button for the back.
     * Initially, the method is called with front = true, so the front of the flashcard is displayed.
     * After the user clicks the "Flip" button, this method is called by itself with front = false so the back is shown.
     * When a review button is clicked, the review callback is called to update the flashcard.
     * This callback is passed whether the user clicked "Correct" or "Incorrect" to correctly update the flashcard.
     * If there are more due flashcards, the callback will call this method again; if not, it will exit the loop.
     *
     * Input: flashcard to study, whether to show the front, and a callback to be run when the flashcard has been reviewed.
     * Output: no return value, displays the front or back of the flashcard in a study screen.
     */
    public void displayStudyScreen(Flashcard flashcard, boolean front, ReviewCallback reviewCallback) {
        if (flashcard == null || reviewCallback == null) {
            throw new NullPointerException("Could not display study screen because passed parameter is null.");
        }

        JPanel panel;
        try {
            panel = generateMainPanel("Study", true);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }

        String text;
        if (front) {
            text = flashcard.front;
        }
        else {
            text = flashcard.back;
        }

        JLabel textLabel = new JLabel(text);
        addPanelComponent(panel, textLabel, Format.STUDY_TEXT, true);

        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.add(Box.createGlue());

        Runnable flipCard = () -> displayStudyScreen(flashcard, false, reviewCallback);
        Runnable correctReview = () -> reviewCallback.run(true);
        Runnable incorrectReview = () -> reviewCallback.run(false);

        panel.getActionMap().put("flipCard", new ExecuteRunnable(flipCard));
        panel.getActionMap().put("correctReview", new ExecuteRunnable(correctReview));
        panel.getActionMap().put("incorrectReview", new ExecuteRunnable(incorrectReview));
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (front) {
            JButton flipButton = generateButton("Flip");
            addPanelComponent(bar, flipButton, Format.COMPONENT, false);

            flipButton.addActionListener(generateActionListener(flipCard));
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "flipCard");

        } else {
            JButton correctButton = generateButton("Correct");
            addPanelComponent(bar, correctButton, Format.COMPONENT, true);
            correctButton.addActionListener(generateActionListener(correctReview));
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "correctReview");

            JButton incorrectButton = generateButton("Incorrect");
            addPanelComponent(bar, incorrectButton, Format.COMPONENT, false);
            incorrectButton.addActionListener(generateActionListener(incorrectReview));
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "incorrectReview");
        }

        bar.add(Box.createGlue());

        panel.add(Box.createVerticalGlue());
        addPanelComponent(panel, bar, Format.NESTED_PANEL, false);

        setFrameContent(panel);
    }

    /*
     * displaySortScreen displays the sort screen.
     * The sort screen consists of two drop-down boxes, allowing the user to select the field to sort by and the direction to sort by,
     * and a "Sort" button to confirm their selection.
     * When the "Sort" button is clicked, a passed callback is run with the provided configuration by the user,
     * which is the logic that ultimately sorts the flashcards.
     *
     * Input: callback to sort the flashcards.
     * Output: no return value, displays the sort screen.
     */
    public void displaySortScreen(SortCallback sortCallback) {
        if (sortCallback == null)
            throw new NullPointerException("Cannot display sort screen with null callback.");

        JPanel panel;
        try {
            panel = generateMainPanel("Sort", true);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }

        JComboBox fieldSelector = new JComboBox<Flashcard.Field>(Flashcard.FIELDS);

        String[] directions = { "Forwards", "Backwards" };
        JComboBox directionSelector = new JComboBox<String>(directions);

        JComboBox[] comboBoxes = {
                fieldSelector,
                directionSelector,
        };

        int maxWidth = -1;
        for (JComboBox comboBox : comboBoxes) {
            int width = comboBox.getPreferredSize().width;
            if (width > maxWidth)
                maxWidth = width;
        }

        for (JComboBox comboBox : comboBoxes) {
            int height = comboBox.getPreferredSize().height;
            comboBox.setPreferredSize(new Dimension(maxWidth, height));
            addPanelComponent(panel, comboBox, Format.COMPONENT, true);
        }

        JButton confirmButton = generateButton("Sort");
        confirmButton.addActionListener(generateActionListener(() -> {
            Flashcard.Field field = (Flashcard.Field) fieldSelector.getSelectedItem();
            boolean reversed = directionSelector.getSelectedIndex() == 1; // reversing direction is index 1
            sortCallback.run(field, reversed);
            reenterMainScreen();
        }));
        addPanelComponent(panel, confirmButton, Format.COMPONENT, false);

        setFrameContent(panel);
    }

    /*
     * displayGuidePage displays a given guide page.
     * The guide pages are defined in GUIDE_FILEPATH using xml.
     * They consist of varying content, but always have a button bar on the bottom with Previous, Close, and Next buttons.
     * Previous goes to the previous page, Close closes the guide, and Next advances to the next page.
     *
     * Input: the number of the page to display.
     * Output: no return value, displays the requested guide page.
     */
    public void displayGuidePage(int pageNum) {
        try {
            displayGuidePageRoutine(pageNum);
        } catch (GuideDisplayException e) {
            Arion.displayException(e);
        }
    }

    /*
     * displayAboutScreen displays the about screen.
     * The About screen gives an overview of the design and philosophy behind Arion.
     * The text is defined in ABOUT_FILEPATH.
     *
     * Input: no input.
     * Output: no return value, displays the about screen.
     */
    public void displayAboutScreen() {
        JPanel panel;
        try {
            panel = generateMainPanel("About", false);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }
        panel.setPreferredSize(getPopupSize());
        MutablePopup popup = new MutablePopup(frame, panel);

        String aboutContent;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(ABOUT_FILEPATH));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            aboutContent = sb.toString()
                    .replaceAll("\n *(?!\n)", " ") // remove single new lines
                    .replaceAll("\n *", "\n    ") // remove spaces in front of newlines and add leading tab
                    .replaceAll("^ *", "    ") // remove leading spaces and add a tab to the start of paragraphs
                    .replaceAll(" *$", ""); // remove trailing spaces
        } catch (FileNotFoundException e) {
            warningAlert("Cannot display About window because could not find " + ABOUT_FILEPATH);
            return;
        } catch (IOException e) {
            Arion.displayException(e);
            return;
        }

        JTextArea textArea = generateTextArea(aboutContent);
        textArea.setEditable(false);
        textArea.setBackground(panel.getBackground());
        setFont(textArea, Format.POPUP);

        JScrollPane scrollPane = scrollWrap(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        addPanelComponent(panel, scrollPane, MAX_DIMENSION, Format.POPUP, true);

        JButton closeButton = generatePopupButton("Close");
        closeButton.addActionListener(generateActionListener(() -> {
            popup.hide();
        }));
        addPanelComponent(panel, closeButton, Format.COMPONENT, false);

        popup.show();
    }

    /*
     * alert statically displays a message in a dialog box; this is useful if ArionDisplay may not be initialized.
     *
     * Input: message to display.
     * Output: no return value, displays the message in a dialog box.
     */
    public static void alert(String message) {
        if (message == null) {
            throw new NullPointerException("Could not alert because message is null.");
        }
        JOptionPane.showMessageDialog(null, message);
    }

    /*
     * warningAlert works just like alert, but the message is intended as a warning.
     *
     * Input: message to display.
     * Output: no return value, displays the message in a dialog box.
     */
    public static void warningAlert(String message) {
        alert("Warning: " + message);
    }

    /*
     * displayConfirmationWindow displays a confirmation window with the provided message and title.
     *
     * Input: message and title of the confirmation window.
     * Output: boolean, whether the user confirmed.
     */
    public boolean displayConfirmationWindow(String message, String title) {
        if (message == null || title == null)
            throw new NullPointerException("Cannot display confirmation window with null parameters.");

        return JOptionPane.showConfirmDialog(frame, message, title,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /*
     * quit cleans up the class, removing the JFrame.
     *
     * Input: no input.
     * Output: no output.
     */
    public void quit() {
        frame.setVisible(false);
        frame.dispose();
    }

    /*
     * displaySuccessScreen displays a success screen, congratulating the user on successfully studying all their flashcards.
     *
     * Input: no input.
     * Output: no return value, displays the success screen.
     */
    public void displaySuccessScreen() {
        JPanel panel;
        try {
            panel = generateMainPanel("Congratulations!", false);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }

        JLabel text = new JLabel("All due flashcards have been studied.");
        addPanelComponent(panel, text, Format.COMPONENT, true);

        JButton returnButton = generateButton("Return to Main Screen");
        returnButton.addActionListener(generateActionListener(() -> reenterMainScreen()));
        addPanelComponent(panel, returnButton, Format.COMPONENT, false);

        setFrameContent(panel);
    }

    /*
     * generateMainPanel generates the main panel used by all screens.
     * It formats the panel and adds a header using the given screen name.
     *
     * Input: name of the screen, and a boolean whether to add a back button to return to the main screen.
     * Output: a JPanel for the main screen.
     */
    private JPanel generateMainPanel(String screenName, boolean addBackButton) throws MainPanelDisplayException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel topBar = new JPanel();
        topBar.setLayout(new OverlayLayout(topBar));

        JLabel header = new JLabel(screenName);
        addPanelComponent(topBar, header, Format.H1, false);

        if (addBackButton) {
            Optional<ImageIcon> iconOption = readImageFile(BACK_BUTTON_FILE, Format.BACK_BUTTON_WIDTH);
            ImageIcon icon = iconOption.orElseThrow(
                    () -> new MainPanelDisplayException("Could not read back button image."));

            Color c = new Color(238, 238, 238);
            JButton backButton = new JButton(icon);
            backButton.addActionListener(generateActionListener(() -> reenterMainScreen()));

            JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            addPanelComponent(buttonWrapper, backButton, Format.COMPONENT, false);
            addPanelComponent(topBar, buttonWrapper, MAX_DIMENSION, Format.NESTED_PANEL, false);
        }

        Dimension maxSize = topBar.getPreferredSize();
        maxSize.width = Integer.MAX_VALUE;
        addPanelComponent(panel, topBar, maxSize, Format.NESTED_PANEL, true);
        return panel;
    }

    /*
     * makeDisplayReady makes a main panel ready to be displayed to the user.
     * It sets the border to be empty with a with of MARGIN_SIZE to add a margin between the padding and external JFrame.
     * It is factored out of setFrameContent for when the content is not eventually displayed in the JFrame,
     * such as when it is displayed in a pop-up.
     * 
     * Input: panel to make display ready.
     * Output: no return value, modifies the input panel.
     */
    static void makeDisplayReady(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(
                Format.MARGIN_SIZE,
                Format.MARGIN_SIZE,
                Format.MARGIN_SIZE,
                Format.MARGIN_SIZE));
    }

    /*
     * setFrameContent displays a panel to the user in the JFrame.
     *
     * Input: panel to display.
     * Output: no return value, displays the given panel.
     */
    private void setFrameContent(JPanel panel) {
        makeDisplayReady(panel);

        Container contentPane = frame.getContentPane();
        contentPane.removeAll();
        contentPane.add(panel);

        frame.validate();
    }

    /*
     * generateTable generates a JTable that can be displayed to the user.
     *
     * Input: names of columns and table data.
     * Output: formatted table.
     */
    private JTable generateTable(String[] columnNames, String[][] data) {
        JTable table = new JTable(data, columnNames);
        setFont(table.getTableHeader(), Format.H2); // set table header font
        setFont(table, Format.COMPONENT); // set table content font

        // set row height to match font size and text padding
        table.setRowHeight((int) Format.COMPONENT.fontSize + Format.TABLE_CELL_PADDING * 2);

        return table;
    }

    /*
     * This is a wrapper method hiding the use of Optionals in the generate text area implementation
     * to only have functionality in one method.
     * This method signature takes no text, returning a formatted text area that has no default text.
     *
     * Input: no input.
     * Output: formatted text area with no text.
     */
    private JTextArea generateTextArea() {
        return _generateTextArea(Optional.empty());
    }

    /*
     * This is a wrapper method hiding the use of Optionals in the generate text area implementation
     * to only have functionality in one method.
     * This method signature takes default text, returning a formatted text area that with default text.
     *
     * Input: text in the text area.
     * Output: formatted text area with text.
     */
    private static JTextArea generateTextArea(String text) {
        return _generateTextArea(Optional.of(text));
    }

    /*
     * generateTextArea generates a formatted text area that can be displayed to the user.
     * It can optionally have text passed to it, depending on the method signature used.
     *
     * Input: Optional text to put in the text area.
     * Output: formatted text area.
     */
    private static JTextArea _generateTextArea(Optional<String> text) {
        JTextArea textArea;
        if (text.isPresent()) textArea = new JTextArea(text.get());
        else textArea = new JTextArea();

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        setFont(textArea, Format.COMPONENT);

        return textArea;
    }

    /*
     * scrollWrap wraps the passed component in a scroll pane.
     *
     * Input: component to wrap.
     * Output: scroll pane wrapping the component.
     */
    private static JScrollPane scrollWrap(Component component) {
        return new JScrollPane(
                component,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /*
     * generateButton generates a formatted button.
     *
     * Input: text in the button.
     * Output: formatted button.
     */
    private JButton generateButton(String text) {
        return generateSizedButton(text, Format.BUTTON_SIZE);
    }

    /*
     * generatePopupButton generates a button for display in a pop-up.
     * The difference between this method and generateButton is that the 
     * buttons for the pop-up window have a different size.
     *
     * Input: text in the pop-up button.
     * Output: formatted button.
     */
    private JButton generatePopupButton(String text) {
        return generateSizedButton(text, Format.POPUP_BUTTON_SIZE);
    }

    /*
     * generateSizedButton generates a button of the provided size.
     * 
     * Input: button text and button size.
     * Output: formatted button.
     */
    private JButton generateSizedButton(String text, Dimension size) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        return button;
    }

    /*
     * deriveFont replicates the component's font with the font style and size
     * defined in the Style class.
     *
     * Input: component with the base font, and font styling.
     * Output: component font with the Style's font style and size.
     */
    private static Font deriveFont(JComponent component, Style style) {
        return component.getFont().deriveFont(style.fontStyle, style.fontSize);
    }

    /*
     * setFont sets the component's font size and style to the size and style
     * defined in the Style class.
     *
     * Input: component with font to overwrite, and style defining the font size and style.
     * Output: no return value, writes to the component's font.
     */
    private static void setFont(JComponent component, Style style) {
        component.setFont(deriveFont(component, style));
    }

    /*
     * This method is a wrapper for _addPanelComponent that hides the use of an Optional to
     * optionally accept a maximum size.
     * This method signature does not accept a maximum size, so passes an empty Optional.
     *
     * Input: panel to add the component, component to add, component style,
     * and whether to append padding after the component.
     * Output: no return value, adds the component to the panel.
     */
    static void addPanelComponent(JPanel panel, JComponent component, Style style, boolean addPadding) {
        _addPanelComponent(panel, component, Optional.empty(), style, addPadding);
    }
    
    /*
     * This method is a wrapper for _addPanelComponent that hides the use of an Optional to
     * optionally accept a maximum size.
     * This method signature accepts a maximum size, which it passes through an Optional.
     *
     * Input: panel to add the component, component to add, maximum size of the component,
     * component style, and whether to append padding after the component.
     * Output: no return value, adds the component to the panel.
     */
    static void addPanelComponent(JPanel panel, JComponent component, Dimension maxSize, Style style, boolean addPadding) {
        _addPanelComponent(panel, component, Optional.of(maxSize), style, addPadding);
    }

    /*
     * addPanelComponent formats a JComponent, then adds it to the panel.
     * This method is not supposed to run; instead, two other method signatures can be run, which
     * each pass the Optional max size.
     * If there is no max size, the max size is the preferred size of the component AFTER setting its font.
     *
     * Input: panel to add the component, component to add, component style,
     * optionally the maximum size of the component, and whether to append padding after the component.
     * Output: no return value, adds the component to the panel.
     */
    private static void _addPanelComponent(JPanel panel, JComponent component, Optional<Dimension> maxSize,
            Style style, boolean addPadding) {

        component.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        component.setAlignmentY(JComponent.TOP_ALIGNMENT);
        setFont(component, style);

        component.setMaximumSize(maxSize.orElse(component.getPreferredSize()));
        panel.add(component);
        if (addPadding) {
            panel.add(Box.createRigidArea(new Dimension(style.padding, style.padding)));
        }
    }

    /*
     * renderBrowseScreen renders the browse screen.
     * The browse screen consists of an editable table listing the flashcards in memory,
     * where users can click and edit the fields of their flashcards.
     * There is an "Update Flashcards" button to confirm the edited fields,
     * and a "Delete Flashcard" button to delete any selected flashcards.
     *
     * Input: no inputs.
     * Output: no return value, displays the browse screen.
     */
    private void renderBrowseScreen() {
        JPanel panel;
        try {
            panel = generateMainPanel("Browse", true);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }

        String[] columnNames = Flashcard.FIELD_TITLES;
        String[][] tableData = new String[browseFlashcards.size()][];
        for (int i = 0; i < browseFlashcards.size(); i++)
            tableData[i] = browseFlashcards.get(i).toStringArray();

        JTable table = generateTable(columnNames, tableData);

        JScrollPane scroll = scrollWrap(table);
        Dimension size = scroll.getPreferredSize();
        size.width = 875;
        scroll.setPreferredSize(size);
        addPanelComponent(panel, scroll, Format.COMPONENT, true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // track the previous data to only update changed browseFlashcards
        String[][] prevData = new String[tableData.length][];
        for (int i = 0; i < tableData.length; i++)
            prevData[i] = tableData[i].clone();

        JButton updateButton = generateButton("Update Flashcards");
        updateButton.addActionListener(generateActionListener(() -> {
            sendUpdatedFlashcards(tableData, prevData, editCallback);
            renderBrowseScreen(); // re-render the screen with the updated browseFlashcards
        }));
        addPanelComponent(buttonPanel, updateButton, Format.COMPONENT, true);

        JButton deleteButton = generateButton("Delete Flashcard");
        deleteButton.addActionListener(generateActionListener(() -> {
            boolean confirmation = displayConfirmationWindow("Delete Flashcards?", "Delete Confirmation");
            if (confirmation)
                deleteCallback.run(table.getSelectedRows());
            renderBrowseScreen(); // re-render the screen with the updated browseFlashcards
        }));
        addPanelComponent(buttonPanel, deleteButton, Format.COMPONENT, false);

        addPanelComponent(panel, buttonPanel, Format.NESTED_PANEL, false);
        setFrameContent(panel);
    }

    /*
     * sendUpdatedFlashcards loops through all the Strings in the table, and compares them to the previous Strings.
     * If they differ, it calls the edit callback to update the Flashcard fields in memory.
     *
     * Input: current table data, previous table data, edit callback to update the flashcard's fields in memory.
     * Output: no return value.
     */
    private void sendUpdatedFlashcards(String[][] tableData, String[][] prevData, EditCallback editCallback) {
        if (tableData.length != prevData.length)
            throw new IllegalArgumentException("Cannot update flashcards because data is improperly sized.");

        for (int i = 0; i < tableData.length; i++) {
            boolean tableValid = tableData[i].length == Flashcard.FIELD_COUNT;
            boolean prevValid = prevData[i].length == Flashcard.FIELD_COUNT;

            if (!tableValid | !prevValid)
                throw new IllegalArgumentException(
                        "Cannot update flashcard at index " + i + " because the data is improperly sized.");

            for (int j = 0; j < Flashcard.FIELD_COUNT; j++) {
                if (!tableData[i][j].equals(prevData[i][j])) {
                    editCallback.run(i, tableData[i]);
                }
            }
            prevData[i] = tableData[i].clone(); // update the previous data
        }
    }

    /*
     * generateActionListener generates an action listener that catches any exceptions,
     * and sends them to Arion to be displayed to the user.
     *
     * Input: code to run when the action listener is run.
     * Output: action listener that catches thrown exceptions.
     */
    private static ActionListener generateActionListener(Runnable onRun) {
        return (ActionEvent e) -> {
            try {
                onRun.run();
            } catch (Exception exception) {
                Arion.displayException(exception);
            }
        };
    }

    /*
     * reenterMainScreen enters the main screen without having to pass the add and study callbacks
     * originally passed. Instead, it uses the callbacks stored when displayMainScreen was originally run.
     *
     * Input: no input.
     * Output: no return value, displays the main screen.
     */
    private void reenterMainScreen() {
        displayMainScreen(addCallback, studyCallback);
    }

    /*
     * parseGuidePages statically parses the xml guide passed in guideFilepath.
     * The guide file may not exist, or the file may be improperly formatted;
     * thus, an Optional is returned in the case that the guide does not exist.
     *
     * Input: file path of the guide xml file.
     * Output: optionally an array nodes, representing a list of pages.
     */
    private static Optional<Node[]> parseGuidePages(String guideFilepath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new SAXErrorHandler());

            Document doc = builder.parse(guideFilepath);
            doc.getDocumentElement().normalize();

            return Optional.of(getRealChildren(doc.getDocumentElement()));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            Arion.displayException("Could not generate guide", e);
        }
        return Optional.empty();
    }

    /*
     * generateGuidePanel generates the panel displaying the guide page "pageNum".
     * It also requires the pop-up in which it will be displayed, since the next and previous
     * buttons modify this pop-up.
     *
     * Input: pop-up in which the guide will be displayed, and the page of the guide to display.
     * Output: panel with the guide content.
     */
    private JPanel generateGuidePanel(MutablePopup popup, int pageNum) throws GuideDisplayException {
        
        Node[] pages = guidePages.orElseThrow(() -> new GuideDisplayException("Guide is not available."));
        int pageCount = pages.length;
        if (pageNum < 0) {
            throw new GuideDisplayException("Cannot display guide page " + pageNum + " because it is less than 0.");
        }
        if (pageNum >= pageCount) {
            throw new GuideDisplayException(
                    "Cannot display guide page " + pageNum + " because there are only " + pageCount + " pages.");
        }
        
        Node page = pages[pageNum];
        String pageName = getNodeAttribute(page, "name")
                .orElseThrow(() -> new GuideDisplayException("Page has no name."));

        JPanel panel;
        try {
            panel = generateMainPanel(pageName, false);
        } catch (MainPanelDisplayException e) {
            throw new GuideDisplayException(e.getMessage());
        }

        // generate the content displaying the guide page
        JPanel content = new JPanel();
        makeDisplayReady(content);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = scrollWrap(content);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(getPopupSize());
        scrollPane.setMinimumSize(getPopupSize());

        int contentWidth = calculateGuideContentWidth(scrollPane);

        Node[] nodes = getRealChildren(page);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            int nodeWidth = contentWidth;

            Optional<String> scaleOption = getNodeAttribute(node, "scale");
            if (scaleOption.isPresent())
                nodeWidth *= parseScale(scaleOption);

            boolean addPadding = (i < nodes.length - 1);
            addNode(content, node, nodeWidth, addPadding, content.getBackground());
        }
        addPanelComponent(panel, scrollPane, Format.COMPONENT, true);
        
        // add buttons to the panel
        GridLayout layout = new GridLayout(1, 3);
        JPanel buttonBar = new JPanel(layout);
        int buttonCount = 0;
        
        boolean prevPage = (pageNum - 1 >= 0);
        boolean nextPage = (pageNum + 1 < pageCount);

        JButton prevButton = generatePopupButton("Previous");
        prevButton.addActionListener(generateActionListener(() -> {
            setGuidePopupContent(popup, pageNum - 1);
        }));
        if (!prevPage) {
            prevButton.setEnabled(false);
        }
        addPanelComponent(buttonBar, prevButton, Format.COMPONENT, true);
        buttonCount++;

        JButton closeButton = generatePopupButton("Close");
        closeButton.addActionListener(generateActionListener(() -> {
            popup.hide();
        }));
        addPanelComponent(buttonBar, closeButton, Format.COMPONENT, true);
        buttonCount++;

        JButton nextButton = generatePopupButton("Next");
        nextButton.addActionListener(generateActionListener(() -> {
            setGuidePopupContent(popup, pageNum + 1);
        }));
        if (!nextPage) {
            nextButton.setEnabled(false);
        }
        addPanelComponent(buttonBar, nextButton, Format.COMPONENT, false);
        buttonCount++;

        setPreferredWidth(buttonBar, getPopupSize().width);
        addPanelComponent(panel, buttonBar, Format.NESTED_PANEL, false);

        return panel;
    }

    /*
     * calculateGuideContentWidth calculates the valid width of the content in the guide.
     * It takes into account the inset and scroll bar of the scroll pane, the width of the pop-up,
     * and the margin around the content.
     *
     * Input: scroll pane in which the content will be displayed.
     * Output: valid width in which content can be displayed inside the scroll pane.
     */
    private int calculateGuideContentWidth(JScrollPane scrollPane) {
        int scrollInset = 0;

        Insets insets = scrollPane.getInsets();
        scrollInset += insets.left + insets.right;

        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        if (scrollBar != null && scrollBar.isVisible())
            scrollInset += scrollBar.getMaximumSize().width;

        JViewport rowHeader = scrollPane.getRowHeader();
        if (rowHeader != null && rowHeader.isVisible())
            scrollInset += rowHeader.getMaximumSize().width;

        int contentWidth = getPopupSize().width - scrollInset; // scroll pane border
        contentWidth -= Format.MARGIN_SIZE * 2; // margin around border

        return contentWidth;
    }

    /*
     * getNodeAttribute optionally gets the String attribute of the node.
     *
     * Input: node with the attribute, and the attribute to get.
     * Output: optional string, representing the value of the attribute.
     */
    private static Optional<String> getNodeAttribute(Node node, String attribute) {
        Node attributeNode = node.getAttributes().getNamedItem(attribute);
        if (attributeNode == null)
            return Optional.empty();
        else
            return Optional.of(attributeNode.getNodeValue());
    }

    /*
     * getRealChildren returns an array of the real children of the passed node.
     * This method filters out comments in the xml file.
     *
     * Input: parent of the children.
     * Output: array of the parent's non-comment children.
     */
    private static Node[] getRealChildren(Node parent) {
        NodeList children = parent.getChildNodes();
        ArrayList<Node> realChildren = new ArrayList<>();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.COMMENT_NODE) {
                continue;
            }

            realChildren.add(child);
        }

        return realChildren.toArray(new Node[0]);
    }

    /*
     * addNode adds a node to the passed panel.
     *
     * Input: panel to add the node to, the node to add, the display width of the node,
     * whether to add padding to the node, and the background color of the node.
     * Output: no return value, adds content to the panel.
     */
    private static void addNode(JPanel panel, Node node, int width, boolean addPadding, Color background)
            throws GuideDisplayException {
        String name = node.getNodeName();

        switch (name) {
            case "txt":
                addTextNode(panel, node, width, addPadding, background);
                break;
            case "img":
                addImageNode(panel, node, width, addPadding);
                break;
            case "pair":
                addPairNode(panel, node, width, addPadding, background);
                break;
            default:
                throw new GuideDisplayException("Encountered invalid guide element.");
        }
    }

    /*
     * addTextNode adds a text node to the passed panel.
     *
     * Input: panel to add the node to, the text node to add, the display width of the node,
     * whether to add padding to the node, and the background color of the node.
     * Output: no return value, adds content to the panel.
     */
    private static void addTextNode(JPanel panel, Node node, int width, boolean addPadding, Color background) {
        StringBuilder textBuilder = new StringBuilder();

        for (Node child : getRealChildren(node)) {
            if (child.getNodeName().equals("brk")) {
                textBuilder.append("\n");
            } else {
                String cdata = child.getNodeValue()
                        .replaceAll("\n *", " ") // remove new lines
                        .replaceAll("^ *", "") // remove leading spaces
                        .replaceAll(" *$", ""); // remove trailing spaces
                textBuilder.append(cdata);
            }
        }

        JTextArea textArea = generateTextArea(textBuilder.toString());
        textArea.setSize(new Dimension(width, 1)); // this sets the preferred width without setting the height
        textArea.setEditable(false);
        textArea.setBackground(background);

        addPanelComponent(panel, textArea, Format.POPUP, addPadding);
    }

    /*
     * addImageNode adds an image node to the passed panel.
     *
     * Input: panel to add the node to, the image node to add, the display width of the node,
     * whether to add padding to the node, and the background color of the node.
     * Output: no return value, adds content to the panel.
     */
    private static void addImageNode(JPanel panel, Node node, int width, boolean addPadding)
            throws GuideDisplayException {

        String src = getNodeAttribute(node, "src").get();
        width -= Format.IMAGE_BORDER_THICKNESS * 2;
        ImageIcon icon = readImageFile(src, width).orElseThrow(
                () -> new GuideDisplayException("Could not read " + src));

        JLabel imageLabel = new JLabel(icon);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.black, Format.IMAGE_BORDER_THICKNESS));
        addPanelComponent(panel, imageLabel, Format.IMAGE, addPadding);
    }

    /*
     * addPairNode adds a pair node to the passed panel.
     * Pair nodes groups two nodes horizontally.
     *
     * Input: panel to add the node to, the pair node to add, the display width of the node,
     * whether to add padding to the node, and the background color of the node.
     * Output: no return value, adds content to the panel.
     */
    private static void addPairNode(JPanel panel, Node node, int width, boolean addPadding, Color background)
            throws GuideDisplayException {
        Node[] children = getRealChildren(node);
        if (children.length != 2)
            throw new GuideDisplayException("Attempting to create pair with invalid number of children.");

        int availableWidth = width - Format.POPUP.padding;

        Node first = children[0];
        Optional<String> firstScaleOption = getNodeAttribute(first, "scale");
        int firstWidth = availableWidth / 2;

        Node second = children[1];
        Optional<String> secondScaleOption = getNodeAttribute(second, "scale");
        int secondWidth;

        if (firstScaleOption.isPresent() && secondScaleOption.isPresent())
            throw new GuideDisplayException("Attempting to construct pair with two sized elements.");

        if (firstScaleOption.isPresent()) {
            firstWidth = (int) (availableWidth * parseScale(firstScaleOption));
        } else if (secondScaleOption.isPresent()) {
            firstWidth = (int) (availableWidth * (1 - parseScale(secondScaleOption)));
        }
        secondWidth = availableWidth - firstWidth;

        JPanel pair = new JPanel();
        pair.setLayout(new BoxLayout(pair, BoxLayout.X_AXIS));

        addNode(pair, first, firstWidth, true, background);
        addNode(pair, second, secondWidth, false, background);

        Dimension maxSize = pair.getMaximumSize();
        maxSize.width = width;

        addPanelComponent(panel, pair, maxSize, Format.NESTED_PANEL, addPadding);
    }

    /*
     * parseScale parses the decimal value of the scale of an image from a String.
     * If there is no scale, no scale is applied, denoted by simply returning 1.
     *
     * Input: Optional String representing the image scale.
     * Output: decimal scale value.
     */
    private static double parseScale(Optional<String> scaleOption) throws GuideDisplayException {
        if (scaleOption.isEmpty()) {
            return 1;
        }

        try {
            double scale = Double.valueOf(scaleOption.get()).doubleValue();
            return Math.min(scale, 1);
        } catch (NumberFormatException e) {
            throw new GuideDisplayException("Attempting to parse invalid scale value.");
        }
    }

    /*
     * setPreferredWidth sets only the width portion of the preferred size of the passed JComponent.
     *
     * Input: component to edit, preferred width.
     * Output: no return value, modifies the passed component.
     */
    private static void setPreferredWidth(JComponent component, int width) {
        Dimension size = component.getPreferredSize();
        size.width = width;
        component.setPreferredSize(size);
    }

    /*
     * getPopupSize calculates the size of a pop-up.
     *
     * Input: no input.
     * Output: desired size of a pop-up window.
     */
    private Dimension getPopupSize() {
        int width = (int) (frame.getWidth() / Format.POPUP_WINDOW_RATIO);
        int height = (int) (frame.getHeight() / Format.POPUP_WINDOW_RATIO);
        return new Dimension(width, height);
    }

    /*
     * readImageFile reads the image from the passed file into an ImageIcon,
     * scaling it such that its width is "width".
     * If the file does not exist, the image cannot be read, or any other error occurs,
     * no image is returned.
     *
     * Input: name of image file, desired width of image.
     * Output: optional image read from the file.
     */
    private static Optional<ImageIcon> readImageFile(String filename, int width) {
        Optional<File> fileOption = Optional.empty();
        for (String dir : IMAGE_DIRECTORIES) {
            File file = new File(dir, filename);
            if (file.canRead()) {
                fileOption = Optional.of(file);
                break;
            }
        }
        if (fileOption.isEmpty())
            return Optional.empty();
        File file = fileOption.get();

        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            return Optional.empty();
        }

        // -1 indicates to maintain the aspect ratio of the original image
        Image scaledImage = img.getScaledInstance(width, -1, Image.SCALE_SMOOTH);
        return Optional.of(new ImageIcon(scaledImage));
    }
    
    /*
     * setGuidePopupContent sets the content of the guide pop-up, without having to refresh it.
     * 
     * Input: the pop-up to edit and the number of the new page to display.
     * Output: no return value, modifies the content of the pop-up.
     */
    private void setGuidePopupContent(MutablePopup popup, int pageNum) {
        try {
            popup.setContent(generateGuidePanel(popup, pageNum));
        }
        catch (GuideDisplayException e) {
            Arion.displayException(e);
        }
    }

    /*
     * displayGuidePageRoutine displays the guide page, without catching exceptions.
     * This is useful for testing, and ensuring that certain exceptions are thrown given certain inputs.
     *
     * Input: number of the guide page to display.
     * Output: no return value, displays a guide page.
     */
    private void displayGuidePageRoutine(int pageNum) throws GuideDisplayException {
        MutablePopup popup = new MutablePopup(frame);
        JPanel panel = generateGuidePanel(popup, pageNum);
        popup.setContent(panel);
        popup.show();
    }
}

/*
 * SAXErrorHandler handles errors that occur when parsing xml documents.
 */
class SAXErrorHandler implements ErrorHandler {
    
    /*
     * error receives notification of a recoverable error.
     *
     * Input: recoverable error.
     * Output: no return value, displays the error to the user.
     */
    public void error(SAXParseException e) {
        Arion.displayException(e);
    }

    /*
     * fatalError receives notification of a non-recoverable error.
     *
     * Input: non-recoverable error.
     * Output: no return value, displays the error to the user.
     */
    public void fatalError(SAXParseException e) {
        Arion.displayException(e);
    }

    /*
     * warning receives notification of a warning.
     *
     * Input: warning.
     * Output: no return value, displays the warning to the user.
     */
    public void warning(SAXParseException e) {
        Arion.displayException(e);
    }
}

/*
 * ExecuteRunnable is a keybind action that simply runs the Runnable it is passed when executed.
 */
class ExecuteRunnable extends AbstractAction {
    public Runnable run;

    /*
     * The Constructor simply stores the provided Runnable.
     *
     * Input: Runnable to execute when the action is performed.
     * Output: new ExecuteRunnable class.
     */
    public ExecuteRunnable(Runnable run) {
        this.run = run;
    }

    /*
     * actionPerformed is invoked when an action occurs.
     *
     * Input: the action event performed.
     * Output: no return value.
     */
    public void actionPerformed(ActionEvent e) {
        run.run();
    }
}

/*
 * The MutablePopup class is a wrapper around the Popup class.
 * It stores the pop-up's child, so it can modify the child to change the content
 * of the pop-up without instantiating another class.
 */
class MutablePopup {
    Popup popup;
    JPanel child;
    JFrame parent;
    boolean before = false;

    /*
     * This constructor instantiates a MutablePopup class with the desired parent.
     *
     * Input: parent of the pop-up.
     * Output: new MutablePopup class.
     */
    public MutablePopup(JFrame parent) {
        this(parent, Optional.empty());
    }
    
    /*
     * This constructor instantiates a MutablePopup class, given the parent and desired content.
     *
     * Input: parent of the pop-up, pop-up content.
     * Output: new MutablePopup class.
     */
    public MutablePopup(JFrame parent, JPanel panel) {
        this(parent, Optional.of(panel));
    }

    /*
     * This constructor signature is only available within the MutablePopup class; is uses an Optional
     * to conditionally set the content of the pop-up.
     *
     * Input: parent of the pop-up, optional pop-up content.
     * Output: new MutablePopup class.
     */
    private MutablePopup(JFrame parent, Optional<JPanel> panelOption) {
        this.parent = parent;
        child = new JPanel();
        child.setLayout(new BoxLayout(child, BoxLayout.Y_AXIS));
        child.setBorder(BorderFactory.createRaisedBevelBorder());

        if (panelOption.isPresent()) {
            setContent(panelOption.get());
        }
    }
    
    /*
     * show instantiates the underlying pop-up, and shows it.
     *
     * Input: no input.
     * Output: no return value, displays a pop-up.
     */
    public void show() {
        int x = (parent.getWidth() - child.getPreferredSize().width) / 2;
        x += parent.getX();
        int y = (parent.getHeight() - child.getPreferredSize().height) / 2;
        y += parent.getY();
        
        PopupFactory factory = PopupFactory.getSharedInstance();
        popup = factory.getPopup(parent, child, x, y);
        popup.show();
    }

    /*
     * hide hides the pop-up.
     *
     * Input: no input.
     * Output: no return value, hides the pop-up.
     */
    public void hide() {
        popup.hide();
    }
    
    /*
     * setContent sets the content of the underlying pop-up, without instantiating another pop-up.
     *
     * Input: JPanel containing desired content.
     * Output: no return value, modifies the child of the pop-up.
     */
    public void setContent(JPanel panel) {
        ArionDisplay.makeDisplayReady(panel);
        
        child.removeAll();
        ArionDisplay.addPanelComponent(child, panel, ArionDisplay.MAX_DIMENSION, ArionDisplay.Format.NESTED_PANEL, false);
        child.setVisible(true);
        child.validate();
    }
}
