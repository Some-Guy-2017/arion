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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class ArionDisplay {
    JFrame frame;

    private Optional<Runnable> addCallbackOption = Optional.empty();
    private Optional<Runnable> studyCallbackOption = Optional.empty();
    private Optional<EditCallback> editCallbackOption = Optional.empty();
    private Optional<DeleteCallback> deleteCallbackOption = Optional.empty();
    private Optional<ArrayList<Flashcard>> browseFlashcardsOption = Optional.empty();

    private final static String[] IMAGE_DIRECTORIES = { "./", "./img/" };
    private final static String GUIDE_FILEPATH = "./guide.xml";
    private final static String ABOUT_FILEPATH = "./about.txt";
    private final static String BACK_BUTTON_FILE = "back-button.png";
    final static Dimension MAX_DIMENSION = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    private Optional<String> aboutContentOption = readAboutContent(ABOUT_FILEPATH);
    private static Optional<Node[]> guidePages = parseGuidePages(GUIDE_FILEPATH);

    /*
     * The Style class represents the styling parameters for a given element.
     * It specifies font style (bold, italics, plain, etc.), the font size, and
     * the padding between this element and the next (if added).
     */
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

    /*
     * The Format class holds all the values used in formatting Arion.
     * This includes the available Styles, the width of text boxes, the number of
     * rows for text areas, and more.
     */
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
        final static float BROWSE_TABLE_WIDTH_RATIO = 1.7f;
        final static int BROWSE_TABLE_MAX_WIDTH = 875;
        final static int TABLE_CELL_PADDING = 2;
        final static Dimension BUTTON_SIZE = new Dimension(300, 40);
        static double POPUP_WINDOW_RATIO = 1.5;
        final static Dimension POPUP_BUTTON_SIZE = new Dimension(125, 25);
        final static int IMAGE_BORDER_THICKNESS = 1;
        final static int BACK_BUTTON_WIDTH = 20;
    }

    /*
     * The ArionDisplay constructor initializes the JFrame containing the GUI.
     * This includes validating the title, width, and height.
     *
     * Input: window title, width, and height.
     * Output: ArionDisplay class.
     */
    public ArionDisplay(String title, int width, int height) {

        if (width < 0 || height < 0) {
            warningAlert("Cannot construct JFrame with invalid width and height values.\n"
                    + "Setting width and height values to 0.");
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
     * displayMenuBar adds a menu bar to the window, given the titles, action names,
     * and callbacks of the menu options.
     *
     * Input: menu titles, action names, and callbacks.
     * Output: no return value, adds menu bar to window.
     */
    public void displayMenuBar(String[] menuTitles, String[][] actions, Runnable[][] callbacks) {

        if (menuTitles == null || actions == null || callbacks == null) {
            throw new NullPointerException("Cannot construct menu because received a null parameter.");
        }
        if (menuTitles.length != actions.length || menuTitles.length != callbacks.length) {
            throw new IllegalArgumentException("Cannot construct menu because"
                    + " menu titles, actions, and callbacks are not equally sized.");
        }

        int menuCount = menuTitles.length;
        if (menuCount == 0) {
            System.out.println("WARNING: Menu is empty, so it will not render.");
        }

        JMenuBar menuBar = new JMenuBar();

        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++) {
            String menuActions[] = actions[menuIdx];
            Runnable[] menuCallbacks = callbacks[menuIdx];

            if (menuActions.length != menuCallbacks.length) {
                throw new IllegalArgumentException("Cannot construct menu because"
                        + " menu actions and callbacks are different lengths at index "
                        + menuIdx + ".");
            }

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

        frame.setJMenuBar(menuBar);
    }

    /*
     * displayMainScreen displays the main screen.
     * The main screen contains a Study button, which calls a passed callback, and
     * an Add button, which calls another callback.
     *
     * Input: Add button and Study button callbacks.
     * Output: no return value, displays main screen.
     */
    public void displayMainScreen(Runnable addCallback, Runnable studyCallback) {
        if (addCallback == null || studyCallback == null) {
            throw new NullPointerException("Could not display main screen because passed callbacks are null.");
        }

        // store the callbacks so the main screen can be displayed again with them
        this.addCallbackOption = Optional.of(addCallback);
        this.studyCallbackOption = Optional.of(studyCallback);

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
     * displayBrowseScreen sets flashcards, edit callback, and delete callback for
     * the edit screen.
     * It does not directly render the browse so that other internal methods can
     * render the previous browse screen,
     * without passing flashcards or callbacks.
     * The description of the browse screen is described above the
     * renderBrowseScreen method.
     *
     * Input: list of flashcards to display in the browse screen, and an edit and
     * delete callback for the browse screen.
     * Output: no return value, displays the browse screen.
     */
    public void displayBrowseScreen(ArrayList<Flashcard> flashcards, EditCallback editCallback,
            DeleteCallback deleteCallback) {
        
        if (flashcards == null || editCallback == null || deleteCallback == null) {
            throw new NullPointerException("Could not display browse screen because passed callbacks are null.");
        }

        this.browseFlashcardsOption = Optional.of(flashcards);
        this.editCallbackOption = Optional.of(editCallback);
        this.deleteCallbackOption = Optional.of(deleteCallback);
        renderBrowseScreen();
    }

    /*
     * displayAddScreen displays the add screen.
     * The add screen consists of a small text field for the user to input the front
     * of the flashcard,
     * and a large text area for the user to input the back of the card.
     * It also has an "Add Flashcard" button for the user to confirm their inputs
     * and add a new flashcard with the provided fields to the deck.
     * This functionality is provided through a callback passed to the method.
     *
     * Input: "Add Flashcard" button callback.
     * Output: no return value, displays add screen.
     */
    public void displayAddScreen(AddCallback callback) {
        
        if (callback == null) {
            throw new NullPointerException("Could not display add screen because passed callback is null.");
        }

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
     * The study screen displays the front or back of the flashcard, whichever is
     * passed by the Study functionality.
     * It consists of the flashcard front or back, along with a "Flip" button for
     * the front, or a "Correct" button and an "Incorrect" button for the back.
     * Initially, the method is called with front = true, so the front of the
     * flashcard is displayed.
     * After the user clicks the "Flip" button, this method is called by itself with
     * front = false so the back is shown.
     * When a review button is clicked, the review callback is called to update the
     * flashcard.
     * This callback is passed the boolean representing whether the user clicked "Correct" or "Incorrect",
     * to correctly update the flashcard.
     * If there are more due flashcards, the callback will call this method again;
     * if not, it will exit the loop.
     *
     * Input: flashcard to study, whether to show the front, and a callback to be
     * run when the flashcard has been reviewed.
     * Output: no return value, displays the front or back of the flashcard in a
     * study screen.
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
        } else {
            text = flashcard.back;
        }

        JLabel textLabel = new JLabel(text);
        addPanelComponent(panel, textLabel, Format.STUDY_TEXT, true);

        JPanel buttonBar = new JPanel();
        buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));
        buttonBar.add(Box.createGlue());

        // add actions to the JPanel
        Runnable flipCard = () -> displayStudyScreen(flashcard, false, reviewCallback);
        Runnable correctReview = () -> reviewCallback.run(true);
        Runnable incorrectReview = () -> reviewCallback.run(false);
        panel.getActionMap().put("flipCard", new ExecuteRunnable(flipCard));
        panel.getActionMap().put("correctReview", new ExecuteRunnable(correctReview));
        panel.getActionMap().put("incorrectReview", new ExecuteRunnable(incorrectReview));
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (front) {
            JButton flipButton = generateButton("Flip");
            addPanelComponent(buttonBar, flipButton, Format.COMPONENT, false);

            flipButton.addActionListener(generateActionListener(flipCard));

            // add keybind to execute flipping the card
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "flipCard");

        } else {
            JButton correctButton = generateButton("Correct");
            addPanelComponent(buttonBar, correctButton, Format.COMPONENT, true);
            correctButton.addActionListener(generateActionListener(correctReview));

            // add keybind to execute the correct review
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "correctReview");

            JButton incorrectButton = generateButton("Incorrect");
            addPanelComponent(buttonBar, incorrectButton, Format.COMPONENT, false);
            incorrectButton.addActionListener(generateActionListener(incorrectReview));

            // add keybind to execute the incorrect review
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "incorrectReview");
        }

        buttonBar.add(Box.createGlue());

        panel.add(Box.createVerticalGlue());
        addPanelComponent(panel, buttonBar, Format.NESTED_PANEL, false);

        setFrameContent(panel);
    }

    /*
     * displaySortScreen displays the sort screen.
     * The sort screen consists of two drop-down boxes, allowing the user to select
     * the field to sort by, and the direction to sort by.
     * Additionally, this screen has a "Sort" button for the user to confirm their selection.
     * When the "Sort" button is clicked, the passed callback is executed with the provided
     * configuration by the user.
     *
     * Input: callback to sort the flashcards.
     * Output: no return value, displays the sort screen.
     */
    public void displaySortScreen(SortCallback sortCallback) {
        
        if (sortCallback == null) {
            throw new NullPointerException("Cannot display sort screen with null callback.");
        }

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

        // find the max width of the combo boxes so all boxes have
        // the same width
        int maxWidth = -1;
        for (JComboBox comboBox : comboBoxes) {
            int width = comboBox.getPreferredSize().width;
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        for (JComboBox comboBox : comboBoxes) {
            setPreferredWidth(comboBox, maxWidth);
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
     * displayGuidePage displays the guide on a given page.
     * The guide pages are defined in GUIDE_FILEPATH using xml.
     * The explain how to use all the features in Arion, and always have
     * a button bar on the bottom with Previous, Close, and Next buttons.
     * Previous goes to the previous page, Close closes the guide, and Next
     * advances to the next page.
     * This particular method is a wrapper around the method that actually
     * displays the guide, to catch and handle any exceptions.
     *
     * Input: the number of the page to display.
     * Output: no return value, displays the requested guide page.
     */
    public void displayGuidePage(int pageNum) {
        try {
            MutablePopup popup = new MutablePopup(frame);
            JPanel panel = generateGuidePanel(popup, pageNum);
            popup.setContent(panel);
            popup.show();
        } catch (GuideDisplayException e) {
            Arion.displayException(e);
        }
    }

    /*
     * displayAboutScreen displays the About screen.
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

        if (aboutContentOption.isEmpty()) {
            warningAlert("Cannot display About window because could not parse " + ABOUT_FILEPATH);
            return;
        }
        JTextArea textArea = generateTextArea(aboutContentOption.get());
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
     * alert statically displays a message in a dialog box; this is useful if
     * ArionDisplay may not be initialized.
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
     * displayConfirmationWindow displays a confirmation window with the provided
     * message and title.
     *
     * Input: message and title of the confirmation window.
     * Output: boolean, whether the user confirmed.
     */
    public boolean displayConfirmationWindow(String message, String title) {
        if (message == null || title == null) {
            throw new NullPointerException("Cannot display confirmation window with null parameters.");
        }

        int response = JOptionPane.showConfirmDialog(frame, message,
                title, JOptionPane.YES_NO_OPTION);
        
        return response == JOptionPane.YES_OPTION; 
    }

    /*
     * quit cleans the class, removing the JFrame.
     *
     * Input: no input.
     * Output: no output.
     */
    public void quit() {
        frame.setVisible(false);
        frame.dispose();
    }

    /*
     * displaySuccessScreen displays a success screen, congratulating the user on
     * successfully studying all their flashcards.
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
     * Input: name of the screen, and a boolean whether to add a back button to
     * return to the main screen.
     * Output: a JPanel for the main screen.
     */
    private JPanel generateMainPanel(String screenName, boolean addBackButton) throws MainPanelDisplayException {
        if (screenName == null) {
            throw new NullPointerException("Cannot display main panel with null name");
        }
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // top bar contains the header and the back button
        JPanel topBar = new JPanel();
        topBar.setLayout(new OverlayLayout(topBar));
        
        JLabel header = new JLabel(screenName);
        addPanelComponent(topBar, header, Format.H1, false);

        if (addBackButton) {
            Optional<ImageIcon> iconOption = readImageFile(BACK_BUTTON_FILE, Format.BACK_BUTTON_WIDTH);
            ImageIcon icon = iconOption.orElseThrow(
                    () -> new MainPanelDisplayException("Could not read back button image."));

            JButton backButton = new JButton(icon);
            backButton.addActionListener(generateActionListener(() -> reenterMainScreen()));

            // wrap the button to align it to the right
            JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            addPanelComponent(buttonWrapper, backButton, Format.COMPONENT, false);
            
            Dimension maxSize = buttonWrapper.getPreferredSize();
            maxSize.width = Integer.MAX_VALUE;
            addPanelComponent(topBar, buttonWrapper, maxSize, Format.NESTED_PANEL, false);
        }

        Dimension maxSize = topBar.getPreferredSize();
        maxSize.width = (int) ((frame.getWidth() - Format.MARGIN_SIZE * 2) * 0.99);
        addPanelComponent(panel, topBar, maxSize, Format.NESTED_PANEL, true);
        return panel;
    }

    /*
     * makeDisplayReady makes the main panel ready to be displayed to the user.
     * It is factored out of setFrameContent for when the content is not eventually
     * displayed in the JFrame, such as when it is displayed in a pop-up.
     * 
     * Input: panel to make display ready.
     * Output: no return value, modifies the input panel.
     */
    static void makeDisplayReady(JPanel panel) {
        if (panel == null) {
            throw new NullPointerException("Cannot make null panel display ready.");
        }
        
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
        if (panel == null) {
            throw new NullPointerException("Cannot make frame content null.");
        }
        
        makeDisplayReady(panel);

        Container contentPane = frame.getContentPane();
        contentPane.removeAll();
        contentPane.add(panel);

        frame.validate();
    }

    /*
     * generateTable generates a JTable that can be displayed to the user.
     *
     * Input: names of the columns and the table data.
     * Output: formatted table.
     */
    private JTable generateTable(String[] columnNames, String[][] data) {

        // validate the data
        if (columnNames == null || data == null) {
            throw new NullPointerException("Cannot generate table with null parameters");
        }
        if (data.length != 0) {
            if (columnNames.length != data[0].length) {
                throw new IllegalArgumentException("Cannot generate table with differently sized columns and data.");
            }
        }
        int len = columnNames.length;
        for (int i = 0; i < len; i++) {
            if (columnNames[i] == null) {
                throw new NullPointerException("Column name #" + (i+1) + " is null.");
            }
            for (int j = 0; j < data.length; j++) {
                if (data[j] == null) {
                    throw new NullPointerException("Data row #" + (i+1) + " is null.");
                }
                if (data[j][i] == null) {
                    throw new NullPointerException("Data array[" + i + "][" + j + "] is null.");
                }
            }
        }

        JTable table = new JTable(data, columnNames);
        setFont(table.getTableHeader(), Format.H2);
        setFont(table, Format.COMPONENT); // set the font of the table content

        // set row height to match font size and text padding
        table.setRowHeight((int) Format.COMPONENT.fontSize + Format.TABLE_CELL_PADDING * 2);

        return table;
    }

    /*
     * This is a wrapper method hiding the use of Optionals in the
     * generateTextArea method.
     * This signature takes no text, so returns a formatted text area with
     * no default text.
     *
     * Input: no input.
     * Output: formatted text area with no text.
     */
    private JTextArea generateTextArea() {
        return _generateTextArea(Optional.empty());
    }

    /*
     * This is a wrapper method hiding the use of Optionals in the
     * generateTextArea implementation.
     * This signature takes the text to insert in the text area, and returns a formatted text area
     * with the text.
     *
     * Input: text in the text area.
     * Output: formatted text area with text.
     */
    private static JTextArea generateTextArea(String text) {
        if (text == null) {
            throw new NullPointerException("Cannot generate text area with null text");
        }
        
        return _generateTextArea(Optional.of(text));
    }

    /*
     * _generateTextArea generates a formatted text area that can be displayed to the
     * user.
     * It can optionally have text passed to it, depending on the method signature
     * used.
     * This method is not intended to be used outside of its other signatures; instead,
     * the signatures should be used.
     *
     * Input: Optional text to put in the text area.
     * Output: formatted text area.
     */
    private static JTextArea _generateTextArea(Optional<String> text) {
        if (text == null) {
            throw new NullPointerException("Cannot generate text area with null text Optional");
        }
        
        JTextArea textArea;
        if (text.isPresent()) {
            textArea = new JTextArea(text.get());
        } else {
            textArea = new JTextArea();
        }

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
        if (component == null) {
            throw new NullPointerException("Cannot wrap null component in scroll pane.");
        }
        
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
        if (text == null) {
            throw new NullPointerException("Cannot generate button with null text.");
        }

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
        if (text == null) {
            throw new NullPointerException("Cannot generate button with null text.");
        }
        return generateSizedButton(text, Format.POPUP_BUTTON_SIZE);
    }

    /*
     * generateSizedButton generates a button of the provided size.
     * 
     * Input: button text and button size.
     * Output: formatted button.
     */
    private JButton generateSizedButton(String text, Dimension size) {
        if (text == null || size == null) {
            throw new NullPointerException("Cannot generate button with null parameters.");
        }
        if (size.width < 0 || size.height < 0) {
            throw new IllegalArgumentException("Cannot generate button with negative dimension");
        }
        
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        return button;
    }

    /*
     * setFont sets the component's font size and style to the size and style
     * defined in the Style class.
     *
     * Input: component with font to overwrite, and style defining the font size and
     * style.
     * Output: no return value, writes to the component's font.
     */
    private static void setFont(JComponent component, Style style) {
        if (component == null || style == null) {
            throw new NullPointerException("Cannot set font with null parameters.");
        }
        
        Font newFont = component.getFont().deriveFont(style.fontStyle, style.fontSize);
        component.setFont(newFont);
    }

    /*
     * This method is a wrapper for addPanelComponent that hides the use of an
     * Optional to optionally accept a maximum size.
     * This method signature does not accept a maximum size, so passes an empty
     * Optional.
     *
     * Input: panel to add the component to, component to add, component style,
     * and whether to append padding after the component.
     * Output: no return value, adds the component to the panel.
     */
    static void addPanelComponent(JPanel panel, JComponent component, Style style, boolean addPadding) {
        _addPanelComponent(panel, component, Optional.empty(), style, addPadding);
    }

    /*
     * This method is a wrapper for addPanelComponent that hides the use of an
     * Optional to optionally accept a maximum size.
     * This method signature accepts a maximum size, which it passes through an
     * Optional.
     *
     * Input: panel to add the component, component to add, maximum size of the
     * component, component style, and whether to append padding after the component.
     * Output: no return value, adds the component to the panel.
     */
    static void addPanelComponent(JPanel panel, JComponent component, Dimension maxSize, Style style,
            boolean addPadding) {
        _addPanelComponent(panel, component, Optional.of(maxSize), style, addPadding);
    }

    /*
     * _addPanelComponent formats a JComponent, then adds it to the panel.
     * This method is not supposed to be used internally, other than by the
     * other two signatures for addPanelComponent. Instead, the signatures
     * should be run.
     * If there is no max size, the max size is set to preferred size of the component
     * after setting its font.
     *
     * Input: panel to add the component to, component to add, component style,
     * optionally the maximum size of the component, and whether to append padding
     * after the component.
     * Output: no return value, adds the component to the panel.
     */
    private static void _addPanelComponent(JPanel panel, JComponent component, Optional<Dimension> maxSize,
            Style style, boolean addPadding) {
        if (panel == null || component == null || maxSize == null || style == null) {
            throw new NullPointerException("Cannot add null panel component");
        }

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
     * The browse screen consists of an editable table listing the flashcards in
     * memory,
     * where users can click and edit the fields of their flashcards.
     * There is an "Update Flashcards" button to confirm the edited fields,
     * and a "Delete Flashcard" button to delete any selected flashcards.
     *
     * Input: no inputs.
     * Output: no return value, displays the browse screen.
     */
    private void renderBrowseScreen() {
        if (browseFlashcardsOption.isEmpty()
                || editCallbackOption.isEmpty()
                || deleteCallbackOption.isEmpty()) {
            throw new IllegalStateException("Cannot render browse screen without having set"
                    + " browse flashcards, edit callback, or delete callback.");
        }
        ArrayList<Flashcard> browseFlashcards = browseFlashcardsOption.get();
        EditCallback editCallback = editCallbackOption.get(); 
        DeleteCallback deleteCallback = deleteCallbackOption.get(); 
        
        JPanel panel;
        try {
            panel = generateMainPanel("Browse", true);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }

        String[] columnNames = Flashcard.FIELD_TITLES;
        String[][] tableData = new String[browseFlashcards.size()][];
        for (int i = 0; i < browseFlashcards.size(); i++) {
            tableData[i] = browseFlashcards.get(i).toStringArray();
        }

        JTable table = generateTable(columnNames, tableData);
        int tableWidth = (int) (frame.getSize().width / Format.BROWSE_TABLE_WIDTH_RATIO);
        tableWidth = Math.min(tableWidth, Format.BROWSE_TABLE_MAX_WIDTH);

        JScrollPane scrollPane = scrollWrap(table);
        setPreferredWidth(scrollPane, tableWidth);
        addPanelComponent(panel, scrollPane, Format.COMPONENT, true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // track the previous data to only update changed flashcards
        String[][] prevData = new String[tableData.length][];
        for (int i = 0; i < tableData.length; i++) {
            prevData[i] = ArionUtils.cloneArray(tableData[i]);
        }

        JButton updateButton = generateButton("Update Flashcards");
        updateButton.addActionListener(generateActionListener(() -> {
            sendUpdatedFlashcards(tableData, prevData, editCallback);
            renderBrowseScreen(); // re-render the screen with the updated flashcards
        }));
        addPanelComponent(buttonPanel, updateButton, Format.COMPONENT, true);

        JButton deleteButton = generateButton("Delete Flashcards");
        deleteButton.addActionListener(generateActionListener(() -> {
            
            boolean confirmation = displayConfirmationWindow("Delete Flashcards?", "Delete Confirmation");
            if (confirmation) {
                deleteCallback.run(table.getSelectedRows());
            }
            renderBrowseScreen(); // re-render the screen with the updated flashcards
                                  
        }));
        addPanelComponent(buttonPanel, deleteButton, Format.COMPONENT, false);

        addPanelComponent(panel, buttonPanel, Format.NESTED_PANEL, false);
        setFrameContent(panel);
    }

    /*
     * sendUpdatedFlashcards sends the updated flashcards to the passed edit callback.
     * It loops through the Strings currently in the table, and the ones in the previous
     * table.
     * If they differ, it passes them to the edit callback to update the flashcard fields.
     *
     * Input: current browse table data, previous browse table data, edit callback to update the
     * flashcard's fields in memory.
     * Output: no return value.
     */
    private void sendUpdatedFlashcards(String[][] tableData, String[][] prevData, EditCallback editCallback) {
        if (tableData == null || prevData == null || editCallback == null) {
            throw new NullPointerException("Cannot send updated flashcards with null parameters.");
        }
        if (tableData.length != prevData.length) {
            throw new IllegalArgumentException("Cannot update flashcards because data is improperly sized.");
        }

        for (int i = 0; i < tableData.length; i++) {
            boolean tableValid = tableData[i].length == Flashcard.FIELD_COUNT;
            boolean prevValid = prevData[i].length == Flashcard.FIELD_COUNT;

            if (!tableValid || !prevValid) {
                throw new IllegalArgumentException("Cannot update flashcard at index"
                        + i + " because the data is improperly sized.");
            }

            boolean same = true;
            for (int j = 0; j < Flashcard.FIELD_COUNT; j++) {
                if (!tableData[i][j].equals(prevData[i][j])) {
                    same = false;
                    break;
                }
            }
            if (!same) {
                editCallback.run(i, tableData[i]);
            }
            prevData[i] = ArionUtils.cloneArray(tableData[i]); // update the previous data
        }
    }

    /*
     * generateActionListener generates an action listener that catches any
     * exceptions, and sends them to Arion to be displayed to the user.
     *
     * Input: code to run when the action listener is run.
     * Output: action listener that catches thrown exceptions.
     */
    private static ActionListener generateActionListener(Runnable onRun) {
        if (onRun == null) {
            throw new NullPointerException("Cannot generate action listener with null Runnable");
        }
        
        return (ActionEvent e) -> {
            try {
                onRun.run();
            } catch (Exception exception) {
                Arion.displayException(exception);
            }
        };
    }

    /*
     * reenterMainScreen enters the main screen without having to pass the add and
     * study callbacks originally passed. Instead, it uses the callbacks stored when
     * displayMainScreen was originally run.
     *
     * Input: no input.
     * Output: no return value, displays the main screen.
     */
    private void reenterMainScreen() {
        if (addCallbackOption.isEmpty() || studyCallbackOption.isEmpty()) {
            throw new IllegalStateException("Cannot enter main screen without add or study callback.");
        }
        
        displayMainScreen(addCallbackOption.get(), studyCallbackOption.get());
    }

    /*
     * parseGuidePages parses the xml file passed in guideFilepath that represents
     * the guide.
     * The guide file may not exist, or the file may be improperly formatted;
     * thus, an Optional is returned in the case that the guide does not exist.
     *
     * Input: file path of the guide xml file.
     * Output: optionally an array nodes, representing a list of pages.
     */
    private static Optional<Node[]> parseGuidePages(String guideFilepath) {
        if (guideFilepath == null) {
            throw new NullPointerException("Cannot parse null guide");
        }
        
        Document doc;
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true); // validate the document as it is parsed
            factory.setIgnoringElementContentWhitespace(true); // eliminate ignorable
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new SAXErrorHandler());

            doc = builder.parse(guideFilepath);
            doc.getDocumentElement().normalize();

        } catch (ParserConfigurationException | SAXException | IOException e) {
            return Optional.empty();
        }
        
        return Optional.of(getRealChildren(doc.getDocumentElement()));
    }

    /*
     * generateGuidePanel generates the panel that displays the specified guide page.
     * It also requires the pop-up in which it will be displayed, since the next and
     * previous buttons modify this pop-up.
     *
     * Input: pop-up in which the guide will be displayed, and the page of the guide
     * to display.
     * Output: panel with the guide content.
     */
    private JPanel generateGuidePanel(MutablePopup popup, int pageNum) throws GuideDisplayException {
        if (popup == null) {
            throw new NullPointerException("Cannot generate guide panel with null pop-up");
        }

        Node[] pages = guidePages.orElseThrow(() -> new GuideDisplayException("Guide is not available."));
        int pageCount = pages.length;
        if (pageNum < 0) {
            throw new GuideDisplayException("Cannot display guide page "
                    + pageNum + " because it is less than 0.");
        }
        if (pageNum >= pageCount) {
            throw new GuideDisplayException("Cannot display guide page "
                    + pageNum + " because there are only " + pageCount + " pages.");
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
            if (scaleOption.isPresent()) {
                nodeWidth *= parseScale(scaleOption);
            }

            boolean addPadding = (i < nodes.length - 1);
            addNode(content, node, nodeWidth, addPadding, content.getBackground());
        }
        addPanelComponent(panel, scrollPane, Format.COMPONENT, true);

        // add buttons to the panel
        JPanel buttonBar = new JPanel(new GridLayout(1, 3));
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
     * calculateGuideContentWidth calculates the valid width of the content in the
     * guide.
     * It takes into account the inset and scroll bar of the scroll pane, the width
     * of the pop-up, and the margin around the content.
     *
     * Input: scroll pane in which the content will be displayed.
     * Output: valid width in which content can be displayed inside the scroll pane.
     */
    private int calculateGuideContentWidth(JScrollPane scrollPane) {
        if (scrollPane == null) {
            throw new NullPointerException("Cannot calculate guide content width of null scroll pane");
        }
        
        int widthInset = Format.MARGIN_SIZE * 2;

        Insets insets = scrollPane.getInsets();
        widthInset += insets.left + insets.right;

        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        boolean useScrollBar = scrollPane.getVerticalScrollBarPolicy() != JScrollPane.VERTICAL_SCROLLBAR_NEVER;
        boolean scrollBarExists = (scrollBar != null);
        if (useScrollBar && scrollBarExists && scrollBar.isVisible()) {
            widthInset += scrollBar.getMaximumSize().width;
        }

        JViewport rowHeader = scrollPane.getRowHeader();
        if (rowHeader != null && rowHeader.isVisible()) {
            widthInset += rowHeader.getMaximumSize().width;
        }

        return getPopupSize().width - widthInset;
    }

    /*
     * getNodeAttribute optionally gets the String attribute of the node.
     *
     * Input: node with the attribute, and the attribute to get.
     * Output: optionally a string representing the value of the attribute.
     */
    private static Optional<String> getNodeAttribute(Node node, String attribute) {
        if (node == null || attribute == null) {
            throw new NullPointerException("Cannot get node attribute will null parameters.");
        }
        
        Node attributeNode = node.getAttributes().getNamedItem(attribute);
        if (attributeNode == null) {
            return Optional.empty();
        } else {
            return Optional.of(attributeNode.getNodeValue());
        }
    }

    /*
     * getRealChildren returns an array of the real children of the passed node.
     * This method filters out comments in the xml file.
     *
     * Input: parent of the children.
     * Output: array of the parent's non-comment children.
     */
    private static Node[] getRealChildren(Node parent) {
        if (parent == null) {
            throw new NullPointerException("Cannot get real children of null parent.");
        }
        
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
     * Input: panel to add the node to, the node to add, the display width of the
     * node, whether to add padding to the node, and the background color of the
     * node.
     * Output: no return value, adds content to the panel.
     */
    private static void addNode(JPanel panel, Node node, int width, boolean addPadding, Color background)
            throws GuideDisplayException {
        if (panel == null || node == null || background == null) {
            throw new NullPointerException("Cannot add node with null parameters.");
        }
            
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
     * Input: panel to add the node to, the text node to add, the display width of
     * the node, whether to add padding to the node, and the background color of the node.
     * Output: no return value, adds content to the panel.
     */
    private static void addTextNode(JPanel panel, Node node, int width, boolean addPadding, Color background) {
        if (panel == null || node == null || background == null) {
            throw new NullPointerException("Cannot add text node with null parameters.");
        }
        if (!node.getNodeName().equals("txt")) {
            throw new IllegalArgumentException("Must pass text node");
        }
        
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
     * Input: panel to add the node to, the image node to add, the display width of
     * the node, whether to add padding to the node,
     * and the background color of the node.
     * Output: no return value, adds content to the panel.
     */
    private static void addImageNode(JPanel panel, Node node, int width, boolean addPadding)
            throws GuideDisplayException {
        if (panel == null || node == null) {
            throw new NullPointerException("Cannot add image node with null parameters.");
        }
            
        if (!node.getNodeName().equals("img")) {
            throw new IllegalArgumentException("Must pass text node");
        }

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
     * Pair nodes contains two children, and it groups them horizontally.
     *
     * Input: panel to add the node to, the pair node to add, the display width of
     * the node, whether to add padding to the node,
     * and the background color of the node.
     * Output: no return value, adds content to the panel.
     */
    private static void addPairNode(JPanel panel, Node node, int width, boolean addPadding, Color background)
            throws GuideDisplayException {
        if (panel == null || node == null || background == null) {
            throw new NullPointerException("Cannot add pair node with null parameters.");
        }
            
        if (!node.getNodeName().equals("pair")) {
            throw new IllegalArgumentException("Must pass text node");
        }
            
        Node[] children = getRealChildren(node);
        if (children.length != 2) {
            throw new GuideDisplayException("Attempting to create pair with invalid number of children.");
        }
        
        // find the content width by subtracting the width of the padding
        // between the elements
        int contentWidth = width - Format.POPUP.padding;
        
        Node first = children[0];
        Optional<String> firstScaleOption = getNodeAttribute(first, "scale");

        Node second = children[1];
        Optional<String> secondScaleOption = getNodeAttribute(second, "scale");

        if (firstScaleOption.isPresent() && secondScaleOption.isPresent()) {
            throw new GuideDisplayException("Attempting to construct pair with two sized elements.");
        }

        int firstWidth;
        if (firstScaleOption.isPresent()) {
            firstWidth = (int) (contentWidth * parseScale(firstScaleOption));
        } else if (secondScaleOption.isPresent()) {
            firstWidth = (int) (contentWidth * (1 - parseScale(secondScaleOption)));
        } else {
            firstWidth = contentWidth / 2;
        }
        int secondWidth = contentWidth - firstWidth;

        JPanel pair = new JPanel();
        pair.setLayout(new BoxLayout(pair, BoxLayout.X_AXIS));

        addNode(pair, first, firstWidth, true, background);
        addNode(pair, second, secondWidth, false, background);

        Dimension maxSize = pair.getMaximumSize();
        maxSize.width = width;

        addPanelComponent(panel, pair, maxSize, Format.NESTED_PANEL, addPadding);
    }

    /*
     * parseScale parses the decimal value of an image scale from a String.
     * If there is no scale, no scale is applied, denoted by simply returning 1.
     * Since this scale is a proportion of the maximum width,
     * it cannot be larger than 1.
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
     * setPreferredWidth sets the width portion of a component's preferred size.
     *
     * Input: component to edit, and the preferred width.
     * Output: no return value, modifies the passed component.
     */
    private static void setPreferredWidth(JComponent component, int width) {
        if (component == null) {
            throw new NullPointerException("Cannot set preferred width of null component");
        }
        if (width < 0) {
            throw new IllegalArgumentException("Cannot set width to less than 0.");
        }
        
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
     * readImageFile reads the image from the passed file name into an ImageIcon,
     * scaling it such that its width matches the passed value.
     * If the file does not exist, the image cannot be read, or any other error
     * occurs, no image is returned.
     *
     * Input: name of image file, and desired width of image.
     * Output: optional image read from the file.
     */
    private static Optional<ImageIcon> readImageFile(String filename, int width) {
        if (filename == null) {
            throw new NullPointerException("Cannot read null image file");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("Cannot make image less than 0 pixels wide.");
        }
        
        File file = null;
        boolean found = false;
        for (String dir : IMAGE_DIRECTORIES) {
            file = new File(dir, filename);
            if (file.canRead()) {
                found = true;
                break;
            }
        }
        
        if (!found) {
            return Optional.empty();
        }

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
     * setGuidePopupContent sets the content of the guide pop-up to a certain
     * page of the guide, without having to refresh it.
     * 
     * Input: the pop-up to edit and the number of the new page to display.
     * Output: no return value, modifies the content of the pop-up.
     */
    private void setGuidePopupContent(MutablePopup popup, int pageNum) {
        if (popup == null) {
            throw new NullPointerException("Cannot set content of null pop-up");
        }
        try {
            popup.setContent(generateGuidePanel(popup, pageNum));
        } catch (GuideDisplayException e) {
            Arion.displayException(e);
        }
    }

    /*
     * readAboutContent tries to read the content from the about file. If an exception occurs,
     * such as if the file does not exist, it will return an empty Optional; otherwise, it
     * will return the content.
     *
     * Input: filepath to read about content from.
     * Output: optionally content of the about page.
     */
    private static Optional<String> readAboutContent(String filepath) {
        if (filepath == null) {
            throw new NullPointerException("Cannot read about content from null filepath.");
        }

        String aboutContent;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            aboutContent = sb.toString()
                    .replaceAll("\n *\n", "\n\n") // remove lines with only spaces
                    .replaceAll("\n *", "\n") // remove spaces in front of newlines
                    .replaceAll("(?<!\n)\n(?!\n)", " ") // remove single new lines
                    .replaceAll("\n", "\n    ") // add tabs after newlines
                    .replaceAll("^ *", "    ") // remove leading spaces and add a tab to the start of paragraphs
                    .replaceAll(" *$", ""); // remove trailing spaces
        } catch (IOException e) {
            return Optional.empty();
        }

        return Optional.of(aboutContent);
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
        if (e == null) {
            Arion.displayException(new NullPointerException("Cannot parse handle null exception."));
            return;
        }
        Arion.displayException(e);
    }

    /*
     * fatalError receives notification of a non-recoverable error.
     *
     * Input: non-recoverable error.
     * Output: no return value, displays the error to the user.
     */
    public void fatalError(SAXParseException e) {
        if (e == null) {
            Arion.displayException(new NullPointerException("Cannot parse handle null exception."));
            return;
        }
        Arion.displayException(e);
    }

    /*
     * warning receives notification of a warning.
     *
     * Input: warning.
     * Output: no return value, displays the warning to the user.
     */
    public void warning(SAXParseException e) {
        if (e == null) {
            Arion.displayException(new NullPointerException("Cannot parse handle null exception."));
            return;
        }
        Arion.displayException(e);
    }
}

/*
 * ExecuteRunnable is a keybind action that simply runs the Runnable it is
 * passed when executed.
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
        if (run == null) {
            throw new NullPointerException("Cannot construct ExecuteRunnable with null Runnable");
        }
        this.run = run;
    }

    /*
     * actionPerformed is invoked when an action occurs.
     *
     * Input: the action event performed.
     * Output: no return value.
     */
    public void actionPerformed(ActionEvent e) {
        if (e == null) {
            throw new NullPointerException("Cannot perform action with null action event.");
        }
        run.run();
    }
}

/*
 * The MutablePopup class is a wrapper around the Popup class.
 * It stores the pop-up's child, so it can modify the child to change the
 * content
 * of the pop-up without instantiating another class.
 */
class MutablePopup {
    Optional<Popup> popupOption;
    JPanel child;
    JFrame parent;

    /*
     * This constructor instantiates a MutablePopup class with the desired parent.
     *
     * Input: parent of the pop-up.
     * Output: new MutablePopup class.
     */
    public MutablePopup(JFrame parent) {
        if (parent == null) {
            throw new NullPointerException("Cannot construct MutablePopup with null parameters.");
        }
        _init(parent, Optional.empty());
    }

    /*
     * This constructor instantiates a MutablePopup class, given the parent and
     * desired content.
     *
     * Input: parent of the pop-up, pop-up content.
     * Output: new MutablePopup class.
     */
    public MutablePopup(JFrame parent, JPanel panel) {
        if (parent == null || panel == null) {
            throw new NullPointerException("Cannot construct MutablePopup with null parameters.");
        }
        _init(parent, Optional.of(panel));
    }

    /*
     * _init is only used internally by the constructors to initialize this class.
     * It uses an Optional to conditionally set the content of the pop-up.
     *
     * Input: parent of the pop-up, optional pop-up content.
     * Output: new MutablePopup class.
     */
    private void _init(JFrame parent, Optional<JPanel> panelOption) {
        if (parent == null || panelOption == null) {
            throw new NullPointerException("Cannot construct MutablePopup with null parameters.");
        }
        
        this.parent = parent;
        child = new JPanel();
        child.setLayout(new BoxLayout(child, BoxLayout.Y_AXIS));
        child.setBorder(BorderFactory.createRaisedBevelBorder());

        if (panelOption.isPresent()) {
            setContent(panelOption.get());
        }
    }

    /*
     * shows the underlying pop-up.
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
        Popup popup = factory.getPopup(parent, child, x, y);
        popupOption = Optional.of(popup);
        popup.show();
    }

    /*
     * hide hides the pop-up.
     *
     * Input: no input.
     * Output: no return value, hides the pop-up.
     */
    public void hide() {
        if (popupOption.isPresent()) {
            popupOption.get().hide();
        }
    }

    /*
     * setContent sets the content of the underlying pop-up, without instantiating
     * another pop-up.
     *
     * Input: JPanel containing desired content.
     * Output: no return value, modifies the child of the pop-up.
     */
    public void setContent(JPanel panel) {
        if (panel == null) {
            throw new NullPointerException("Cannot set content with null panel.");
        }
        ArionDisplay.makeDisplayReady(panel);
        child.removeAll();
        ArionDisplay.addPanelComponent(child, panel, ArionDisplay.MAX_DIMENSION,
                ArionDisplay.Format.NESTED_PANEL, false);
        child.setVisible(true);
        child.validate();
    }
}
