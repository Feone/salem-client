package haven.minimap;

import haven.*;

public class Marker {
	public final String name;
	public final Gob gob;

	public Marker(String name, Gob gob) {
		this.name = name;
		this.gob = gob;
	}

	public boolean hit(Coord c) {
		Coord3f ptc3f = gob.getc();
		if (ptc3f == null)
			return false;
		Coord p = new Coord((int) ptc3f.x, (int) ptc3f.y);
		int radius = 4;
		return (c.x - p.x) * (c.x - p.x) + (c.y - p.y) * (c.y - p.y) < radius * radius * MCache.tilesz.x * MCache.tilesz.y;
	}

	public void draw(GOut g, Coord c) {
		Coord3f ptc3f = gob.getc();
		if (ptc3f == null)
			return;
		Coord ptc = new Coord((int) ptc3f.x, (int) ptc3f.y);
		ptc = ptc.div(MCache.tilesz).add(c);

		try {
			GobIcon icon = gob.getattr(GobIcon.class);
			if (icon != null) {
				Tex tex = icon.tex();
				g.image(tex, ptc.sub(tex.sz().div(2)));
				return;
			}
		} catch (Loading l) {
		}
	}
}