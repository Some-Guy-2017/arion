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
    private final static Dimension MAX_DIMENSION = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

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
        public static void setGuideFilepath(String guideFilepath) {
            guidePages = parseGuidePages(guideFilepath);
        }

        public static String getGuideFilepath() {
            return GUIDE_FILEPATH;
        }

        public void displayGuidePageWithExceptions(int pageNum) throws GuideDisplayException {
            displayGuidePageRoutine(pageNum);
        }
    }

    public _TestHooks _testHooks = new _TestHooks();

    public ArionDisplay(String title, int width, int height) {

        if (width < 0 || height < 0) {
            String message = "Cannot construct JFrame with invalid width and height values." + "\n"
                    + "Setting width and height values to 0.";
            Arion.displayWarningMessage(message);
        }

        if (title == null) {
            Arion.displayWarningMessage("Title not provided; setting title to empty string.");
            title = "";
        }

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void displayMenuBar(String[] menuTitles, String[][] actions, Runnable[][] callbacks) {

        if (menuTitles == null || actions == null || callbacks == null)
            throw new NullPointerException("Cannot construct menu because received a null parameter.");
        if (menuTitles.length != actions.length || menuTitles.length != callbacks.length)
            throw new IllegalArgumentException(
                    "Cannot construct menu because menu titles, actions, and callbacks are not equally sized.");

        int menuCount = menuTitles.length;
        if (menuCount == 0)
            System.out.println("WARNING: Menu is empty, so it will not render.");

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

    public void displayBrowseScreen(ArrayList<Flashcard> flashcards, EditCallback editCallback,
            DeleteCallback deleteCallback) {
        if (flashcards == null || editCallback == null || deleteCallback == null)
            throw new NullPointerException("Could not display browse screen because passed callbacks are null.");

        this.browseFlashcards = flashcards;
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
        renderBrowseScreen();
    }

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

    public void displayStudyScreen(Flashcard flashcard, boolean front, ReviewCallback reviewCallback) {
        if (flashcard == null || reviewCallback == null)
            throw new NullPointerException("Could not display study screen because passed parameter is null.");

        JPanel panel;
        try {
            panel = generateMainPanel("Study", true);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }

        String text;
        if (front)
            text = flashcard.front;
        else
            text = flashcard.back;

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

    public void displayGuidePage(int pageNum) {
        try {
            displayGuidePageRoutine(pageNum);
        } catch (GuideDisplayException e) {
            Arion.displayException(e);
        }
    }

    public void displayAboutScreen() {
        JPanel panel;
        try {
            panel = generateMainPanel("About", false);
        } catch (MainPanelDisplayException e) {
            Arion.displayException(e);
            return;
        }
        panel.setPreferredSize(getPopupSize());
        Popup popup = generatePopup(panel);

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
            Arion.displayWarningMessage("Cannot display About window because could not find " + ABOUT_FILEPATH);
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
        addPanelComponent(panel, scrollPane, MAX_DIMENSION, Format.POPUP, true);

        JButton closeButton = generatePopupButton("Close");
        closeButton.addActionListener(generateActionListener(() -> {
            popup.hide();
        }));
        addPanelComponent(panel, closeButton, Format.COMPONENT, false);

        popup.show();
        // setFrameContent(panel);
    }

    public static void alert(String message) {
        if (message == null) {
            throw new NullPointerException("Could not alert because message is null.");
        }
        JOptionPane.showMessageDialog(null, message);
    }

    public boolean displayConfirmationWindow(String message, String title) {
        if (message == null || title == null)
            throw new NullPointerException("Cannot display confirmation window with null parameters.");

        return JOptionPane.showConfirmDialog(frame, message, title,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void quit() {
        frame.setVisible(false);
        frame.dispose();
    }

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

    private static void makeDisplayReady(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(
                Format.MARGIN_SIZE,
                Format.MARGIN_SIZE,
                Format.MARGIN_SIZE,
                Format.MARGIN_SIZE));
    }

    private void setFrameContent(JPanel panel) {
        makeDisplayReady(panel);

        Container contentPane = frame.getContentPane();
        contentPane.removeAll();
        contentPane.add(panel);

        frame.validate();
    }

    private JTable generateTable(String[] columnNames, String[][] data) {
        JTable table = new JTable(data, columnNames);
        setFont(table.getTableHeader(), Format.H2); // set table header font
        setFont(table, Format.COMPONENT); // set table content font

        // set row height to match font size and text padding
        table.setRowHeight((int) Format.COMPONENT.fontSize + Format.TABLE_CELL_PADDING * 2);

        return table;
    }

    private JTextArea generateTextArea() {
        JTextArea textArea = new JTextArea();
        formatTextArea(textArea);
        return textArea;
    }

    private static JTextArea generateTextArea(String text) {
        JTextArea textArea = new JTextArea(text);
        formatTextArea(textArea);
        return textArea;
    }

    private static void formatTextArea(JTextArea textArea) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        setFont(textArea, Format.COMPONENT);
    }

    private static JScrollPane scrollWrap(Component component) {
        return new JScrollPane(
                component,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JButton generateButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(Format.BUTTON_SIZE);
        return button;
    }

    private JButton generatePopupButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(Format.POPUP_BUTTON_SIZE);
        return button;
    }

    private static Font deriveFont(JComponent component, Style style) {
        return component.getFont().deriveFont(style.fontStyle, style.fontSize);
    }

    private static void setFont(JComponent component, Style style) {
        component.setFont(deriveFont(component, style));
    }

    private static void addPanelComponent(JPanel panel, JComponent component, Style style, boolean addPadding) {
        _addPanelComponent(panel, component, Optional.empty(), style, addPadding);
    }

    private static void addPanelComponent(JPanel panel, JComponent component, Dimension maxSize, Style style,
            boolean addPadding) {
        _addPanelComponent(panel, component, Optional.of(maxSize), style, addPadding);
    }

    // add a component to the panel, but allow for deciding of its maximum size
    // not for general use; use the specific function signature, not Optionals
    private static void _addPanelComponent(JPanel panel, JComponent component, Optional<Dimension> maxSizeOption,
            Style style, boolean addPadding) {

        // format the component properly for displaying; center it, set its font, and
        // force the layout manager to use the preferred size
        component.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        component.setAlignmentY(JComponent.TOP_ALIGNMENT);
        setFont(component, style);

        component.setMaximumSize(maxSizeOption.orElse(component.getPreferredSize()));
        panel.add(component);
        if (addPadding) {
            panel.add(Box.createRigidArea(new Dimension(style.padding, style.padding)));
        }
    }

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
        // scroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        // // allow the table to be
        // // arbitrarily large
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
                if (!tableData[i][j].equals(prevData[i][j]))
                    editCallback.run(i, tableData[i]);
            }
            prevData[i] = tableData[i].clone(); // update the previous data
        }
    }

    private static ActionListener generateActionListener(Runnable onRun) {
        return (ActionEvent e) -> {
            try {
                onRun.run();
            } catch (Exception exception) {
                Arion.displayException(exception);
            }
        };
    }

    private void reenterMainScreen() {
        displayMainScreen(addCallback, studyCallback);
    }

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

    private JPanel generateGuidePanel(int pageNum) throws GuideDisplayException {

        Node[] pages = guidePages.orElseThrow(() -> new GuideDisplayException("Guide is not available."));
        Node page = pages[pageNum];
        String pageName = getNodeAttribute(page, "name")
                .orElseThrow(() -> new GuideDisplayException("Page has no name."));

        JPanel panel;
        try {
            panel = generateMainPanel(pageName, false);
        } catch (MainPanelDisplayException e) {
            throw new GuideDisplayException(e.getMessage());
        }

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
        return panel;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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

    // in getScaledInstance, -1 indicates no dimension is provided

    /*
     * private static void addPanelImage(JPanel panel, BufferedImage img, boolean
     * lastComponent) {
     * addPanelImage(panel, img, -1, -1, lastComponent);
     * }
     * 
     * private static void addPanelImageWithWidth(JPanel panel, BufferedImage img,
     * int width, boolean lastComponent) {
     * addPanelImage(panel, img, width, -1, lastComponent);
     * }
     * 
     * private static void addPanelImageWithHeight(JPanel panel, BufferedImage img,
     * int height, boolean lastComponent) {
     * addPanelImage(panel, img, -1, height, lastComponent);
     * }
     */

    private Popup generatePopup(JPanel panel) {
        makeDisplayReady(panel);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(BorderFactory.createRaisedBevelBorder());
        addPanelComponent(wrapper, panel, MAX_DIMENSION, Format.NESTED_PANEL, false);

        int x = (frame.getWidth() - wrapper.getPreferredSize().width) / 2;
        x += frame.getX();
        int y = (frame.getHeight() - wrapper.getPreferredSize().height) / 2;
        y += frame.getY();

        PopupFactory factory = PopupFactory.getSharedInstance();
        return factory.getPopup(frame, wrapper, x, y);
    }

    private void displayGuidePageRoutine(int pageNum) throws GuideDisplayException {
        if (guidePages.isEmpty())
            throw new GuideDisplayException("Cannot display guide because it does not exist.");
        int pageCount = guidePages.get().length;

        if (pageNum < 0) {
            throw new GuideDisplayException("Cannot display guide page " + pageNum + " because it is less than 0.");
        }
        if (pageNum >= pageCount) {
            throw new GuideDisplayException(
                    "Cannot display guide page " + pageNum + " because there are only " + pageCount + " pages.");
        }

        boolean prevPage = (pageNum - 1 >= 0);
        boolean nextPage = (pageNum + 1 < pageCount);

        JPanel panel = generateGuidePanel(pageNum);
        Popup popup = generatePopup(panel);

        GridLayout layout = new GridLayout(1, 3);
        JPanel buttonBar = new JPanel(layout);
        int buttonCount = 0;

        JButton prevButton = generatePopupButton("Previous");
        prevButton.addActionListener(generateActionListener(() -> {
            popup.hide();
            displayGuidePage(pageNum - 1);
        }));
        if (!prevPage)
            prevButton.setEnabled(false);
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
            popup.hide();
            displayGuidePage(pageNum + 1);
        }));
        if (!nextPage)
            nextButton.setEnabled(false);
        addPanelComponent(buttonBar, nextButton, Format.COMPONENT, false);
        buttonCount++;

        setPreferredWidth(buttonBar, getPopupSize().width);
        addPanelComponent(panel, buttonBar, Format.NESTED_PANEL, false);

        popup.show();
    }

    private static Optional<String> getNodeAttribute(Node node, String attribute) {
        Node attributeNode = node.getAttributes().getNamedItem(attribute);
        if (attributeNode == null)
            return Optional.empty();
        else
            return Optional.of(attributeNode.getNodeValue());
    }

    private static Node[] getRealChildren(Node parent) {
        NodeList children = parent.getChildNodes();
        ArrayList<Node> realChildren = new ArrayList<>();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.COMMENT_NODE)
                continue;

            realChildren.add(child);
        }

        return realChildren.toArray(new Node[0]);
    }

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

    private static double parseScale(Optional<String> scaleOption) throws GuideDisplayException {
        if (!scaleOption.isPresent())
            return 1;

        try {
            double scale = Double.valueOf(scaleOption.get()).doubleValue();
            return Math.min(scale, 1);
        } catch (NumberFormatException e) {
            throw new GuideDisplayException("Attempting to parse invalid scale value.");
        }
    }

    private static void setPreferredWidth(JComponent component, int width) {
        Dimension size = component.getPreferredSize();
        size.width = width;
        component.setPreferredSize(size);
    }

    private static void setPreferredHeight(JComponent component, int height) {
        Dimension size = component.getPreferredSize();
        size.height = height;
        component.setPreferredSize(size);
    }

    private static int getColumnWidth(JComponent component, Style style) {
        Font font = deriveFont(component, style);
        FontMetrics metrics = component.getFontMetrics(font);
        return metrics.charWidth('m');
    }

    private Dimension getPopupSize() {
        int width = (int) (frame.getWidth() / Format.POPUP_WINDOW_RATIO);
        int height = (int) (frame.getHeight() / Format.POPUP_WINDOW_RATIO);
        System.out.println(frame.getSize());
        return new Dimension(width, height);
    }

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
}

class SAXErrorHandler implements ErrorHandler {
    public void error(SAXParseException e) {
        Arion.displayException(e);
    }

    public void fatalError(SAXParseException e) {
        Arion.displayException(e);
    }

    public void warning(SAXParseException e) {
        Arion.displayException(e);
    }
}

class ExecuteRunnable extends AbstractAction {
    public Runnable run;

    public ExecuteRunnable(Runnable run) {
        this.run = run;
    }

    public void actionPerformed(ActionEvent e) {
        run.run();
    }
}
