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

import java.util.*;
import java.util.stream.Collectors;

import haven.render.*;
import haven.Skeleton.Pose;
import haven.Skeleton.PoseMod;
import static haven.Composited.ED;
import static haven.Composited.MD;

public class Composite extends Drawable {
    public final static float ipollen = 0.2f;
    public final Indir<Resource> base;
    public final Composited comp;
    public int pseq;
    public List<MD> nmod;
    public List<ED> nequ;
    private Collection<ResData> nposes = null, tposes = null;
    private boolean nposesold, retainequ = false;
    private float tptime;
    private WrapMode tpmode;
    private List<MD> nmod2;
    boolean changed = false;
    private String resId = null;
    private List<String> poses = new LinkedList<>();
    
    public Composite(Gob gob, Indir<Resource> base) {
	super(gob);
	this.base = base;
	comp = new Composited(base.get().layer(Skeleton.Res.class).s);
	comp.eqowner = gob;
    }
    
    public void added(RenderTree.Slot slot) {
	slot.add(comp);
	super.added(slot);
    }

    public static List<PoseMod> loadposes(Collection<ResData> rl, Skeleton.ModOwner owner, Skeleton skel, boolean old) {
	List<PoseMod> mods = new ArrayList<PoseMod>(rl.size());
	for(ResData dat : rl) {
	    PoseMod mod = skel.mkposemod(owner, dat.res.get(), dat.sdt.clone());
	    if(old)
		mod.age();
	    mods.add(mod);
	}
	return(mods);
    }

    private List<PoseMod> loadposes(Collection<ResData> rl, Skeleton skel, boolean old) {
	return(loadposes(rl, gob, skel, old));
    }

    private List<PoseMod> loadposes(Collection<ResData> rl, Skeleton skel, WrapMode mode) {
	List<PoseMod> mods = new ArrayList<PoseMod>(rl.size());
	for(ResData dat : rl) {
	    for(Skeleton.ResPose p : dat.res.get().layers(Skeleton.ResPose.class))
		mods.add(p.forskel(gob, skel, (mode == null)?p.defmode:mode));
	}
	return(mods);
    }

    private void updequ() {
	retainequ = false;
	if(nmod != null) {
	    try {
		comp.chmod(nmod);
		nmod = null;
	    } catch(Loading l) {
	    }
	}
	if(nequ != null) {
	    try {
		comp.chequ(nequ);
		nequ = null;
	    } catch(Loading l) {
	    }
	}
    }

    public void ctick(double dt) {
	if(nposes != null) {
	    try {
		Composited.Poses np = comp.new Poses(loadposes(nposes, comp.skel, nposesold));
		np.set(nposesold?0:ipollen);
		this.poses = nposes.stream().map(pose -> pose.res.get().name).collect(Collectors.toList());
		gob.poseUpdated();
		nposes = null;
		updequ();
	    } catch(Loading e) {}
	} else if(tposes != null) {
	    try {
		final Composited.Poses cp = comp.poses;
		Composited.Poses np = comp.new Poses(loadposes(tposes, comp.skel, tpmode)) {
			protected void done() {
			    cp.set(ipollen);
			    updequ();
			}
		    };
		np.limit = tptime;
		np.set(ipollen);
		tposes = null;
		retainequ = true;
	    } catch(Loading e) {}
	} else if(!retainequ) {
	    updequ();
	}
	processResId();
	comp.tick(dt);
    }

    public void gtick(Render g) {
	comp.gtick(g);
    }

    public Resource getres() {
	return(base.get());
    }

    @Override
    public Indir<Resource> getires() {
	return base;
    }
    
    public Pose getpose() {
	return(comp.pose);
    }
    
    public void chposes(Collection<ResData> poses, boolean interp) {
	if(tposes != null)
	    tposes = null;
	nposes = poses;
	nposesold = !interp;
    }
    
    @Deprecated
    public void chposes(List<Indir<Resource>> poses, boolean interp) {
	chposes(ResData.wrap(poses), interp);
    }

    public void tposes(Collection<ResData> poses, WrapMode mode, float time) {
	this.tposes = poses;
	this.tpmode = mode;
	this.tptime = time;
    }
    
    @Deprecated
    public void tposes(List<Indir<Resource>> poses, WrapMode mode, float time) {
	tposes(ResData.wrap(poses), mode, time);
    }

    public void chmod(List<MD> mod) {
	nmod = mod;
	changed(mod);
    }

    public void chequ(List<ED> equ) {
	nequ = equ;
    }

    public Object staticp() {
	return(null);
    }
    
    public String resId() { return resId; }
    
    private String makeResId() {
	if(nmod2 == null) {return resId;}
	
	Set<String> res = new HashSet<>();
	String name;
	try {
	    name = base.get().name;
	    if("gfx/borka/body".equals(name)) {
		return name;
	    } else if(name != null) {
		for (MD mod : nmod2) {
		    String mname = mod.mod.get().name;
		    if(!name.equals(mname))
			res.add(mname);
		}
	    }
	} catch (Loading e) {
	    return null;
	}
	
	if(name == null) {
	    return null;
	} else if(res.isEmpty()) {
	    return name;
	} else {
	    String mods = res.stream().sorted().collect(Collectors.joining(","));
	    return String.format("%s[%s]", name, mods);
	}
    }
    private void changed(List<MD> mods) {
        nmod2 = mods;
        changed = true;
    }
    
    private void processResId() {
	if(changed) {
	    String id = makeResId();
	    if(id != null) {
		resId = id;
		changed = false;
		nmod2 = null;
		gob.idUpdated();
	    }
	}
    }
    
    public boolean hasPose(String request) {
	for (String pose : poses) {
	    if(pose.contains(request)) {
		return true;
	    }
	}
	return false;
    }
}
