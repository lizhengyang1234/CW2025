// 清行结果类：用来记录一次清行之后产生的各种数据。
// 包括清掉了多少行、更新后的棋盘矩阵、以及应该加多少分。
// 只是一个简单的数据包，不负责具体的清行逻辑。
//
// Result object for a row-clearing operation.
// Stores how many lines were removed, the updated board matrix,
// and the score bonus gained. Acts as a simple data container.

package com.comp2042;

public final class ClearRow {

    private final int linesRemoved;
    private final int[][] newMatrix;
    private final int scoreBonus;

    public ClearRow(int linesRemoved, int[][] newMatrix, int scoreBonus) {
        this.linesRemoved = linesRemoved;
        this.newMatrix = newMatrix;
        this.scoreBonus = scoreBonus;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public int[][] getNewMatrix() {
        return MatrixOperations.copy(newMatrix);
    }

    public int getScoreBonus() {
        return scoreBonus;
    }
}
