package haven;

import java.util.ArrayList;

public class ToolBeltManager {
	private int max = 12;
	private ToolBelt[] belts;
	private ArrayList<ToolBeltUI> beltUIs;
	private GameUI parent;
	@SuppressWarnings("unchecked")
	public Indir<Resource>[] contents = new Indir[144];

	public ToolBeltManager(GameUI parent) {
		this.parent = parent;
		belts = new ToolBelt[12];
		this.beltUIs = new ArrayList<ToolBeltUI>();
		initBelts();
	}

	private void initBelts() {

		// String val = Utils.getpref("belttype", "n");
		// TODO settings
		ToolBelt initialBelt = new ToolBelt(new Coord(0, 0), parent, 0, contents);
		ToolBeltUI initialUI = new ToolBeltUI(initialBelt, parent);
		belts[0] = initialBelt;
		beltUIs.add(initialUI);
	}

	public void addBelt() {
		addBelt(null);
	}

	private int getNewBeltID() {
		for (int i = 0; i < max; i++) {
			if (belts[i] == null) {
				return i;
			}
		}
		return -1;
	}

	public void addBelt(ToolBeltUI beltUI) {
		int id = getNewBeltID();
		if (id != -1) {
			ToolBelt belt = new ToolBelt(new Coord(0, 0), parent, id, contents);
			if (beltUI == null) {
				beltUI = new ToolBeltUI(belt, parent);
				beltUIs.add(beltUI);
				beltUI.setCoord(new Coord(500, 500));
			} else {
				beltUI.addBelt(belt);
			}
			belts[id] = belt;
		}
	}

	public void switchNumber(int beltID) {
		ToolBelt belt = belts[beltID];
		for (int i = beltID; i < max; i++) {
			if (belts[i] == null) {
				switchNumber(belt, i);
				return;
			}
		}
		for (int i = 0; i < belt.getID(); i++) {
			if (belts[i] == null) {
				switchNumber(belt, i);
				return;
			}
		}
	}

	private void switchNumber(ToolBelt old, int to) {
		ToolBelt newBelt = new ToolBelt(new Coord(0, 0), parent, to, contents);
		belts[to] = newBelt;
		old.getUI().replaceBelt(old, newBelt);
		belts[old.getID()] = null;
	}

	public boolean isLastUI() {
		return (beltUIs.size() <= 1);
	}

	public void resize(int x, int y) { // move the belt with gameUI on init,
										// TODO better name;
		ToolBeltUI beltUI;
		for (int i = 0; i < beltUIs.size(); i++) {
			beltUI = beltUIs.get(i);
			if (beltUI != null) {
				beltUI.setCoord(new Coord(x, y - belts[0].sz.y));
			}
		}
	}

	public void removeBelt(ToolBelt belt) {
		belt.destroy();
		belts[belt.getID()] = null;
		for (int i = 0; i < beltUIs.size(); i++) {
			beltUIs.get(i).removeBelt(belt.getID());
		}
	}

	public void removeBeltUI(ToolBeltUI beltUI) {
		beltUIs.remove(beltUI);
	}

	public void uimsg(String msg, Object... args) {
		int slot = (Integer) args[0];
		if (args.length < 2) {
			setBelt(slot, null);
			return;
		}
		int replace = (Integer) args[1];
		setBelt(slot, replace);
	}

	public void setBelt(Integer slot, Integer replace) {
		if ((belts[slot / 12] == null) || (!belts[slot / 12].getUI().isLocked())) {
			if (replace == null) {
				contents[slot] = null;
			} else {
				contents[slot] = parent.ui.sess.getres(replace);
			}
		}
	}
}
