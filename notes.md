# TODO
# Formatting the code
[ ] Remove test functions
[ ] Remove Flashcard.toString
[ ] Remove Flashcard.equals
[ ] Remove any extra main methods

## Done
[✓] make RANDOM_WORD_COUNT in ArionUtils final again
[✓] Reread
    [✓] Add inline comments
[✓] Change the Guide's about image to an actual image of the About window (not Lena 😭)
[✓] Remove lena.png from images
[✓] Remove mock flashcards
[✓] Remove headless mode
[✓] Remove test hooks
[✓] Add inline comments
    [✓] Comments above each file
    [✓] Write comments for SAXErrorHandler and MutablePopup 
    [✓] Methods
        [✓] All methods have a block comment summarizing: Purpose, Input and Output
        [-] Some complex methods have explanatory comments inside
[✓] Format code
[✓] Add tests for all public methods
[✓] Figure out how to implement the features required by the rubric:
    [✓] Sequential Search: When studying finding cards due today
    [✓] Binary Search: When adding, maintain the sorted order
    [-] Casting datatypes (??)
    [✓] ArrayList: Flashcards array
    [-] Stack (maybe)
    [✓] Queue: Used to store the due cards
    [✓] Abstract Class & Method Overloading: Different kinds of flashcards
        - Simple Flashcard
        - Single select (radio buttons)
        - Multi-select & check (check boxes)
[✓] Write pseudocode
    [✓] Ensure menu callbacks are considered
        [✓] Include the Browse feature
    [✓] Include ArionDisplay.displayConfirmationWindow
[✓] Change sequence diagrams:
    [-] Add flashcard.toStringArray and flashcard.fromStringArray
    [✓] Change the class diagram:
        [✓] Remove Guide classes
            [✓] Change Guide sequence diagram to reflect this
        [✓] Add parseGuide
        [-] ArionDisplay includes displayConfirmationWindow
        [-] Flashcard includes fromStringArray
            [-] Edit sequence diagram
            [-] Add sequence diagram
        [✓] ArionDisplay includes displayMessage
            [✓] Study sequence diagram
        [✓] Remove updateReviewedCard
        [✓] Make review date & review interval public
        [✓] Add quit function to ArionDisplay
    [-] Double check that text matches sequence diagrams
    [-] Change Save, Load, and Delete so their overwriting confirmation includes display.displayConfirmationWindow
[ ] Actually implement test cases for existing methods
    [✓] ArionDisplay.java
    [✓] Database.java
        [✓] Change ArionTests.java deleteTestCase so it also checks the resulting flashcards are correct (maybe add unique flashcards)
    [✓] Arion.java
    [ ] Flashcard.java
    [ ] Refactor so tests use Test abstract class
[✓] Add text to all thrown exceptions
    [✓] ArionDisplay.java
    [✓] Flashcard.java
    [✓] Database.java
    [✓] Arion.java
[✓] Main Screen
    [✓] Clean up the code a little
        - Rename "makeDisplayReady" if possible
        - Move the code for setting up the JLabel header into a distinct function
            (like generateHeader or something, there should be a header for each screen)
    [✓] Implement the button callbacks
    [-] Use a 'displayElement' class to denote different kinds of elements:
        - ONLY DO THIS if there end up being many kinds of elements (more than header & paragraph)
        - enum for kind of element (paragraph, header, etc.)
        - font size
        - JComponent
        - this may be doable using just an enum that gets passed around with the actual component
[✓] Browse (not including delete & edit)
    [✓] Make adding the 'content' sub-panel to the main panel cleaner (setting the maximum size is not clean)
    [✓] Add buttons next to entries
[✓] Add
    [✓] Fix displayAddScreen so the text fields aren't so large
[✓] Save
    [✓] User confirmation
[✓] Load
    [✓] User confirmation
    - Wait until Save and Add features is completed
[✓] Delete
    [✓] User confirmation
[✓] Edit
[✓] Study
    [✓] Use Queue
[✓] Sort
    [✓] Make combo box sizes the same
[✓] Quit
[✓] Guide
    [✓] Make the text area not have to scroll horizontally
    [-] Use a real pop-up window, rather than this thing that cannot be dragged around (maybe another frame?).
    [✓] Add images into guide rendering
    [✓] Make generation of Guide panel generic (i.e. No predefined width / height)
    [✓] Make the buttons not clip into the bottom
    [✓] Fix all the image sizes
    [✓] Make pair consider image width when deciding sizes
    [✓] Make text parsing not consider white space
    [✓] Set JTextArea height to its actual size
    [✓] Write the guide
[✓] Press space to flip the flashcard
[✓] About
    [✓] What is SRS?
    [✓] Who uses it?
    [✓] How was Arion developed?
[✓] Ensure that window and pop up is not hard coded (pop up size should be relative to window size)
[✓] Add back buttons

# Testing

## Unit Testing
- Tests the smallest unit:
    - Classes
    - Methods
    - Data structures
- Uses test cases to test:
    - Valid: expected conditions
    - Invalid: unexpected data
- Documented in Unit Test Reports:
    - Prepared in tables
    - Headings:
        - Unit being tested
        - Test case
        - Expected Results
        - Returned Results:
            - Accurate output
            - Inaccurate output
            - User prompts to clarify input
            - Errors
            - Crashes
        - Status of Solution

## System Testing
- Tests that the software system **as a whole**:
    - Is error-free
    - Meets the requirements
    - Has seamless integration
    - Is robust
- System testing is performed similarly to unit testing
- Black-box testing

# Example Test Report
| Unit    | Test Cases | Expected                    | Returned                     | Solution      |
-----------------------------------------------------------------------------------------------------
| addNum  | 7, 7       | 14                          | 14                           |               |
|         | 10, -1     | 9                           | 11                           | Fixed         |
|         | a, 1       | Please enter a valid number | Please enter a valid number  |               |
|         | 1, a       | Please enter a valid number | Please enter a valid number  |               |
|         | a, a       | Please enter a valid number | crash                        | Fixed         |
| getName | Jim Jones  | "Jim Jones"                 | "Jim Jones"                  |               |
|         | Jones, Jim | prompt user                 | prompt user                  |               |
|         | Jim.Jones  | prompt user                 | prompt user                  |               |
|         | JimJones   | prompt user                 | "JimJones"                   | Unresolved    |
|         | Jim#1Jones | prompt user                 | prompt user                  |               |
