package xyz.mamotromico;

import haven.GOut;
import haven.GameUI;
import haven.Widget;
import haven.Coord;

public class ScriptWnd extends GameUI.Hidewnd {
    public ScriptWnd() {
        super(Coord.z, "Scripts");
    }
    
    public static class ScriptList extends Widget {
        public Widget scripts;
    
        private ScriptList(Coord sz, Widget scripts) {
            super(sz);
            this.scripts = scripts;
        }
        
        private void upd() {
        
        }
        
        public void draw(GOut g) {
            upd();
            super.draw(g);
            
        }
    }
}
