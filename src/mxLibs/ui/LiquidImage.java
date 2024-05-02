package mxLibs.ui;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;

public class LiquidImage extends Stack {


    public LiquidImage(TextureRegion region, float amount) {

        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f).scaling(Scaling.fit);
        }));

        if (amount != 0) {
            add(new Table(t -> {
                t.left().bottom();
                t.add(amount * 60f + "").style(Styles.outlineLabel);
                t.pack();
            }));
        }
    }

    public LiquidImage(LiquidStack stack) {
        this(stack.liquid.uiIcon, stack.amount);
    }

}
