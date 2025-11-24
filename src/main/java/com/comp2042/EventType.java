// 游戏里的事件类型。代表玩家或系统可能触发的几种基本动作。
// 包括向下、向左、向右移动，以及旋转方块。
// 主要用在事件分发和输入处理里。
//
// Basic game event types. Used to describe the actions triggered by
// the user or the game system, such as moving down/left/right or rotating.

package com.comp2042;
public enum EventType {
    DOWN, LEFT, RIGHT, ROTATE
}
