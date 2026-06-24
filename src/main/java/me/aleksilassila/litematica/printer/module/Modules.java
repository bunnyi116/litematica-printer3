package me.aleksilassila.litematica.printer.module;

import com.google.common.collect.ImmutableList;
import me.aleksilassila.litematica.printer.module.modules.*;

public class Modules {
    public final static GuiModule GUI = new GuiModule();
    public final static PrintModule PRINT = new PrintModule();
    public final static FillModule FILL = new FillModule();
    public final static MineModule MINE = new MineModule();
    public final static FluidModule FLUID = new FluidModule();
    public final static BedrockModule BEDROCK = new BedrockModule();
    public final static ImmutableList<Module> VALUES = ImmutableList.of(GUI, MINE, FLUID, PRINT, FILL, BEDROCK);
}
