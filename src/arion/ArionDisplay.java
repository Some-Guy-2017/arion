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
    JFrame frame;
    final static double FONT_PROPORTION = 0.5;
    final static int MARGIN_SIZE = 20;
    final static int COMPONENT_PADDING = 10;
    final static float BIG_FONT_SIZE = 20f;
    
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

    private JButton generateMainScreenButton(String text)
    {
        Dimension desiredSize = new Dimension(100, 40);

        JButton button = new JButton(text);
        button.setPreferredSize(desiredSize);
        button.setMaximumSize(desiredSize);
        button.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        setProportionalFont(button, button.getText(), desiredSize);
        return button;
    }

    public void displayMainScreen(Runnable addCallback, Runnable studyCallback)
    { 
        JButton studyButton = generateMainScreenButton("Study");
        JButton addButton = generateMainScreenButton("Add");

        JPanel panel = new JPanel();
        setFrameContent(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(studyButton);
        panel.add(Box.createRigidArea(new Dimension(0, COMPONENT_PADDING)));
        panel.add(addButton);
        panel.add(Box.createGlue());
            
        setFrameContent(panel);


            //JPanel panel = new JPanel(new GridBagLayout());
            //GridBagConstraints gbc = generateGBC();
            //gbc.fill = GridBagConstraints.BOTH; // resize components to match grid cell size
            //gbc.weightx = 0.0;
            //
            //gbc.weighty = 5.0;
            //gbc.gridx = 0;
            //gbc.gridy = 0;
            //addPanelPadding(panel, gbc);

            //gbc.weighty = 1.0;
            //gbc.gridx = 0;
            //gbc.gridy++;
            //JButton studyButton = new JButton("Study");
            //studyButton.addActionListener((ActionEvent e) -> studyCallback.run());
            //addPanelButton(panel, gbc, studyButton, 0.1);
            //
            //gbc.weighty = 5.0;
            //gbc.gridx = 0;
            //gbc.gridy++;
            //addPanelPadding(panel, gbc);
            //
            //gbc.weighty = 1.0;
            //gbc.gridx = 0;
            //gbc.gridy++;
            //JButton addButton = new JButton("Add");
            //addButton.addActionListener((ActionEvent e) -> addCallback.run());
            //addPanelButton(panel, gbc, addButton, 0.1);

            //gbc.weighty = 5.0;
            //gbc.gridx = 0;
            //gbc.gridy++;
            //addPanelPadding(panel, gbc);

            //setFrameContent(panel);

            //// this must be done after setting the frame content
            //// so the button sizes are known
            //setProportionalFont(studyButton, studyButton.getText());
    }

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

        //component.setFont(component.getFont().deriveFont(BIG_FONT_SIZE));

    }

    private void setProportionalFont(JComponent component, String text)
    {
        Dimension componentSize = new Dimension();
        component.getSize(componentSize);
        setProportionalFont(component, text, componentSize);
    }

    // adds a button to the JPanel
    // the button has half its width in padding on either side:
    //     |[p] [bbbb] [p]|
    // 'p' is padding, 'b' is button, '|' is the border of the JPanel
    private void addPanelButton(JPanel panel, GridBagConstraints gbc, JButton button, double proportion)
    {
        if (proportion <= 0.0 || proportion > 1)
        {
            printWarning("Invalid button proportion.");
            return;
        }

        double prevWeight = gbc.weightx; // remember the previous weightx
        
        // add padding to side
        gbc.weightx = (1.0 - proportion) / 2;
        addPanelPadding(panel, gbc);

        // add button
        gbc.weightx = proportion;
        gbc.gridx++;
        panel.add(button, gbc);

        // add padding to side
        gbc.weightx = (1.0 - proportion) / 2;
        gbc.gridx++;
        addPanelPadding(panel, gbc);

        gbc.weightx = prevWeight;
    }

    // add padding to the JPanel according to the provided constraints;
    // this is its own method to abstract away the use of an empty panel as padding.
    private void addPanelPadding(JPanel panel, GridBagConstraints gbc)
    {
        panel.add(new JLabel(), gbc);
    }

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

    private GridBagConstraints generateGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        
        // start at (0, 0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }
}
