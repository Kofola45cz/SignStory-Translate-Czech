package com.maximuslotro.mc.bnnwidget;

import javax.annotation.Nonnull;

/**
 * Widgetを生成します
 *
 * @author TeamFruit
 */
public interface WidgetBuilder<E extends WCommon> {
	@Nonnull
	E build();
}
