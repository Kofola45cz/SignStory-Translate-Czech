package com.maximuslotro.mc.signpic.plugin.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.maximuslotro.mc.bnnwidget.WBase;
import com.maximuslotro.mc.bnnwidget.WEvent;
import com.maximuslotro.mc.bnnwidget.font.FontPosition;
import com.maximuslotro.mc.bnnwidget.position.Area;
import com.maximuslotro.mc.bnnwidget.position.Point;
import com.maximuslotro.mc.bnnwidget.position.R;
import com.maximuslotro.mc.bnnwidget.render.OpenGL;
import com.maximuslotro.mc.bnnwidget.render.RenderOption;
import com.maximuslotro.mc.bnnwidget.render.WRenderer;
import com.maximuslotro.mc.signpic.Client;

import net.minecraft.util.ResourceLocation;

public class GuiManagerButton extends WBase {
	protected static @Nonnull ResourceLocation hitSound = new ResourceLocation("signpic", "gui.confirm");

	public @Nullable String text;

	public GuiManagerButton(final R position) {
		super(position);
	}

	public @Nullable String getText() {
		return this.text;
	}

	public @Nonnull GuiManagerButton setText(final String text) {
		this.text = text;
		return this;
	}

	@Override
	public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity, @Nonnull final RenderOption opt) {
		drawText(ev, pgp, p, frame, popacity);
	}

	public void drawText(final @Nonnull WEvent ev, final @Nonnull Area pgp, final @Nonnull Point mouse, final float frame, final float popacity) {
		final String text = getText();
		if (this.text!=null) {
			final Area a = getGuiPosition(pgp);
			OpenGL.glColor4f(1f, 1f, 1f, 1f);
			WRenderer.startTexture();
			final FontPosition fp = GuiManager.PLAIN_FONT.getSetting()
					.setScale(.5f)
					.setPosition(a.x1()+a.w()/2, a.y1())
					.setFontSize(Math.round(a.h()/ev.owner.scale())*2)
					.setAlign(Align.CENTER)
					.setShadow(true)
					.setText(text);
			GuiManager.PLAIN_FONT.drawString(fp);
		}
	}

	protected boolean onClicked(final @Nonnull WEvent ev, final @Nonnull Area pgp, final @Nonnull Point mouse, final int button) {
		return true;
	}

	@Override
	public boolean mouseClicked(final @Nonnull WEvent ev, final @Nonnull Area pgp, final @Nonnull Point p, final int button) {
		final Area abs = getGuiPosition(pgp);
		if (abs.pointInside(p))
			if (button<2)
				if (onClicked(ev, pgp, p, button)) {
					Client.playSound(hitSound, 1f);
					return true;
				}
		return false;
	}
}