package com.comp2042;

// 游戏控制器：负责整个游戏流程的核心逻辑。
// 包含处理输入（左右/旋转/下落/硬降）、触发方块下落、生成新方块、清行、更新分数等。
// 也会把更新后的棋盘和数据同步给 GUI。

public class GameController implements InputEventListener {

    // 棋盘：25 行（前 2 行隐藏）、10 列
    private Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());

        // 绑定分数到右侧 SCORE
        viewGuiController.bindScore(board.getScore().scoreProperty());
        // 新增：绑定总消行数到右侧 LINES
        viewGuiController.bindLines(board.getScore().linesProperty());
    }

    /**
     * 处理“向下”事件：
     * - eventType == DOWN：原来的单步下落逻辑
     * - eventType == HARD_DROP：空格键，一路落到底
     */
    @Override
    public DownData onDownEvent(MoveEvent event) {

        ClearRow clearRow = null;

        // ======= 硬降逻辑（空格键） =======
        if (event.getEventType() == EventType.HARD_DROP) {
            int steps = 0;

            // 一直往下走，直到不能再走
            while (board.moveBrickDown()) {
                steps++;
            }

            // 落到底后，合并到背景
            board.mergeBrickToBackground();

            // 检查并清行
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                Score score = board.getScore();
                score.add(clearRow.getScoreBonus());
                // 把这次消掉的行数加到总行数里
                score.addLines(clearRow.getLinesRemoved());
            }

            // 为硬降的每一格移动加分（例如 1 分/格）
            if (steps > 0) {
                board.getScore().add(steps);
            }

            // 生成新方块，如果一开始就冲突，则游戏结束
            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }

            // 刷新背景棋盘
            viewGuiController.refreshGameBackground(board.getBoardMatrix());

            return new DownData(clearRow, board.getViewData());
        }

        // ======= 普通单步下落（原来的逻辑） =======
        boolean canMove = board.moveBrickDown();
        if (!canMove) {
            // 不能再往下：合并到背景
            board.mergeBrickToBackground();

            // 检查并清行，加分
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                Score score = board.getScore();
                score.add(clearRow.getScoreBonus());
                // 普通下落时也要累计总消行数
                score.addLines(clearRow.getLinesRemoved());
            }

            // 生成新方块，若一开始就冲突，则游戏结束
            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }

            // 刷新背景
            viewGuiController.refreshGameBackground(board.getBoardMatrix());

        } else {
            // 成功向下移动一格：
            // 只有玩家手动按键（USER）才加 1 分，自动下落（THREAD）不加分
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }

        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
}