package com.brandon3055.draconicevolution.api.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Created by brandon3055 on 22/01/2023
 * port-26.1.2: слоя RenderStateShard/CompositeState больше нет — 26 использует RenderPipeline/RenderSetup.
 * Кастомные типы заменены ближайшими vanilla-аналогами из RenderTypes (деградации отмечены):
 * - BOX_NO_DEPTH: был translucent position-color quads без depth-write/test -> debugFilledBox (depth включён — визуальная деградация окклюзии боксов).
 * - OUTLINE_TYPE: был position_color_normal LINES width=4 без depth-test -> vanilla lines() (толщина 1, depth включён).
 * Оригинальные определения сохранены в git-истории (ветка 1.21).
 */
public class DERenderTypes {

    public static final RenderType BOX_NO_DEPTH = RenderTypes.debugFilledBox();

    public static final RenderType OUTLINE_TYPE = RenderTypes.lines();

}
