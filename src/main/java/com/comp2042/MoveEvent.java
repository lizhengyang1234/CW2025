// 移动事件类：把一次输入动作（比如左移、右移、下落、旋转）封装成一个对象。
// 事件里包含动作类型（EventType）以及事件来源（用户/系统）。
// GUI 通过创建 MoveEvent 来把按键动作传给 GameController。
//
// Move event class. Wraps a single action such as left, right, down, or rotate.
// Contains both the event type and where it came from (user or game thread).
// GUI uses MoveEvent to forward keyboard input to the GameController.

package com.comp2042;

public final class MoveEvent {
    private final EventType eventType;
    private final EventSource eventSource;

    public MoveEvent(EventType eventType, EventSource eventSource) {
        this.eventType = eventType;
        this.eventSource = eventSource;
    }

    public EventType getEventType() {
        return eventType;
    }

    public EventSource getEventSource() {
        return eventSource;
    }
}
