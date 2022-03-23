package com.maximuslotro.mc.signpic.plugin.gui;

import java.text.SimpleDateFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.maximuslotro.mc.bnnwidget.WBase;
import com.maximuslotro.mc.bnnwidget.WEvent;
import com.maximuslotro.mc.bnnwidget.WList;
import com.maximuslotro.mc.bnnwidget.WPanel;
import com.maximuslotro.mc.bnnwidget.component.FontScaledLabel;
import com.maximuslotro.mc.bnnwidget.font.WFontRenderer;
import com.maximuslotro.mc.bnnwidget.motion.Motion;
import com.maximuslotro.mc.bnnwidget.position.Area;
import com.maximuslotro.mc.bnnwidget.position.Coord;
import com.maximuslotro.mc.bnnwidget.position.Point;
import com.maximuslotro.mc.bnnwidget.position.R;
import com.maximuslotro.mc.bnnwidget.render.OpenGL;
import com.maximuslotro.mc.bnnwidget.render.RenderOption;
import com.maximuslotro.mc.bnnwidget.render.WRenderer;
import com.maximuslotro.mc.bnnwidget.util.NotifyCollections;
import com.maximuslotro.mc.bnnwidget.util.NotifyCollections.IModCount;
import com.maximuslotro.mc.bnnwidget.var.V;
import com.maximuslotro.mc.bnnwidget.var.VMotion;
import com.maximuslotro.mc.signpic.Client;
import com.maximuslotro.mc.signpic.attr.AttrReaders;
import com.maximuslotro.mc.signpic.attr.prop.SizeData;
import com.maximuslotro.mc.signpic.entry.EntryId;
import com.maximuslotro.mc.signpic.entry.content.ContentManager;
import com.maximuslotro.mc.signpic.gui.SignPicLabel;
import com.maximuslotro.mc.signpic.plugin.SignData;
import com.maximuslotro.mc.signpic.plugin.search.FilterExpression;
import com.maximuslotro.mc.signpic.plugin.search.Searchable;

import net.minecraft.util.ResourceLocation;

public class GuiList extends ScrollPanel implements Searchable {
	protected static @Nonnull ResourceLocation mouseoverSound = new ResourceLocation("signpic", "gui.mouseover");
	protected static @Nonnull SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	protected final @Nonnull IModCount<SignData> data;
	protected final @Nonnull WPanel scrollPane;
	protected final @Nonnull WList<SignData, ListElement> list;

	protected @Nonnull IModCount<SignData> now;

	public GuiList(final R position, final IModCount<SignData> data) {
		super(position);
		this.data = data;
		this.now = data;
		this.list = new WList<SignData, ListElement>(new R(), data) {
			@Override
			protected ListElement createWidget(final SignData t, final int i) {
				final VMotion top = V.am(i*30);
				return new ListElement(new R(Coord.top(top), Coord.height(30)), top, t);
			}

			@Override
			protected void onMoved(final SignData t, final ListElement w, final int from, final int to) {
				w.top.stop().add(Motion.move(to*30)).start();
			}
		};
		this.scrollPane = new WPanel(new R(Coord.left(0), Coord.right(15), Coord.top(this.top))) {
			@Override
			protected void initWidget() {
				add(GuiList.this.list);
			}
		};
	}

	@Override
	protected void initWidget() {
		add(this.scrollPane);
		add(new GuiScrollBar(new R(Coord.right(3), Coord.width(8)), this));
	}

	@Override
	public boolean mouseScrolled(final WEvent ev, final Area pgp, final Point p, final int scroll) {
		if (pgp.pointInside(p))
			scroll(scroll, (GuiManager) ev.owner, getGuiPosition(pgp));
		return false;
	}

	@Override
	public void scrollTo(final float to, final @Nullable GuiManager manager, final @Nullable Area position) {
		super.scrollTo(to, manager, position);
		if (-to>getScrollableHeight()&&manager!=null) {
			final int size = this.data.size();
			manager.get(size, size+100);
		}
	}

	@Override
	public float getAllHeight() {
		return this.now.size()*30;
	}

	private boolean searching;

	@Override
	public void filter(@Nullable final FilterExpression expression) {
		if (expression==null) {
			this.now = this.data;
			scrollTo(0, null, null);
			this.list.setList(this.now);
		} else
			new Thread() {
				@Override
				public void run() {
					GuiList.this.searching = true;
					GuiList.this.now = expression.findList();
					invokeLater(new Runnable() {
						@Override
						public void run() {
							scrollTo(0, null, null);
							GuiList.this.list.setList(GuiList.this.now);
							GuiList.this.searching = false;
						}
					});
				}
			}.start();
	}

	@Override
	public IModCount<SignData> getNow() {
		return this.now;
	}

	@Override
	public boolean isSearching() {
		return this.searching;
	}

	protected class ListElement extends WPanel {
		public final @Nonnull VMotion top;

		protected final @Nonnull SignData data;
		protected final @Nonnull EntryId id;
		protected final @Nullable AttrReaders meta;

		public ListElement(final R position, final VMotion top, final SignData t) {
			super(position);
			this.top = top;
			this.data = t;
			this.id = EntryId.from(this.data.getSign());
			this.meta = this.id.getMeta();
		}

		@Override
		protected void initWidget() {
			add(new WPanel(new R(Coord.top(.5f), Coord.bottom(.5f))) {
				@Override
				protected void initWidget() {
					add(new SignPicLabel(new R(Coord.left(0), Coord.width(38.6f)), ContentManager.instance) {
						@Override
						public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity, final @Nonnull RenderOption opt) {
							final Area a = getGuiPosition(pgp);
							final Area list = GuiList.this.area;
							if (list!=null) {
								final Area t = list.trimArea(a);
								WRenderer.startShape();
								OpenGL.glColor4f(0f, 0f, 0f, .5f);
								draw(t);
								if (t!=null)
									opt.put("trim", Area.abs(0f, 1f/a.h()*(t.y1()-a.y1()), 1f, 1f/a.h()*(a.h()-(a.y2()-t.y2()))));
								super.draw(ev, pgp, p, frame, popacity, opt);
							}
						};
					}.setEntryId(ListElement.this.id));
					final AttrReaders meta = ListElement.this.meta;
					if (meta!=null) {
						final SizeData size = meta.sizes.getMovie().get();
						add(new ListLabel(new R(Coord.left(40), Coord.right(0), Coord.height(8)), GuiManager.BOLD_FONT)
								.setAlign(Align.LEFT)
								.setText(size.getHeight()+" × "+size.getWidth()+" - "+ListElement.this.data.getPlayerName()));
						add(new ListLabel(new R(Coord.left(0), Coord.right(2), Coord.height(8)), GuiManager.BOLD_FONT)
								.setAlign(Align.RIGHT)
								.setText(ListElement.this.data.getX()+", "+ListElement.this.data.getY()+", "+ListElement.this.data.getZ()+" - "+ListElement.this.data.getWorldName()));
						add(new ListLabel(new R(Coord.left(40), Coord.right(0), Coord.height(6), Coord.top(18)), GuiManager.PLAIN_FONT)
								.setAlign(Align.LEFT)
								.setText("Last Updated: "+dateFormat.format(ListElement.this.data.getUpdateDate())));
						add(new WBase(new R(Coord.bottom(1), Coord.right(1), Coord.height(15), Coord.width(15))) {
							@Override
							public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity, final RenderOption opt) {
								super.draw(ev, pgp, p, frame, popacity, opt);
								final Area list = GuiList.this.area;
								if (list!=null)
									if (list.areaInside(getGuiPosition(pgp))) {
										WRenderer.startTexture();
										texture().bindTexture(SignPicLabel.defaultTexture);
										drawTexture(getGuiPosition(pgp), null, null);
										super.draw(ev, pgp, p, frame, popacity, opt);
									}
							}
						});
						final IModCount<ResourceLocation> list = new NotifyCollections.NotifyArrayList<ResourceLocation>();
						add(new WList<ResourceLocation, WBase>(new R(Coord.bottom(1), Coord.right(40), Coord.height(15)), list) {
							@Override
							protected void initWidget() {
								for (final AttrIcons attr : AttrIcons.values())
									if (attr.isInclude(meta))
										list.add(attr.getIcon());
							}

							@Override
							protected WBase createWidget(final ResourceLocation resource, final int i) {
								return new WBase(new R(Coord.right(i*20), Coord.width(15))) {
									@Override
									public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity, final RenderOption opt) {
										final Area list = GuiList.this.area;
										if (list!=null)
											if (list.areaInside(getGuiPosition(pgp))) {
												WRenderer.startTexture();
												texture().bindTexture(resource);
												drawTexture(getGuiPosition(pgp), null, null);
												super.draw(ev, pgp, p, frame, popacity, opt);
											}
									}
								};
							}
						});
					}
				}

				@Override
				public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity, final @Nonnull RenderOption opt) {
					final Area list = GuiList.this.area;
					if (list!=null)
						if (list.areaOverlap(getGuiPosition(pgp))) {
							WRenderer.startShape();
							OpenGL.glColor4f(.3f, .3f, .3f, .5f);
							draw(list.trimArea(getGuiPosition(pgp)));
							super.draw(ev, pgp, p, frame, popacity, opt);
						}
				}

				protected boolean playsound = false;

				@Override
				public void update(final WEvent ev, final Area pgp, final Point p) {
					final Area list = GuiList.this.area;
					if (list!=null)
						if (list.areaOverlap(getGuiPosition(pgp))) {
							final boolean mouseover = getGuiPosition(pgp).pointInside(p);
							if (!mouseover)
								this.playsound = false;
							if (!this.playsound&&mouseover) {
								Client.playSound(mouseoverSound, 1f);
								this.playsound = true;
							}
							super.update(ev, pgp, p);
						}
				}
			});
		}
	}

	protected class ListLabel extends FontScaledLabel {

		public ListLabel(final R position, final WFontRenderer wf) {
			super(position, wf);
		}

		@Override
		public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity, final RenderOption opt) {
			final Area list = GuiList.this.area;
			if (list!=null)
				if (list.areaInside(getGuiPosition(pgp)))
					super.draw(ev, pgp, p, frame, popacity, opt);
		}
	}
}
