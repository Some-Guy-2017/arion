// Written by - Joseph Hare
// Thursday April 25 2024

/*
 * ArionDisplay contains all the functionality for the Arion GUI.
 */

package arion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import callback.*;

public class ArionDisplay
{
    private class FontSize
    {
        public final static float HEADER_SIZE = 25f;
        public final static float PARAGRAPH_SIZE = 17f;
    }
    
    final static int MARGIN_SIZE = 20;
    final static int COMPONENT_PADDING = 10;
    final static int HEADER_PADDING = 20;

    final static Dimension BUTTON_SIZE = new Dimension(300, 40);
    
    JFrame frame;
    
    public ArionDisplay(String title, int width, int height)
    {
        if (width < 0 || height < 0)
        {
            printWarning("Could not construct JFrame because of invalid width and height values.");
            printWarning("Setting width and height values to 0.");
            width = 0;
            height = 0;
        }

        if (title == null)
        {
            printWarning("Title not provided; setting title to empty string.");
            title = "";
        }

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
    }

    public void displayMenuBar(String[] menuTitles, String[][] actions, Runnable[][] callbacks)
    {
        if (menuTitles == null || actions == null || callbacks == null)
        {
            printWarning("displayMenuBar parameters are null.");
            return;
        }
        
        if (menuTitles.length != actions.length || menuTitles.length != callbacks.length)
        {
            printWarning("Could not construct menu because menu titles, actions, and callbacks are not equally sized.");
            return;
        }
        int menuCount = menuTitles.length;    

        if (menuCount == 0)
        {
            printWarning("Menu is empty, so it will not render.");
        }

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar); 
        
        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++)
        {
            String menuActions[] = actions[menuIdx];
            Runnable[] menuCallbacks = callbacks[menuIdx];
            
            if (menuActions.length != menuCallbacks.length)
            {
                printWarning("Could not construct menu because menu actions and callbacks are different lengths at index " + menuIdx + ".");
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

    // aligns the component in the center, and sets the font size
    private void makeDisplayReady(JComponent component, int fontStyle, float fontSize)
    {
        component.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        component.setFont(component.getFont().derivefont(fontStyle, fontSize));
    }

    private JButton generateButton(String text)
    {
        JButton button = new JButton(text);
        button.setPreferredSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);
        makeDisplayReady(button, Font.PLAIN, FontSize.PARAGRAPH_SIZE);
        return button;
    }

    public void displayMainScreen(Runnable addCallback, Runnable studyCallback)
    { 
        JPanel panel = new JPanel();
        setFrameContent(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JButton studyButton = generateButton("Study");
        JButton addButton = generateButton("Add");

        JLabel label = new JLabel("Main Screen");
        makeDisplayReady(label, FontSize.HEADER_SIZE);
        
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
    
    public void displayBrowseScreen(Flashcard[] flashcards, EditCallback editCallback, DeleteCallback deleteCallback)
    {
        System.out.println("TODO: Display browse screen.");
    }

    public void displayAddScreen(AddCallback callback)
    {
        System.out.println("TODO: Display add screen.");
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

    private void printWarning(String text)
    {
        System.err.println("WARNING: " + text);
    }
}
