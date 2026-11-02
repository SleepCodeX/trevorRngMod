package dev.trevorroll;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2fc;

/** Immutable extraction snapshot; the renderer never reads live menu or hunt state. */
record SolidModelState(int model,double angle,double seconds,double progress,float opacity,
    int x0,int y0,int x1,int y1,float scale,Matrix3x2fc pose,
    ScreenRectangle scissorArea,ScreenRectangle bounds) implements PictureInPictureRenderState {}
