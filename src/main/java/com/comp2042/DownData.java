// 下落结果数据：用来把方块下落后的结果一起打包返回。
// 里面包含了清行的数据（如果有清行的话），以及界面需要显示的最新 ViewData。
// 本类只是一个简单的数据容器，不负责任何逻辑。
//
// Downward movement data holder. Bundles together the result of a falling
// operation, including row clearing info (if any) and updated ViewData.
// Works as a simple container without any game logic.

package com.comp2042;

public final class DownData {
    private final ClearRow clearRow;
    private final ViewData viewData;

    public DownData(ClearRow clearRow, ViewData viewData) {
        this.clearRow = clearRow;
        this.viewData = viewData;
    }

    public ClearRow getClearRow() {
        return clearRow;
    }

    public ViewData getViewData() {
        return viewData;
    }
}
