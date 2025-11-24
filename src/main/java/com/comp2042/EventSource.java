// 事件来源类型：用来区分事件是用户触发的，还是系统线程触发的。
// 例如：用户按键移动 vs 游戏自动让方块下落。
//
// Event source type: distinguishes whether an action comes from the user
// (keyboard input) or from the game thread (auto fall, timers).

package com.comp2042;

public enum EventSource {
    USER, THREAD
}
