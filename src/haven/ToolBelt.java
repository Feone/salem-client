package haven;

import static haven.Inventory.invsq;
import static haven.Inventory.isqsz;
import java.awt.event.KeyEvent;

public class ToolBelt extends Widget implements DTarget, DropTarget {
	public int curbelt = 0;
	public Indir<Resource>[] contents;
	private GameUI gui;
	private int size;
	private ToolBeltUI beltUI;
	private Button number, up, down;
	private boolean flipped;

	/*
	 * private static final int fkeys[] = {KeyEvent.VK_F1, KeyEvent.VK_F2,
	 * KeyEvent.VK_F3, KeyEvent.VK_F4, KeyEvent.VK_F5, KeyEvent.VK_F6,
	 * KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10,
	 * KeyEvent.VK_F11, KeyEvent.VK_F12};
	 */
	private static final int beltkeys[] = { KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0, KeyEvent.VK_MINUS, KeyEvent.VK_EQUALS };
	private final String[] keyTags = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=" };

	public ToolBelt(Coord c, GameUI parent, int ID, Indir<Resource>[] contents) {
		super(c, Inventory.invsz(new Coord(12, 1)), parent);
		this.contents = contents;
		this.curbelt = ID;
		size = 1;
		flipped = false;
		// TODO save settings
		this.gui = parent;
		initButtons();

	}

	private void initButtons() {
		number = new Button(c, 25, gui, Integer.toString(curbelt + 1)) {
			public void click() {
				gui.beltManager.switchNumber(curbelt);
			}
		};
		up = new Button(c, 25, gui, "up") {
			public void click() {
				beltUI.moveDown(curbelt);
			}
		};
		down = new Button(c, 25, gui, "down") {
			public void click() {
				beltUI.moveUp(curbelt);
			}
		};
	}

	public void setUI(ToolBeltUI beltUI) {
		this.beltUI = beltUI;
	}

	@Override
	public void destroy() {
		number.destroy();
		up.destroy();
		down.destroy();
		super.destroy();
	}

	public ToolBeltUI getUI() {
		return beltUI;
	}

	public void resize() {
		super.sz = flipped ? Inventory.invsz(new Coord(size, 12)) : Inventory.invsz(new Coord(12, size));
	}

	public void keyact(final int slot) {
		MapView map = gui.map;
		if (map != null) {
			Coord mvc = map.rootxlate(ui.mc);
			if (mvc.isect(Coord.z, map.sz)) {
				map.delay(map.new Hittest(mvc) {
					protected void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
						if (inf == null)
							gui.wdgmsg("belt", slot, 1, ui.modflags(), mc);
						else
							gui.wdgmsg("belt", slot, 1, ui.modflags(), mc, (int) inf.gob.id, inf.gob.rc);
					}

					protected void nohit(Coord pc) {
						gui.wdgmsg("belt", slot, 1, ui.modflags());
					}
				});
			}
		}
	}

	private Coord beltc(int i) {
		if (!flipped) {
			return (Inventory.sqoff(new Coord(i, 0)));
		} else {
			return (Inventory.sqoff(new Coord(0, i))); // TODO verify
		}
	}

	private int beltslot(Coord c) {
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < size; j++) {
				if (c.isect(beltc(i), isqsz))
					return (i + (curbelt * 12));
			}
		}
		return (-1);
	}

	public void draw(GOut g) {
		if (!flipped) {
			invsq(g, Coord.z, new Coord(12, size));
			for (int i = 0; i < 12; i++) {
				int slot = i + (curbelt * 12);
				Coord c = beltc(i);
				try {
					if (contents[slot] != null)
						g.image(contents[slot].get().layer(Resource.imgc).tex(), c);
				} catch (Loading e) {
				}
				g.chcolor(156, 180, 158, 255);
				if (curbelt == 0) {
					FastText.aprintf(g, c.add(isqsz), 1, 1, "%s", keyTags[i]); // Shortkey
																				// tags;
				}
				g.chcolor();
			}
		} else {
			invsq(g, Coord.z, new Coord(size, 12));
			for (int i = 0; i < 12; i++) {
				int slot = i + (curbelt * 12);
				Coord c = beltc(i);
				try {
					if (contents[slot] != null)
						g.image(contents[slot].get().layer(Resource.imgc).tex(), c);
				} catch (Loading e) {
				}
				g.chcolor(156, 180, 158, 255);
				if (curbelt == 0) {
					FastText.aprintf(g, c.add(isqsz), 1, 1, "%s", keyTags[i]); // Shortkey
																				// tags;
				}
				g.chcolor();
			}
		}
	}

	@Override
	public boolean mousedown(Coord c, int button) {
		int slot = beltslot(c);
		if (slot != -1) {
			if (button == 1)
				gui.wdgmsg("belt", slot, 1, ui.modflags());
			if (button == 3)
				gui.wdgmsg("setbelt", slot, 1);
			return (true);
		}
		return (false);
	}

	public boolean globtype(char ch, KeyEvent ev) {
		if (!key(ch, ev))
			return (super.globtype(ch, ev));
		else
			return true;
	}

	public boolean key(char key, KeyEvent ev) {
		if (key != 0) {
			return false;
		}
		boolean M = (ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0;
		for (int i = 0; i < beltkeys.length; i++) {
			if (ev.getKeyCode() == beltkeys[i]) {
				if (M) {
					curbelt = i;
					return (true);
				} else {
					keyact(i);
					return (true);
				}
			}
		}
		return false;
	}

	public boolean drop(Coord c, Coord ul) {
		int slot = beltslot(c);
		if (slot != -1) {
			this.wdgmsg("setbelt", slot, 0);
			return (true);
		}
		return (false);
	}

	public boolean iteminteract(Coord c, Coord ul) {
		return (false);
	}

	public boolean dropthing(Coord c, Object thing) {
		int slot = beltslot(c);
		if (slot != -1) {
			if (thing instanceof Resource) {
				Resource res = (Resource) thing;
				if (res.layer(Resource.action) != null) {
					gui.wdgmsg("setbelt", slot, res.name);
					return (true);
				}
			}
		}
		return (false);
	}

	public int getID() {
		return curbelt;
	}

	public void hide(boolean hidden) {
		this.visible = !hidden;
		number.visible = !hidden;
		up.visible = !hidden;
		down.visible = !hidden;
	}

	public void flip(boolean flipped) {
		this.flipped = flipped;
		resize();
	}

	public void setCoord(Coord c) {
		this.c = c;
		if (flipped) {
			number.c = c.add(20, 530);
			up.c = c.add(5, 530);
			down.c = c.add(35, 530);
		} else {
			number.c = c.add(530, 20);
			up.c = c.add(530, 5);
			down.c = c.add(530, 35);
		}
	}
}
