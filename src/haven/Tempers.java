/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import haven.RichText.Foundry;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.*;

import static haven.PUtils.*;

public class Tempers extends SIWidget {
	public static final BufferedImage bg = Resource.loadimg("gfx/hud/tempers/bg");
	public static final BufferedImage[] bars, sbars, fbars;
	public static final BufferedImage lcap = Resource.loadimg("gfx/hud/tempers/lcap");
	public static final BufferedImage rcap = Resource.loadimg("gfx/hud/tempers/rcap");
	public static final BufferedImage[] gbtni = { Resource.loadimg("gfx/hud/tempers/gbtn"), Resource.loadimg("gfx/hud/tempers/gbtn"), Resource.loadimg("gfx/hud/tempers/gbtn"), };
	public static final Coord boxc = new Coord(96, 0), boxsz = new Coord(339, 62);
	static final Color softc = new Color(64, 64, 64);
	static final Color foodc = new Color(128, 128, 0);
	static final Coord[] mc = { new Coord(295, 11), new Coord(235, 11), new Coord(235, 35), new Coord(295, 35) };
	static final String[] anm = { "blood", "phlegm", "ybile", "bbile" };
	static final String[] rnm = { "Blood", "Phlegm", "Yellow Bile", "Black Bile" };
	int[] soft = new int[4], hard = new int[4];
	int[] lmax = new int[4];
	public boolean gavail = true;
	private HumourText humourText;
	Tex tt = null;
	Tex blood, phlegm, yellow, black;

	public enum Humour {
		BLOOD(0, "Blood"), PHLEGM(1, "Phlegm"), YELLOW(2, "Yellow Bile"), BLACK(3, "Black Bile");

		public int id;
		public String name;

		private Humour(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	public class HumourText {
		final Foundry foundry;
		public Coord bloodOffset;
		public Coord phlegmOffset;
		public Coord yellowOffset;
		public Coord blackOffset;
		private int[][] oldHumours;
		private Tex[] values;
		private int softIndex, hardIndex, maxIndex;

		public HumourText() {
			foundry = new RichText.Foundry(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, TextAttribute.FOREGROUND, new Color(32, 32, 64), TextAttribute.SIZE, 12);
			bloodOffset = new Coord(325, 10);
			phlegmOffset = new Coord(140, 10);
			yellowOffset = new Coord(140, 33);
			blackOffset = new Coord(325, 33);
			softIndex = 0;
			hardIndex = 1;
			maxIndex = 2;
			int[] bloodOld = new int[] { 0, 0, 0 };
			int[] phlegmOld = new int[] { 0, 0, 0 };
			int[] yellowOld = new int[] { 0, 0, 0 };
			int[] blackOld = new int[] { 0, 0, 0 };
			oldHumours = new int[][] { bloodOld, phlegmOld, yellowOld, blackOld };
			values = new Tex[4];
		}

		public Tex getTex(Humour type) {
			if (isCurrent(type)) {
				return values[type.id];
			} else {
				return getCurrent(type);
			}
		}

		private boolean isCurrent(Humour type) {
			return (soft[type.id] == oldHumours[type.id][softIndex] && hard[type.id] == oldHumours[type.id][hardIndex] && lmax[type.id] == oldHumours[type.id][maxIndex]);
		}

		private Tex getCurrent(Humour type) {
			int[] current = new int[] { soft[type.id] / 100, hard[type.id] / 100, lmax[type.id] / 100 };
			String renderText = (current[0] / 10 + "." + current[0] % 10 + " / " + current[1] / 10 + "." + current[1] % 10 + " / " + current[2] / 10 + "." + current[2] % 10);
			Tex texture = new TexI(Utils.outline2(foundry.render(renderText).img, new Color(240, 240, 240), false));
			values[type.id] = texture;
			oldHumours[type.id] = current;
			return texture;
		}
	}

	Widget gbtn;

	static {
		int n = anm.length;
		BufferedImage[] b = new BufferedImage[n];
		BufferedImage[] s = new BufferedImage[n];
		BufferedImage[] f = new BufferedImage[n];
		for (int i = 0; i < n; i++) {
			b[i] = Resource.loadimg("gfx/hud/tempers/" + anm[i]);
			s[i] = monochromize(b[i], softc);
			f[i] = monochromize(b[i], foodc);
		}
		bars = b;
		sbars = s;
		fbars = f;
	}

	public Tempers(Coord c, Widget parent) {
		super(c, imgsz(bg), parent);
		this.humourText = new HumourText();
	}

	private FoodInfo lfood;

	public void tick(double dt) {
		int[] max = new int[4];
		for (int i = 0; i < 4; i++) {
			max[i] = ui.sess.glob.cattr.get(anm[i]).comp;
			if (max[i] == 0)
				return;
			if (max[i] != lmax[i]) {
				redraw();
				tt = null;
			}
		}
		lmax = max;

		if (gavail && (gbtn == null)) {
			gbtn = new IButton(Coord.z, parent, gbtni[0], gbtni[1], gbtni[2]) {
				public void reqdestroy() {
					new NormAnim(0.25) {
						public void ntick(double a) {
							c = new Coord(Tempers.this.c.x + ((Tempers.this.sz.x - sz.x) / 2), (int) (Tempers.this.c.y + boxsz.y - (a * sz.y)));
							if (a == 1.0)
								destroy();
						}
					};
				}

				public void click() {
					getparent(GameUI.class).act("gobble");
				}

				public void presize() {
					c = new Coord(Tempers.this.c.x + ((Tempers.this.sz.x - sz.x) / 2), (int) (Tempers.this.c.y + boxsz.y));
				}

				{
					if (!Tempers.this.visible)
						hide();
					new NormAnim(0.25) {
						public void ntick(double a) {
							double f = Math.abs(1.0 - (6 * Math.pow(a, 2)) + (5 * Math.pow(a, 3)));
							c = new Coord(Tempers.this.c.x + ((Tempers.this.sz.x - sz.x) / 2), (int) (Tempers.this.c.y + boxsz.y - (f * sz.y)));
						}
					}.ntick(0.0);
				}
			};
			raise();
		} else if (!gavail && (gbtn != null)) {
			gbtn.reqdestroy();
			gbtn = null;
		}

		FoodInfo food = null;
		if (ui.lasttip instanceof WItem.ItemTip) {
			try {
				food = ItemInfo.find(FoodInfo.class, ((WItem.ItemTip) ui.lasttip).item().info());
			} catch (Loading e) {
			}
		}
		if (lfood != food) {
			lfood = food;
			redraw();
		}
	}

	public void show() {
		if (gbtn != null)
			gbtn.show();
	}

	public void hide() {
		if (gbtn != null)
			gbtn.hide();
	}

	public static WritableRaster rmeter(Raster tex, int val, int max) {
		int w = 1 + (Utils.clip(val, 0, max) * (tex.getWidth() - 1)) / Math.max(max, 1);
		WritableRaster bar = copy(tex);
		gayblit(bar, 3, new Coord(w - rcap.getWidth(), 0), rcap.getRaster(), 0, Coord.z);
		for (int y = 0; y < bar.getHeight(); y++) {
			for (int x = w; x < bar.getWidth(); x++)
				bar.setSample(x, y, 3, 0);
		}
		return (bar);
	}

	public static WritableRaster lmeter(Raster tex, int val, int max) {
		int w = 1 + (Utils.clip(val, 0, max) * (tex.getWidth() - 1)) / Math.max(max, 1);
		WritableRaster bar = copy(tex);
		gayblit(bar, 3, new Coord(bar.getWidth() - w, 0), lcap.getRaster(), 0, Coord.z);
		for (int y = 0; y < bar.getHeight(); y++) {
			for (int x = 0; x < bar.getWidth() - w; x++)
				bar.setSample(x, y, 3, 0);
		}
		return (bar);
	}

	private WritableRaster rfmeter(FoodInfo food, int t) {
		return (alphablit(rmeter(fbars[t].getRaster(), soft[t] + food.tempers[t], lmax[t]), rmeter(sbars[t].getRaster(), soft[t], lmax[t]), Coord.z));
	}

	private WritableRaster lfmeter(FoodInfo food, int t) {
		return (alphablit(lmeter(fbars[t].getRaster(), soft[t] + food.tempers[t], lmax[t]), lmeter(sbars[t].getRaster(), soft[t], lmax[t]), Coord.z));
	}

	@Override
	public void draw(GOut g) {
		super.draw(g);
		g.image(humourText.getTex(Humour.BLOOD), humourText.bloodOffset);
		g.image(humourText.getTex(Humour.PHLEGM), humourText.phlegmOffset);
		g.image(humourText.getTex(Humour.YELLOW), humourText.yellowOffset);
		g.image(humourText.getTex(Humour.BLACK), humourText.blackOffset);
	}

	public void draw(BufferedImage buf) {
		WritableRaster dst = buf.getRaster();
		blit(dst, bg.getRaster(), Coord.z);

		if (lfood != null) {
			alphablit(dst, rfmeter(lfood, 0), mc[0]);
			alphablit(dst, lfmeter(lfood, 1), mc[1].sub(bars[1].getWidth() - 1, 0));
			alphablit(dst, lfmeter(lfood, 2), mc[2].sub(bars[2].getWidth() - 1, 0));
			alphablit(dst, rfmeter(lfood, 3), mc[3]);
		} else {
			if (soft[0] > hard[0])
				alphablit(dst, rmeter(sbars[0].getRaster(), soft[0], lmax[0]), mc[0]);
			if (soft[1] > hard[1])
				alphablit(dst, lmeter(sbars[1].getRaster(), soft[1], lmax[1]), mc[1].sub(bars[1].getWidth() - 1, 0));
			if (soft[2] > hard[2])
				alphablit(dst, lmeter(sbars[2].getRaster(), soft[2], lmax[2]), mc[2].sub(bars[2].getWidth() - 1, 0));
			if (soft[3] > hard[3])
				alphablit(dst, rmeter(sbars[3].getRaster(), soft[3], lmax[3]), mc[3]);
		}

		alphablit(dst, rmeter(bars[0].getRaster(), hard[0], lmax[0]), mc[0]);
		alphablit(dst, lmeter(bars[1].getRaster(), hard[1], lmax[1]), mc[1].sub(bars[1].getWidth() - 1, 0));
		alphablit(dst, lmeter(bars[2].getRaster(), hard[2], lmax[2]), mc[2].sub(bars[2].getWidth() - 1, 0));
		alphablit(dst, rmeter(bars[3].getRaster(), hard[3], lmax[3]), mc[3]);
	}

	public void upds(int[] n) {
		this.soft = n;
		redraw();
		tt = null;
	}

	public void updh(int[] n) {
		this.hard = n;
		redraw();
		tt = null;
	}

	public boolean mousedown(Coord c, int button) {
		if (bg.getRaster().getSample(c.x, c.y, 3) > 128)
			return (true);
		return (super.mousedown(c, button));
	}

	public Object tooltip(Coord c, Widget prev) {
		if (c.isect(boxc, boxsz)) {
			if (tt == null) {
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < 4; i++)
					buf.append(String.format("%s: %s/%s/%s\n", rnm[i], Utils.fpformat(hard[i], 3, 1), Utils.fpformat(soft[i], 3, 1), Utils.fpformat(lmax[i], 3, 1)));
				tt = RichText.render(buf.toString(), 0).tex();
			}
			return (tt);
		}
		return (null);
	}
}
