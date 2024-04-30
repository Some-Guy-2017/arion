// Written by - Joseph Hare
// Thursday April 25 2024

/*
 * ArionDisplay contains all the functionality for the Arion GUI.
 */

package arion;

import callback.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class ArionDisplay
{
    private class FontSize
    {
        public final static float HEADER_SIZE = 25f;
        public final static float PARAGRAPH_SIZE = 17f;
        //public final static float PARAGRAPH_SIZE = 5f;
    }
    
    final static int MARGIN_SIZE = 20;
    final static int COMPONENT_PADDING = 10;
    final static int HEADER_PADDING = 20;
    final static int TEXT_FIELD_WIDTH = 50;
    final static int TEXT_AREA_HEIGHT = 20;

    final static Dimension BUTTON_SIZE = new Dimension(300, 40);
    
    JFrame frame;

    private int width;
    private int height;
    
    public ArionDisplay(String title, int width, int height)
    {
        if (width < 0 || height < 0)
        {
            System.out.println("WARNING: Could not construct JFrame because of invalid width and height values.");
            System.out.println("WARNING: Setting width and height values to 0.");
            width = 0;
            height = 0;
        }

        if (title == null)
        {
            System.out.println("WARNING: Title not provided; setting title to empty string.");
            title = "";
        }

        this.width = width;
        this.height = height;

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
    }

    public void displayMenuBar(String[] menuTitles, String[][] actions, Runnable[][] callbacks)
    {
        if (menuTitles == null || actions == null || callbacks == null)
        {
            displayMessage("displayMenuBar parameters are null.", "MenuBar Error");
            return;
        }
        
        if (menuTitles.length != actions.length || menuTitles.length != callbacks.length)
        {
            displayMessage("Could not construct menu because menu titles, actions, and callbacks are not equally sized.", "MenuBar Error");
            return;
        }
        int menuCount = menuTitles.length;    

        if (menuCount == 0)
        {
            displayMessage("Menu is empty, so it will not render.", "MenuBar Empty");
        }

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar); 
        
        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++)
        {
            String menuActions[] = actions[menuIdx];
            Runnable[] menuCallbacks = callbacks[menuIdx];
            
            if (menuActions.length != menuCallbacks.length)
            {
                displayMessage("Could not construct menu because menu actions and callbacks are different lengths at index " + menuIdx + ".", "MenuBar Error");
                return;
            }
            
            JMenu menu = new JMenu(menuTitles[menuIdx]);
            menuBar.add(menu);
            
            int actionCount = menuActions.length;
            for (int actionIdx = 0; actionIdx < actionCount; actionIdx++)
            {
                String action = menuActions[actionIdx];
                JMenuItem menuItem = new JMenuItem(action);
                menu.add(menuItem);
                
                Runnable callback = menuCallbacks[actionIdx];
                menuItem.addActionListener((ActionEvent e) -> callback.run());
            }
        }
    }

    // makes the component ready for displaying;
    // it aligns the component in the center, and sets the font style and size
    private void makeDisplayReady(JComponent component, int fontStyle, float fontSize)
    {
        component.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        component.setFont(component.getFont().deriveFont(fontStyle, fontSize));
        component.setMaximumSize(component.getPreferredSize());
    }

    private JButton generateButton(String text)
    {
        JButton button = new JButton(text);
        button.setPreferredSize(BUTTON_SIZE);
        makeDisplayReady(button, Font.PLAIN, FontSize.PARAGRAPH_SIZE);
        return button;
    }

    public void displayMainScreen(Runnable addCallback, Runnable studyCallback)
    { 
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel label = new JLabel("Main Screen");
        makeDisplayReady(label, Font.BOLD, FontSize.HEADER_SIZE);
        
        JButton studyButton = generateButton("Study");
        studyButton.addActionListener((ActionEvent e) -> studyCallback.run());
        
        JButton addButton = generateButton("Add");
        addButton.addActionListener((ActionEvent e) -> addCallback.run());
        
        addPanelComponent(panel, label, HEADER_PADDING);
        addPanelComponent(panel, studyButton, COMPONENT_PADDING);
        addPanelComponent(panel, addButton, COMPONENT_PADDING);
        panel.add(Box.createGlue());
            
        setFrameContent(panel);

    }

    private void addPanelComponent(JPanel panel, JComponent component, int padding)
    {
        panel.add(component);
        panel.add(Box.createRigidArea(new Dimension(0, padding)));
    }

    /*
    // sets the component's font to be proportional to its size
    // code modified based on:
    // https://stackoverflow.com/questions/19989683/using-an-affline-transformation-to-set-font-size-to-be-proportional-to-its-conta
    private void setProportionalFont(JComponent component, String text, Dimension boundingSize)
    {
        Font font = component.getFont();
        FontMetrics fontMetrics = component.getFontMetrics(font);

        int fontWidth = fontMetrics.stringWidth(text);
        int fontHeight = fontMetrics.getHeight();
        double xscale = boundingSize.getWidth()  / fontWidth;
        double yscale = boundingSize.getHeight() / fontHeight;

        double scale = 0.0;
        if (xscale < yscale) scale = xscale;
        else                 scale = yscale;
        scale *= FONT_PROPORTION;

        Font newFont = font.deriveFont(AffineTransform.getScaleInstance(scale, scale));
        component.setFont(newFont);

    }

    private void setProportionalFont(JComponent component, String text)
    {
        Dimension componentSize = new Dimension();
        component.getSize(componentSize);
        setProportionalFont(component, text, componentSize);
    }
    */

    private void setFrameContent(JPanel panel)
    {
        // add margin
        panel.setBorder(BorderFactory.createEmptyBorder(
            MARGIN_SIZE,
            MARGIN_SIZE,
            MARGIN_SIZE,
            MARGIN_SIZE
        ));
        
        Container contentPane = frame.getContentPane();
        contentPane.removeAll();
        contentPane.add(panel);
        
        frame.validate();
        frame.setVisible(true);
    }
    
    public void displayBrowseScreen(ArrayList<Flashcard> flashcards, EditCallback editCallback, DeleteCallback deleteCallback)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel header = new JLabel("Browse");
        makeDisplayReady(header, Font.BOLD, FontSize.HEADER_SIZE);
        addPanelComponent(panel, header, HEADER_PADDING);

        String[] columnNames = Flashcard.fieldNames;
        String[][] tableData = new String[flashcards.size()][];
        for (int i = 0; i < flashcards.size(); i++) tableData[i] = flashcards.get(i).toStringArray();
        JTable table = new JTable(tableData, columnNames);

        JScrollPane scrollPane = new JScrollPane(
            table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension prevSize = scrollPane.getMaximumSize();
        makeDisplayReady(scrollPane, Font.PLAIN, FontSize.PARAGRAPH_SIZE);
        addPanelComponent(panel, scrollPane, COMPONENT_PADDING);
        scrollPane.setMaximumSize(prevSize);

        panel.add(Box.createGlue());
        setFrameContent(panel);
    }

    private JScrollPane makeTextAreaDisplayReady(JTextArea textArea, int width)
    {
        textArea.setRows(TEXT_AREA_HEIGHT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        makeDisplayReady(textArea, Font.PLAIN, FontSize.PARAGRAPH_SIZE);
        
        JScrollPane scrollPane = new JScrollPane(
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension size = new Dimension(width, textArea.getPreferredSize().height);
        scrollPane.setMaximumSize(size);

        return scrollPane;
    }

    public void displayAddScreen(AddCallback callback)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel header = new JLabel("Add");
        makeDisplayReady(header, Font.BOLD, FontSize.HEADER_SIZE);
        addPanelComponent(panel, header, HEADER_PADDING);
        
        JLabel frontLabel = new JLabel("Front");
        makeDisplayReady(frontLabel, Font.PLAIN, FontSize.PARAGRAPH_SIZE);
        addPanelComponent(panel, frontLabel, COMPONENT_PADDING);
        
        JTextField frontTextField = new JTextField(TEXT_FIELD_WIDTH);
        makeDisplayReady(frontTextField , Font.PLAIN, FontSize.PARAGRAPH_SIZE);
        addPanelComponent(panel, frontTextField, COMPONENT_PADDING);

        JLabel backLabel = new JLabel("Back");
        makeDisplayReady(backLabel, Font.PLAIN, FontSize.PARAGRAPH_SIZE);
        addPanelComponent(panel, backLabel, COMPONENT_PADDING);
        
        // add back text area, surrounded in scroll pane
        JTextArea backTextArea = new JTextArea();
        JScrollPane backScrollPane = makeTextAreaDisplayReady(backTextArea, frontTextField.getPreferredSize().width);
        addPanelComponent(panel, backScrollPane, COMPONENT_PADDING);
        
        JButton addButton = generateButton("Add Flashcard");
        addButton.addActionListener((ActionEvent e) -> {
            String[] fields = { frontTextField.getText(), backTextArea.getText() };
            callback.run(fields);
        });
        addPanelComponent(panel, addButton, COMPONENT_PADDING);

        panel.add(Box.createGlue());
        setFrameContent(panel);
    }
    
    public void displaySortScreen(SortCallback sortCallback)
    {
        System.out.println("TODO: Display sort screen.");
    }

    public void displayGuidePage(int page)
    {
        System.out.println("TODO: Display guide page.");
    }
    
    public void displayAboutScreen()
    {
        System.out.println("TODO: Display about screen.");
    }

    public void displayMessage(String message, String title)
    {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // used for debugging purposes; sets the display to be visible, without necessarily requiring content
    public void _setVisible()
    {
        frame.setVisible(true);
    }
    
}
