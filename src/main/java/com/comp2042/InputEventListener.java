// 输入事件监听接口：用来处理玩家或系统触发的移动、旋转、下落等操作。
// GuiController 会把键盘事件转成 MoveEvent，然后交给实现类（通常是 GameController）处理。
// 这样界面层不用关心具体的游戏逻辑，结构更清晰。
//
// Input listener interface. GUI sends MoveEvent objects here when the user
// presses keys (or when the game thread triggers a down event). The actual
// game logic is handled by classes like GameController.

package com.comp2042;

public interface InputEventListener {

    DownData onDownEvent(MoveEvent event);

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    void createNewGame();
}
