// Written by - Joseph Hare
// Thursday April 25 2024

/*
 * ArionDisplay contains all the functionality for the Arion GUI.
 */

package arion;

import arion.Flashcard;
import callback.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.text.Format;
import java.util.ArrayList;

public class ArionDisplay {
    JFrame frame;

    private int width;
    private int height;

    private Runnable addCallback;
    private Runnable studyCallback;
    
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
        
        final static int MARGIN_SIZE = 20;
        final static int TEXT_BOX_WIDTH = 50;
        final static int TEXT_AREA_ROWS = 20;
        final static int TABLE_CELL_PADDING = 2;
        final static Dimension BUTTON_SIZE = new Dimension(300, 40);
    }

    private EditCallback editCallback;
    private DeleteCallback deleteCallback;
    private ArrayList<Flashcard> browseFlashcards;
    
    public ArionDisplay(String title, int width, int height) {

        if (width < 0 || height < 0) {
            System.out.println("WARNING: Cannot construct JFrame because of invalid width and height values.");
            System.out.println("WARNING: Setting width and height values to 0.");
            width = 0;
            height = 0;
        }

        if (title == null) {
            System.out.println("WARNING: Title not provided; setting title to empty string.");
            title = "";
        }

        this.width = width;
        this.height = height;

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
            throw new IllegalArgumentException("Cannot construct menu because menu titles, actions, and callbacks are not equally sized.");
        
        int menuCount = menuTitles.length;    
        if (menuCount == 0)
            System.out.println("WARNING: Menu is empty, so it will not render.");

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar); 
        
        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++) {
            String menuActions[] = actions[menuIdx];
            Runnable[] menuCallbacks = callbacks[menuIdx];
            
            if (menuActions.length != menuCallbacks.length)
                throw new IllegalArgumentException("Cannot construct menu because menu actions and callbacks are different lengths at index " + menuIdx + ".");
            
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
        
        JPanel panel = generateMainPanel("Main Screen");
        
        JButton studyButton = generateButton("Study");
        studyButton.addActionListener(generateActionListener(studyCallback));
        addPanelComponent(panel, studyButton, Format.COMPONENT, false);
        
        JButton addButton = generateButton("Add");
        addButton.addActionListener(generateActionListener(addCallback));
        addPanelComponent(panel, addButton, Format.COMPONENT, true);
        
        setFrameContent(panel);
    }

    public void displayBrowseScreen(ArrayList<Flashcard> flashcards, EditCallback editCallback, DeleteCallback deleteCallback) {
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
        
        JPanel panel = generateMainPanel("Add");
        
        JLabel frontLabel = new JLabel("Front");
        addPanelComponent(panel, frontLabel, Format.H2, false);
        
        JTextField frontTextField = new JTextField(Format.TEXT_BOX_WIDTH);
        addPanelComponent(panel, frontTextField, Format.COMPONENT, false);

        JLabel backLabel = new JLabel("Back");
        addPanelComponent(panel, backLabel, Format.H2, false);
        
        int backWidth = frontTextField.getPreferredSize().width; // make the back and front text boxes the same width
        JScrollPane backTextArea = generateTextArea(backWidth);
        addPanelComponent(panel, backTextArea, Format.COMPONENT, false);
        
        JButton addButton = generateButton("Add Flashcard");
        addButton.addActionListener(generateActionListener(() -> {
            String[] fields = { frontTextField.getText(), ((JTextArea) backTextArea.getViewport().getView()).getText() };
            callback.run(fields);
            reenterMainScreen();
        }));
        addPanelComponent(panel, addButton, Format.COMPONENT, true);

        setFrameContent(panel);
    }

    public void displayStudyScreen(Flashcard flashcard, boolean front, ReviewCallback reviewCallback) {
        if (flashcard == null || reviewCallback == null)
            throw new NullPointerException("Could not display study screen because passed parameter is null.");
        
        JPanel panel = generateMainPanel("Study");
        
        String text;
        if (front) text = flashcard.front;
        else text = flashcard.back;
        
        JLabel textLabel = new JLabel(text);
        addPanelComponent(panel, textLabel, Format.STUDY_TEXT, false);

        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.add(Box.createGlue());

        if (front) {
            JButton flipButton = generateButton("Flip");
            addPanelComponent(bar, flipButton, Format.COMPONENT, true);

            flipButton.addActionListener((ActionEvent e) -> displayStudyScreen(flashcard, false, reviewCallback));
        }
        else {
            JButton correctButton = generateButton("Correct");
            addPanelComponent(bar, correctButton, Format.COMPONENT, false);
            correctButton.addActionListener((ActionEvent e) -> reviewCallback.run(true));
            
            JButton incorrectButton = generateButton("Incorrect");
            addPanelComponent(bar, incorrectButton, Format.COMPONENT, true);
            incorrectButton.addActionListener((ActionEvent e) -> reviewCallback.run(false));
        }
        
        bar.add(Box.createGlue());

        panel.add(Box.createVerticalGlue());
        addPanelComponent(panel, bar, Format.H1, true);

        setFrameContent(panel);
    }
    
    public void displaySortScreen(SortCallback sortCallback) {
        JPanel panel = generateMainPanel("Sort");

        JComboBox fieldSelector = new JComboBox<Flashcard.Field>(Flashcard.FIELDS);
        
        String[] directions = {"Forwards", "Backwards"};
        JComboBox directionSelector = new JComboBox<String>(directions);
        
        JComboBox[] comboBoxes = {
            fieldSelector,
            directionSelector,
        };
        
        int maxWidth = -1;
        for (JComboBox comboBox : comboBoxes) {
            int width = comboBox.getPreferredSize().width;
            if (width > maxWidth) maxWidth = width;
        }

        for (JComboBox comboBox : comboBoxes) {
            int height = comboBox.getPreferredSize().height;
            comboBox.setPreferredSize(new Dimension(maxWidth, height));
            addPanelComponent(panel, comboBox, Format.COMPONENT, false);
        }

        JButton confirmButton = generateButton("Sort");
        confirmButton.addActionListener((ActionEvent e) -> {
            Flashcard.Field field = (Flashcard.Field) fieldSelector.getSelectedItem();
            boolean reversed = directionSelector.getSelectedIndex() == 1; // reversing direction is index 1
            sortCallback.run(field, reversed);
            reenterMainScreen();
        });
        addPanelComponent(panel, confirmButton, Format.COMPONENT, true);

        setFrameContent(panel);
    }

    public void displayGuidePage(int page) {
        System.out.println("TODO: Display guide page.");
    }
    
    public void displayAboutScreen() {
        System.out.println("TODO: Display About screen.");
    }

    public void displayWarningMessage(String message) {
        if (message == null)
            throw new NullPointerException("Could not display warning message because message is null.");
        
        displayMessage("WARNING: " + message, "Warning Message");
    }
    
    public void displayMessage(String message) {
        if (message == null) throw new NullPointerException("Could not display message because parameter is null.");
        displayMessage(message, "");
    }

    public void displayMessage(String message, String title) {
        if (message == null || title == null)
            throw new NullPointerException("Could not display message because message or title is null.");
        
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void alert(String message) {
        if (message == null) throw new NullPointerException("Could not alert because message is null.");
        JOptionPane.showMessageDialog(null, message);
    }

    public boolean displayConfirmationWindow(String message, String title) {
        return JOptionPane.showConfirmDialog(frame, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void quit() {
        frame.setVisible(false);
        frame.dispose();
    }

    public void displaySuccessScreen() {
        JPanel panel = generateMainPanel("Congratulations!");
        
        JLabel text = new JLabel("All due flashcards have been studied.");
        addPanelComponent(panel, text, Format.COMPONENT, false);

        JButton returnButton = generateButton("Return to Main Screen");
        returnButton.addActionListener((ActionEvent e) -> reenterMainScreen());
        addPanelComponent(panel, returnButton, Format.COMPONENT, true);

        setFrameContent(panel);
    }

    private JPanel generateMainPanel(String screenName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel header = new JLabel(screenName);
        addPanelComponent(panel, header, Format.H1, false);
        
        return panel;
    }
    
    private void setFrameContent(JPanel panel) {
        
        // add margin
        panel.setBorder(BorderFactory.createEmptyBorder(
            Format.MARGIN_SIZE,
            Format.MARGIN_SIZE,
            Format.MARGIN_SIZE,
            Format.MARGIN_SIZE
        ));
        
        Container contentPane = frame.getContentPane();
        contentPane.removeAll();
        contentPane.add(panel);
        
        frame.validate();
    }

    private JScrollPane generateTable(String[] columnNames, String[][] data) {
        JTable table = new JTable(data, columnNames);
        setFont(table.getTableHeader(), Format.H2); // set table header font
        setFont(table, Format.COMPONENT);           // set table content font
        
        // set row height to match font size and text padding
        table.setRowHeight((int) Format.COMPONENT.fontSize + Format.TABLE_CELL_PADDING * 2);

        // wrap the table in a scroll pane, in case it exceeds the size of the panel
        JScrollPane scrollPane = new JScrollPane(
            table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        return scrollPane;
    }
    
    private JScrollPane generateTextArea(int width) {
        JTextArea textArea = new JTextArea();
        textArea.setRows(Format.TEXT_AREA_ROWS); // set the appropriate number of text rows
        textArea.setLineWrap(true);              // wrap lines when they exceed the text box length
        textArea.setWrapStyleWord(true);         // wrap words - do not wrap text mid-word
        setFont(textArea, Format.COMPONENT);
        
        // wrap the text area in a scroll pane, since the text can exceed the text area dimensions
        JScrollPane scrollPane = new JScrollPane(
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension newSize = scrollPane.getPreferredSize();
        newSize.width = width;
        scrollPane.setPreferredSize(newSize);

        return scrollPane;
    }

    private JButton generateButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(Format.BUTTON_SIZE);
        return button;
    }

    private void setFont(JComponent component, Style style) {
        Font newFont = component.getFont().deriveFont(style.fontStyle, style.fontSize);
        component.setFont(newFont);
    }
    
    private void addPanelComponent(JPanel panel, JComponent component, Style style, boolean lastComponent) {
        
        // null dimension signifies using preferred size after font adjustments are made (these can change preferred size)
        addPanelComponent(panel, component, null, style, lastComponent);
    }
    
    // add a component to the panel, but allow for deciding of its maximum size
    private void addPanelComponent(JPanel panel, JComponent component, Dimension maxSize, Style style, boolean lastComponent) {
        
        // format the component properly for displaying; center it, set its font, and force the layout manager to use the preferred size
        component.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        component.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        setFont(component, style);
        
        if (maxSize == null) maxSize = component.getPreferredSize();
        component.setMaximumSize(maxSize);
        
        panel.add(component);
        if (!lastComponent) panel.add(Box.createRigidArea(new Dimension(style.padding, style.padding)));
    }
     
    private void renderBrowseScreen() {
        JPanel panel = generateMainPanel("Browse");
        
        String[] columnNames = Flashcard.FIELD_TITLES;
        String[][] tableData = new String[browseFlashcards.size()][];
        for (int i = 0; i < browseFlashcards.size(); i++)
            tableData[i] = browseFlashcards.get(i).toStringArray();

        JScrollPane tableScrollPane = generateTable(columnNames, tableData);
        JTable table = (JTable) tableScrollPane.getViewport().getView();
        Dimension tableSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); // allow the table to be arbitrarily large
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        // track the previous data to only update changed browseFlashcards
        String[][] prevData = new String[tableData.length][];
        for (int i = 0; i < tableData.length; i++) prevData[i] = tableData[i].clone();
        
        JButton updateButton = generateButton("Update Flashcards");
        updateButton.addActionListener(generateActionListener(() -> {
            sendUpdatedFlashcards(tableData, prevData, editCallback);
            renderBrowseScreen(); // re-render the screen with the updated browseFlashcards
        }));
        addPanelComponent(buttonPanel, updateButton, Format.COMPONENT, false);
        
        JButton deleteButton = generateButton("Delete Flashcard");
        deleteButton.addActionListener(generateActionListener(() -> {
            boolean confirmation = displayConfirmationWindow("Delete Flashcards?", "Delete Confirmation");
            if (confirmation) deleteCallback.run(table.getSelectedRows());
            renderBrowseScreen(); // re-render the screen with the updated browseFlashcards
        }));
        addPanelComponent(buttonPanel, deleteButton, Format.COMPONENT, true);
        
        addPanelComponent(panel, tableScrollPane, tableSize, Format.COMPONENT, false);
        addPanelComponent(panel, buttonPanel, Format.COMPONENT, true);
        setFrameContent(panel);
    }

    private void sendUpdatedFlashcards(String[][] tableData, String[][] prevData, EditCallback editCallback) {
        if (tableData.length != prevData.length)
            throw new IllegalArgumentException("Cannot update flashcards because data is improperly sized.");
        
        for (int i = 0; i < tableData.length; i++) {
            boolean tableValid = tableData[i].length == Flashcard.FIELD_COUNT;
            boolean prevValid = prevData[i].length == Flashcard.FIELD_COUNT;
            
            if (!tableValid | !prevValid)
                throw new IllegalArgumentException("Cannot update flashcard at index " + i + " because the data is improperly sized.");
            
            for (int j = 0; j < Flashcard.FIELD_COUNT; j++) {
                if (!tableData[i][j].equals(prevData[i][j])) editCallback.run(i, tableData[i]);
            }
            prevData[i] = tableData[i].clone(); // update the previous data
        }
    }

    private ActionListener generateActionListener(Runnable onRun) {
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
}
