// 棋盘接口：定义了整个游戏棋盘应该具备的功能。
// 包含移动方块、旋转、生成新方块、合并到背景、清行等核心操作。
// 具体实现会在 SimpleBoard 或其他类里完成。
//
// Board interface: defines the core behaviours of the game board.
// Includes moving/rotating the brick, generating new bricks, merging into the grid,
// clearing rows, and getting board data. Actual logic is implemented in classes
// like SimpleBoard.

package com.comp2042;

public interface Board {

    boolean moveBrickDown();

    boolean moveBrickLeft();

    boolean moveBrickRight();

    boolean rotateLeftBrick();

    boolean createNewBrick();

    int[][] getBoardMatrix();

    ViewData getViewData();

    void mergeBrickToBackground();

    ClearRow clearRows();

    Score getScore();

    void newGame();
}
