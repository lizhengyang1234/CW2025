1. GitHub Repository
https://github.com/lizhengyang1234
2. Compilation Instructions
This project uses Maven and JavaFX 21.0.6.，JDK 23
All dependencies are handled by Maven:
	•	javafx-controls
	•	javafx-fxml
	•	junit-jupiter-api (tests)
	•	junit-jupiter-engine (tests)

3.Implemented and Working Properly:The following required features from the specification are implemented:
	All required base features work correctly:
	•	Piece movement: left, right, down
	•	Rotation
	•	Hard drop
	•	Line clearing
	•	Falling speed
	•	Ghost piece (shows predicted landing position)
	•	Score increases per drop and per cleared line
	•	High score saved and loaded correctly
	•	Level increases every 5 cleared lines
	•	Falling speed accelerates based on level
   Next Piece Preview

  •	Displays the next Tetromino accurately, including bomb pieces in Bomb Mode.
  •	 Game Over Detection
  •	Correctly triggers when a new piece cannot spawn.
  •	Pause and Resume
  •	Both button and keyboard control (“P”) work properly.
Main Menu System
	•	Separate menu scene (mainMenu.fxml)
	•	Two game modes:
	•	Normal Mode
	•	Bomb Mode
	•	“Return to Main Menu” button inside game view
  Bomb Mode (New Feature)

Bomb blocks (ID = 8) appear randomly in Bomb Mode and:
	•	Cause a 3×3 explosion
	•	Remove blocks inside the area immediately
	•	Trigger an animation:
	•	Flash to black
	•	Fade-out
	•	Background shaking

4.Implemented but Not Working Properly：no
5.Features Not Implemented：can not add music,	
•	Custom skin/theme selector
6. New Java Classes
·BoardView
  Handles all visual rendering of the board, brick, ghost, and next preview.
·GhostRenderer
  Renders ghost piece at predicted landing position.
·NextBrickRenderer
  Draws the next Tetromino in the preview window.
·BoardState
  Stores and maintains the background board matrix.
·MovementController
  Pure logic class for movement and collision detection.
·RotationController
  Handles rotation and wall kick logic.
·BrickRotator
  Stores current brick rotation state.
·BrickGenerator (interface)
  Allows switching between normal and bomb brick generation.
·NormalBrickGenerator
  Generates standard Tetromino shapes.
·RandomBrickGenerator
  Generates Tetrominoes plus bomb blocks.
7. Modified Java Classes
GameController
	•	Major cleanup and refactoring
	•	Removed duplicated logic for “brick landed”
	•	Introduced handleBrickLanded() method
	•	Added support for Bomb Mode
	•	Integrated explosion animation trigger
	•	Simplified event-handling logic
 GuiController
	•	Added main menu navigation
	•	Integrated explosion animation into GUI flow
	•	Added digital font loading
	•	Improved HUD updates (Score, Lines, Level)
	•	Disconnected GUI from game logic for better modularity
SimpleBoard
Completely restructured from the original:
	•	Delegated responsibilities to helper classes
	•	Added bomb detection
	•	Added explosion logic and cell recording
	•	Improved matrix handling
	•	More stable and maintainable structure
ClearRow
	•	Expanded to support cleaner handling of updated matrix
	•	Now acts as a safe immutable data container
BoardView
	•	Fully separated rendering logic from game logic
	•	New explosion animation
	•	Added ghost and preview support
	•	Prevented animations from affecting falling piece
8. Unexpected Problems & How They Were Solved
 Explosion animation shaking the falling piece
Problem:
BrickPanel and GamePanel were both shaking because they shared parent layout.
Solution:
Only apply setTranslateX/Y() to gamePanel, not brickPanel

Returning to Main Menu caused “Location is not set” error
Cause: wrong resource path:


