package net.kafgchan.kitsunebi.forge.verification;

import net.kafgchan.kitsunebi.client.KitsunebiAltarScreen;
import net.kafgchan.kitsunebi.menu.KitsunebiAltarMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.nio.file.Files;
import java.nio.file.Path;

/** Opt-in render fixture. Uses no world or user save and exits only its own test client. */
public final class AltarGuiVerifier {
    private static boolean started;
    public static void tick(Minecraft mc) {
        if(!Boolean.getBoolean("kitsunebi.verifyAltarGui")||started||!(mc.screen instanceof TitleScreen))return;
        started=true;
        mc.setScreen(new Preview());
    }
    private static final class Preview extends Screen {
        private KitsunebiAltarScreen altar;
        private int frames,category;
        Preview(){super(Component.literal("Altar GUI verification"));}
        @Override protected void init(){create();}
        private void create() {
            if(category==4)return;
            Inventory inventory=new Inventory(null);
            KitsunebiAltarMenu menu=new KitsunebiAltarMenu(0,inventory);
            menu.setData(0,Math.min(category,2));menu.setData(1,category==0?2:0);menu.setData(2,16);
            if(category==3)menu.setData(3,1);
            menu.getSlot(0).set(new ItemStack(Items.BAMBOO,64));
            menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI,32));
            menu.getSlot(2).set(new ItemStack(Items.GLOW_INK_SAC,16));
            menu.getSlot(3).set(new ItemStack(Items.AMETHYST_SHARD,16));
            menu.getSlot(4).set(new ItemStack(Items.LIGHT_BLUE_DYE,16));
            altar=new KitsunebiAltarScreen(menu,inventory,Component.translatable("container.kitsunebi.altar"));
            altar.init(minecraft,width,height);
            if(category==0) verifyControls(altar,menu);
            if(category==2) {
                if(altar.children().stream().anyMatch(c->c instanceof net.minecraft.client.gui.components.AbstractSliderButton))
                    throw new AssertionError("Equipment tab must not contain a quantity slider");
                if(menu.getQuantity()!=1||menu.canCraft())throw new AssertionError("Equipment recipe accepted insufficient materials");
                for(var child:altar.children()) if(child instanceof net.minecraft.client.gui.components.Button button
                        &&java.util.Set.of("1","2","4","8","16","32","64").contains(button.getMessage().getString()))
                    throw new AssertionError("Equipment tab contains quantity presets");
                System.out.println("PASS: equipment crafting has no quantity presets or slider.");
            }
        }
        private void verifyControls(KitsunebiAltarScreen screen,KitsunebiAltarMenu menu) {
            try {
                var name=KitsunebiAltarScreen.class.getDeclaredMethod("ingredientName",String.class);name.setAccessible(true);
                for(int color=0;color<7;color++) {
                    menu.setData(1,color);
                    String actual=((Component)name.invoke(screen,"Dye")).getString();
                    String expected=net.kafgchan.kitsunebi.block.altar.AltarCraftingEngine.getDyeForColor(net.kafgchan.kitsunebi.registry.ModBlocks.COLORS[color]).getDescription().getString();
                    if(!actual.equals(expected))throw new AssertionError("Incorrect selected dye: "+actual);
                }
                menu.setData(1,2);
                var labels=java.util.List.of("Glow Dust / Glow Ink","Slime / Honey Block","Soul Sand / Soul Soil");
                var alternatives=java.util.List.of(java.util.List.of(Items.GLOWSTONE_DUST,Items.GLOW_INK_SAC),java.util.List.of(Items.SLIME_BLOCK,Items.HONEY_BLOCK),java.util.List.of(Items.SOUL_SAND,Items.SOUL_SOIL));
                for(int i=0;i<labels.size();i++) {
                    String actual=((Component)name.invoke(screen,labels.get(i))).getString();
                    for(var item:alternatives.get(i))if(!actual.contains(item.getDescription().getString()))throw new AssertionError("Abbreviated ingredient: "+actual);
                }
                var slider=(net.minecraft.client.gui.components.AbstractSliderButton)screen.children().stream().filter(c->c instanceof net.minecraft.client.gui.components.AbstractSliderButton).findFirst().orElseThrow();
                var value=net.minecraft.client.gui.components.AbstractSliderButton.class.getDeclaredField("value");value.setAccessible(true);
                var displayed=KitsunebiAltarScreen.class.getDeclaredField("displayedQuantity");displayed.setAccessible(true);
                var tick=KitsunebiAltarScreen.class.getDeclaredMethod("containerTick");tick.setAccessible(true);
                double y=slider.getY()+5;
                slider.mouseClicked(slider.getX()+4,y,0);
                for(int step=0;step<=256;step++) {
                    double fraction=step/256.0,x=slider.getX()+4+fraction*(slider.getWidth()-8);
                    slider.mouseDragged(x,y,0,1,0);tick.invoke(screen);
                    if(displayed.getInt(screen)!=1+(int)Math.round(fraction*63)||Math.abs(value.getDouble(slider)-fraction)>1e-8)
                        throw new AssertionError("Slider snapped to stale server state or integer at "+fraction);
                    if(menu.getQuantity()!=16)throw new AssertionError("Client preview changed server data");
                }
                slider.mouseReleased(slider.getX()+slider.getWidth(),y,0);tick.invoke(screen);
                if(displayed.getInt(screen)!=64)throw new AssertionError("Quantity reverted before acknowledgement");
                menu.setData(2,64);tick.invoke(screen);
                slider.setFocused(true);slider.keyPressed(263,0,0);
                if(displayed.getInt(screen)!=63)throw new AssertionError("Keyboard must move by exactly one");
                var preset=KitsunebiAltarScreen.class.getDeclaredMethod("quantity",int.class);preset.setAccessible(true);
                preset.invoke(screen,16);menu.setData(2,16);tick.invoke(screen);
                System.out.println("PASS: seven exact dye names, full alternative item names, 257 fractional drag positions under stale data, delayed acknowledgement and one-item keyboard steps.");
            } catch(ReflectiveOperationException e){throw new RuntimeException(e);}
        }
        @Override public void render(GuiGraphics g,int mx,int my,float partialTick) {
            if(category==4)renderAssets(g);
            else altar.render(g,category==3?(width-312)/2+(frames<15?85:110):-999,category==3?(height-236)/2+104:-999,partialTick);
            if(++frames==30) {
                g.flush();
                try(var image=Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                    Path path=Path.of("altar-gui-"+category+".png");
                    image.writeToFile(path);
                    System.out.println("PASS: rendered altar category "+category+" at "+width+"x"+height+" GUI pixels");
                }catch(Exception e){throw new RuntimeException(e);}
                if(++category<5){frames=0;create();}
                else {
                    try{Files.writeString(Path.of("altar-gui-verification.txt"),"PASS: categories, repair tooltip, stitched equipment sprite and armor model rendered at "+width+"x"+height);}
                    catch(Exception e){throw new RuntimeException(e);}
                    minecraft.stop();
                }
            }
        }
        private void renderAssets(GuiGraphics g) {
            var atlas=minecraft.getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS);
            var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kitsunebi","entity/kitsunebi_shrine_palette");
            if(!atlas.apply(id).contents().name().equals(id))throw new AssertionError("Equipment atlas sprite missing");
            g.fill(0,0,width,height,0xFF20232D);
            g.drawString(minecraft.font,"Equipment materials / actual game renderer",8,8,0xFFFFFFFF);
            var items=java.util.List.of(net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HEADDRESS,net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HAORI,
                    net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HAKAMA,net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_TABI,
                    net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_SWORD,net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_PICKAXE,
                    net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_AXE,net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_SHOVEL,net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HOE,
                    net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_GOHEI,net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_GOGGLES);
            for(int i=0;i<items.size();i++) {
                g.pose().pushPose();g.pose().translate(10+(i%3)*30,35+(i/3)*35,0);g.pose().scale(1.7F,1.7F,1.7F);
                g.renderItem(new ItemStack(items.get(i).get()),0,0);g.pose().popPose();
            }
            g.flush();
            var model=net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals.create();
            model.young=false;
            var wearer=new net.minecraft.world.entity.decoration.ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND,null) {
                @Override public ItemStack getItemBySlot(net.minecraft.world.entity.EquipmentSlot slot) {
                    return switch(slot) {
                        case HEAD->new ItemStack(net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HEADDRESS.get());
                        case CHEST->new ItemStack(net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HAORI.get());
                        case LEGS->new ItemStack(net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HAKAMA.get());
                        case FEET->new ItemStack(net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_TABI.get());
                        default->ItemStack.EMPTY;
                    };
                }
            };
            for(var slot:new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET}) {
                var stack=wearer.getItemBySlot(slot);
                var hooked=net.minecraftforge.client.extensions.common.IClientItemExtensions.of(stack).getHumanoidArmorModel(wearer,stack,slot,model);
                if(!(hooked instanceof net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisualModel<?>))throw new AssertionError("Armor render hook absent: "+slot);
            }
            for(int count=1;count<=4;count++) {
                g.pose().pushPose();g.pose().translate(120+(count-1)*52,65,80);g.pose().scale(42,42,42);
                g.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(count==1?150:0));
                var mannequin=net.minecraft.client.model.geom.builders.LayerDefinition.create(net.minecraft.client.model.HumanoidModel.createMesh(net.minecraft.client.model.geom.builders.CubeDeformation.NONE,0),128,128).bakeRoot();
                mannequin.getChild("head").render(g.pose(),g.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals.TEXTURE)),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,1);
                for(var slot:new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET}) {
                    model.configure(slot,count,0);
                    var buffer=g.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals.TEXTURE));
                    model.renderToBuffer(g.pose(),buffer,15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,1);
                }
                g.flush();g.pose().popPose();
                g.drawString(minecraft.font,new int[]{1,3,6,9}[count-1]+" tails",104+(count-1)*52,165,0xFFFFFFFF);
            }
        }
    }
}
