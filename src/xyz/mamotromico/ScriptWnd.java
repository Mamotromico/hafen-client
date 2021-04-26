package xyz.mamotromico;

import haven.*;
import xyz.mamotromico.kbot.KREPL;

public class ScriptWnd extends GameUI.Hidewnd {
    KREPL engine;
    public ScriptWnd() {
        super(Coord.z, "Scripts");
        engine = KREPL.getInstance();
        add(new Button(UI.scale(100), "Run Script", false).action(() -> {
        
        }), new Coord(0,5));
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
