package haven;

import java.util.ArrayList;

public class ToolBeltUI {

	private ArrayList<ToolBelt> belts;
	public Button lock, move, flip, hide, addRow, delRow, addBar, delBar;
	private GameUI gui;
	private boolean locked, flipped, hidden;
	private Coord c;

	public ToolBeltUI(ToolBelt initial, GameUI gui) {
		this.c = new Coord(0, 0);
		belts = new ArrayList<ToolBelt>();
		belts.add(initial);
		this.gui = gui;
		initial.setUI(this);
		initButtons();
		showButtons();
	}

	public void addBelt(ToolBelt belt) {
		belts.add(belt);
		belt.setUI(this);
		belt.flip(flipped);
		belt.hide(hidden);
		setCoord(c);
	}

	public void removeBelt(int ID) {
		for (int i = 0; i < belts.size(); i++) {
			if (belts.get(i).getID() == ID) {
				belts.remove(i);
			}
		}
	}

	public void moveUp(int beltID) {
		if (belts.size() == 1)
			return;
		for (int i = 0; i < belts.size(); i++) {
			if (belts.get(i).getID() == beltID) {
				int target = i + 1;
				if (i == (belts.size() - 1)) {
					target = 0;
				}
				ToolBelt tar = belts.get(target);
				ToolBelt cur = belts.get(i);
				belts.set(i, tar);
				belts.set(target, cur);
				setCoord(c);
			}
		}
	}

	public void moveDown(int beltID) {
		if (belts.size() == 1)
			return;
		for (int i = 0; i < belts.size(); i++) {
			if (belts.get(i).getID() == beltID) {
				int target = i - 1;
				if (i == 0) {
					target = belts.size() - 1;
				}
				ToolBelt tar = belts.get(target);
				ToolBelt cur = belts.get(i);
				belts.set(i, tar);
				belts.set(target, cur);
				setCoord(c);
			}
		}

	}

	public void replaceBelt(ToolBelt old, ToolBelt newBelt) {
		for (int i = 0; i < belts.size(); i++) {
			if (belts.get(i).getID() == old.getID()) {
				belts.set(i, newBelt);
				old.destroy();
				newBelt.setUI(this);
				newBelt.hide(false);
				setCoord(c);
			}
		}
	}

	public boolean isLocked() {
		return locked;
	}

	private void initButtons() {
		lock = new Button(c.add(0, -20), 50, gui, "lock") {
			public void click() {
				toggleLock();
			}
		};
		hide = new Button(c.add(50, -20), 50, gui, "hide") {
			public void click() {
				toggleShow();
			}
		};
		flip = new Button(c.add(100, -20), 50, gui, "flip") {
			public void click() {
				toggleFlip();
			}
		};
		move = new MoveButton(c.add(150, -20), 50, gui, "move", this);
		addRow = new Button(c.add(200, -20), 15, gui, "+") {
			public void click() {
				addRow();
			}
		};
		delRow = new Button(c.add(220, -20), 15, gui, "-") {
			public void click() {
				removeRow();
			}
		};
		addBar = new Button(c.add(240, -20), 50, gui, "add bar") {
			public void click() {
				addBar();
			}
		};
		delBar = new Button(c.add(290, -20), 50, gui, "close bar") {
			public void click() {
				destroyUI();
			}
		};
	}

	public class MoveButton extends Button {
		private boolean moving;
		private ToolBeltUI target;
		private Coord start;

		public MoveButton(Coord c, Integer w, Widget parent, String title, ToolBeltUI target) {
			super(c, w, parent, title);
			this.target = target;
		}

		@Override
		public boolean mousedown(Coord c, int button) {
			if (button == 1) {
				moving = true;
				ui.grabmouse(this);
				start = c.inv();
				start = start.add(-150, 20);
			}
			return true;
		}

		@Override
		public boolean mouseup(Coord c, int button) {
			if (!moving) {
				return super.mouseup(c, button);
			} else {
				if (button == 1) {
					moving = false;
					ui.grabmouse(null);
					doMove(this.c.add(c.add(start)));
				}
				return true;
			}
		}

		@Override
		public void mousemove(Coord c) {
			if (moving) {
				doMove(this.c.add(c.add(start)));
			} else {
				super.mousemove(c);
			}
		}

		public void doMove(Coord c) {
			target.setCoord(c);
		}
	}

	private void toggleFlip() {
		flipped = !flipped;
		for (int i = 0; i < belts.size(); i++) {
			belts.get(i).flip(flipped);
		}
		resize();
	}

	private void toggleShow() {
		hidden = !hidden;
		for (int i = 0; i < belts.size(); i++) {
			belts.get(i).hide(hidden);
		}
		if (hidden) {
			hide.change("show");
		} else {
			hide.change("hide");
		}
	}

	private void toggleLock() {
		locked = !locked;
		if (!locked) {
			lock.change("lock");
			move.visible = true;
			flip.visible = true;
			addRow.visible = true;
			delRow.visible = true;
			addBar.visible = true;
			delBar.visible = true;
		}
		if (locked) {
			lock.change("unlock");
			move.visible = false;
			flip.visible = false;
			addRow.visible = false;
			delRow.visible = false;
			addBar.visible = false;
			delBar.visible = false;
		}
	}

	public void showButtons() {
		flip.show();
		hide.show();
		move.show();
		lock.show();
		addRow.show();
		delRow.show();
		addBar.show();
		delBar.show();
	}

	public void setCoord(Coord c) {
		this.c = c;
		int offset = 50;
		for (int i = 0; i < belts.size(); i++) {
			if (!flipped)
				belts.get(i).setCoord(c.add(0, i * offset));
			if (flipped)
				belts.get(i).setCoord(c.add(i * offset, 0));
		}
		setButtonLocation();
	}

	public void setButtonLocation() {
		lock.c = c.add(0, -20);
		hide.c = c.add(50, -20);
		flip.c = c.add(100, -20);
		move.c = c.add(150, -20);
		addRow.c = c.add(200, -20);
		delRow.c = c.add(215, -20);
		addBar.c = c.add(230, -20);
		delBar.c = c.add(280, -20);
	}

	private void addRow() {
		gui.beltManager.addBelt(this);
	}

	private void removeRow() {
		if (belts.size() > 1) {
			gui.beltManager.removeBelt(belts.get(belts.size() - 1));
		} else {
			destroyUI();
		}
	}

	private void addBar() {
		gui.beltManager.addBelt();
	}

	private void destroyUI() {
		if (!gui.beltManager.isLastUI()) {
			for (int i = 0; i < belts.size(); i++) {
				gui.beltManager.removeBelt(belts.get(i));
			}
			lock.destroy();
			hide.destroy();
			flip.destroy();
			move.destroy();
			addRow.destroy();
			delRow.destroy();
			addBar.destroy();
			delBar.destroy();
			gui.beltManager.removeBeltUI(this);
		}
	}

	private void resize() {
		for (int i = 0; i < belts.size(); i++) {
			belts.get(i).resize();
		}
		setCoord(c);
	}
}
