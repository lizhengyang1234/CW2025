// 视图数据类：把当前方块的位置、形状，以及下一个方块的形状打包成一个对象。
// GUI 每一帧都会从 board 取到一个 ViewData，然后根据里面的数据来更新画面。
// 这个类本身不包含逻辑，只负责提供渲染所需的信息。
//
// View data object: contains everything the GUI needs to draw the game,
// including the current brick’s matrix and its position, plus the next brick.
// It has no game logic — it’s purely a data container for rendering.

package com.comp2042;

public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final int[][] nextBrickData;

    public ViewData(int[][] brickData, int xPosition, int yPosition, int[][] nextBrickData) {
        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBrickData = nextBrickData;
    }

    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    public int getxPosition() {
        return xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public int[][] getNextBrickData() {
        return MatrixOperations.copy(nextBrickData);
    }
}
