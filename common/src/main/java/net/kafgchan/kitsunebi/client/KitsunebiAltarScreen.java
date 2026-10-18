package net.kafgchan.kitsunebi.client;

import net.kafgchan.kitsunebi.menu.KitsunebiAltarMenu;
import net.kafgchan.kitsunebi.block.altar.AltarCraftingEngine;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.FormattedCharSequence;
import java.util.ArrayList;
import java.util.List;

/** Compact altar workspace: item-based selection, full ingredient list and explicit crafting. */
public class KitsunebiAltarScreen extends AbstractContainerScreen<KitsunebiAltarMenu> {
    private static final int INK=0xFFE9E3F0,MUTED=0xFFB1A9BF,GOLD=0xFFC7A963;
    private static final int[] PRESETS={1,2,4,8,16,32,64};
    private final List<ItemChoice> choices=new ArrayList<>();
    private Button craft;
    private QuantitySlider slider;
    private int shownCategory=-1;
    private int displayedQuantity=1;
    private int pendingQuantity=-1;

    public KitsunebiAltarScreen(KitsunebiAltarMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title);
        imageWidth=312;imageHeight=236;
        inventoryLabelY=142;
    }
    private static Component text(String key){return Component.translatable("gui.kitsunebi.altar."+key);}
    private void send(int id) {
        if(minecraft!=null&&minecraft.gameMode!=null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);
    }
    @Override protected void init(){super.init();displayedQuantity=menu.getQuantity();pendingQuantity=-1;rebuild();}
    private void rebuild() {
        clearWidgets();choices.clear();shownCategory=menu.getCategoryIndex();
        String[] keys={"category.kitsunebi.fire","category.kitsunebi.phantom","category.kitsunebi.equipment"};
        for(int i=0;i<3;i++) {
            final int category=i;
            Button tab=Button.builder(Component.translatable(keys[i]),b->send(category))
                    .bounds(leftPos+12+i*100,topPos+17,i==2?88:96,16).build();
            tab.active=i!=shownCategory;addRenderableWidget(tab);
        }
        for(int i=0;i<menu.getSelectionCount();i++) {
            ItemChoice choice=new ItemChoice(i,menu.getSelectionStack(i),leftPos+78+(i%5)*18,topPos+46+(i/5)*18);
            choices.add(choice);addRenderableWidget(choice);
        }
        slider=null;
        if(menu.isEquipment()){displayedQuantity=1;pendingQuantity=-1;}
        for(int i=0;!menu.isEquipment()&&i<PRESETS.length;i++) {
            final int q=PRESETS[i];
            addRenderableWidget(Button.builder(Component.literal(""+q),b->quantity(q))
                    .bounds(leftPos+184+i*16,topPos+160,16,14).build());
        }
        if(!menu.isEquipment()) {
            slider=new QuantitySlider(leftPos+184,topPos+178,112,18,displayedQuantity);
            addRenderableWidget(slider);
        }
        craft=Button.builder(text("craft"),b->{commitQuantity();send(KitsunebiAltarMenu.CRAFT_BUTTON);})
                .bounds(leftPos+184,topPos+211,112,18).build();
        addRenderableWidget(craft);
        updateCraft();
    }
    private void quantity(int q){displayedQuantity=q;slider.showQuantity(q);commitQuantity();updateCraft();}
    private void commitQuantity(){if(!menu.isEquipment()){pendingQuantity=displayedQuantity;send(1000+displayedQuantity);}}
    private void updateCraft() {
        craft.active=menu.canCraft(displayedQuantity);
        craft.setMessage(text(menu.isEquipment()?"coming_soon":"craft"));
        craft.setTooltip(Tooltip.create(text(menu.isEquipment()?"next_update":craft.active?"ready":menu.getCurrentResult(displayedQuantity).canCraft()?"output_full":"missing")));
    }
    @Override protected void containerTick() {
        super.containerTick();
        if(shownCategory!=menu.getCategoryIndex()) rebuild();
        if(pendingQuantity==menu.getQuantity()) pendingQuantity=-1;
        if(slider!=null&&!slider.dragging&&pendingQuantity<0&&displayedQuantity!=menu.getQuantity()) {
            displayedQuantity=menu.getQuantity();slider.showQuantity(displayedQuantity);
        }
        updateCraft();
    }

    private void panel(GuiGraphics g,int x,int y,int w,int h) {
        g.fill(x,y,x+w,y+h,0xFF110F19);g.fill(x+1,y+1,x+w-1,y+h-1,0xFF383141);
        g.fill(x+2,y+2,x+w-2,y+h-2,0xFF24212D);
    }
    private void slot(GuiGraphics g,int x,int y,boolean output) {
        g.fill(x-1,y-1,x+17,y+17,output?GOLD:0xFF6D617B);
        g.fill(x,y,x+16,y+16,0xFF14121C);
        g.fill(x,y,x+16,y+1,0xFF0A0910);
    }
    @Override protected void renderBg(GuiGraphics g,float partialTick,int mx,int my) {
        int x=leftPos,y=topPos;
        panel(g,x,y,imageWidth,imageHeight);
        g.fill(x+3,y+3,x+imageWidth-3,y+4,GOLD);
        panel(g,x+7,y+35,62,102);panel(g,x+73,y+35,101,102);
        panel(g,x+179,y+35,122,102);panel(g,x+179,y+140,122,92);
        for(var s:menu.slots) slot(g,x+s.x,y+s.y,s.index==KitsunebiAltarMenu.RESULT_SLOT);
        slot(g,x+210,y+50,false);
        ItemStack preview=menu.getSelectionStack(menu.getSelectionIndex());
        if(!preview.isEmpty()) {
            preview.setCount(displayedQuantity);
            g.renderItem(preview,x+210,y+50);g.renderItemDecorations(font,preview,x+210,y+50);
        }
        g.drawString(font,Component.literal("→"),x+245,y+54,GOLD,false);
    }
    private void label(GuiGraphics g,Component c,int x,int y,int maxWidth,int color) {
        String value=c.getString();
        if(font.width(c)>maxWidth) value=font.plainSubstrByWidth(value,Math.max(0,maxWidth-font.width("…")))+"…";
        g.drawString(font,value,x,y,color,false);
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my) {
        g.drawString(font,title,12,6,INK,false);
        g.drawString(font,text("input"),12,36,MUTED,false);
        g.drawString(font,text(menu.isEquipment()?"equipment_select":menu.getCategoryIndex()==0?"fire_select":"shape"),78,36,MUTED,false);
        g.drawString(font,text("preview"),184,37,MUTED,false);
        g.drawString(font,text("output"),264,37,MUTED,false);
        label(g,menu.isEquipment()?text("placeholder"):menu.getSelectionStack(menu.getSelectionIndex()).getHoverName(),184,72,112,INK);
        var result=menu.getCurrentResult(displayedQuantity);
        IngredientLayout layout=ingredientLayout();
        g.pose().pushPose();g.pose().translate(184,82,0);g.pose().scale(layout.scale(),layout.scale(),1);
        for(int i=0;i<layout.lines().size();i++) {
            IngredientLine line=layout.lines().get(i);
            g.drawString(font,line.text(),0,i*9,line.color(),false);
            if(!line.count().isEmpty()) g.drawString(font,line.count(),(int)(112/layout.scale())-font.width(line.count()),i*9,line.color(),false);
        }
        g.pose().popPose();
        g.drawString(font,playerInventoryTitle,12,142,MUTED,false);
        if(!menu.isEquipment())g.drawString(font,text("quantity"),184,148,MUTED,false);
        else g.drawWordWrap(font,text("next_update"),184,150,112,MUTED);
        label(g,text(menu.isEquipment()?"coming_soon":menu.canCraft(displayedQuantity)?"ready":result.canCraft()?"output_full":"missing"),184,199,112,menu.canCraft(displayedQuantity)?0xFFA8D6A3:MUTED);
        // Full selected name is available on hover, independent of truncation.
        Component selected=menu.isEquipment()?text("placeholder"):menu.getSelectionStack(menu.getSelectionIndex()).getHoverName();
        var lines=font.split(selected,90);
        for(int i=0;i<Math.min(2,lines.size());i++) g.drawString(font,lines.get(i),78,120+i*9,MUTED,false);
    }
    private Component ingredientName(String label) {
        List<Component> parts=ingredientParts(label);
        var name=Component.empty();
        for(int i=0;i<parts.size();i++) {if(i>0)name.append(" / ");name.append(parts.get(i));}
        return name;
    }
    private List<Component> ingredientParts(String label) {
        // Vanilla item names provide exact material identity and the player's language.
        switch(label) {
            case "Phantom Block": return List.of(new ItemStack(ModBlocks.PHANTOM_BLOCK.get()).getHoverName());
            case "Glow Dust / Glow Ink": return List.of(Items.GLOWSTONE_DUST.getDescription(),Items.GLOW_INK_SAC.getDescription());
            case "Slime / Honey Block": return List.of(Items.SLIME_BLOCK.getDescription(),Items.HONEY_BLOCK.getDescription());
            case "Soul Sand / Soul Soil": return List.of(Items.SOUL_SAND.getDescription(),Items.SOUL_SOIL.getDescription());
            case "Dye": return List.of(AltarCraftingEngine.getDyeForColor(ModBlocks.COLORS[menu.getSelectionIndex()]).getDescription());
        }
        String key=switch(label) {
            case "Amethyst Shard"->"amethyst";
            case "Bamboo"->"bamboo";case "Lapis Lazuli"->"lapis";
            case "Kitsunebi Fire"->"fire";case "Amethyst Block"->"amethyst_block";
            case "Kitsunebi Block"->"block";
            case "Phantom Slab (H/V)"->"half";case "Phantom Quarter (H/V)"->"quarter";
            case "Stick"->"stick";default->null;
        };
        return List.of(key==null?Component.literal(label):text("ingredient."+key));
    }
    private record IngredientLine(FormattedCharSequence text,String count,int color,Component tooltip){}
    private record IngredientLayout(List<IngredientLine> lines,float scale){}
    private IngredientLayout ingredientLayout() {
        List<IngredientLine> rows=ingredientLines(1);
        float scale=Math.min(1,54f/(rows.size()*9));
        if(scale<1) rows=ingredientLines(scale);
        return new IngredientLayout(rows,scale);
    }
    private List<IngredientLine> ingredientLines(float scale) {
        List<IngredientLine> rows=new ArrayList<>();
        for(var ingredient:menu.getCurrentResult(displayedQuantity).ingredients()) {
            String count=ingredient.providedCount()+"/"+ingredient.requiredCount();
            Component tooltip=ingredientName(ingredient.label()).copy().append(" "+count);
            List<Component> parts=ingredientParts(ingredient.label());
            int color=ingredient.isSatisfied()?0xFFA8D6A3:0xFFFFA69E;
            for(int p=0;p<parts.size();p++) {
                boolean last=p==parts.size()-1;
                Component part=p==0?parts.get(p):Component.literal("/ ").append(parts.get(p));
                int width=(int)(112/scale)-(last?font.width(count)+4:0);
                var lines=font.split(part,width);
                for(int i=0;i<lines.size();i++) rows.add(new IngredientLine(lines.get(i),last&&i==lines.size()-1?count:"",color,tooltip));
            }
        }
        return rows;
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partialTick) {
        super.render(g,mx,my,partialTick);renderTooltip(g,mx,my);
        if(isHovering(210,50,16,16,mx,my)||isHovering(184,72,112,10,mx,my))
            g.renderTooltip(font,menu.getSelectionStack(menu.getSelectionIndex()),mx,my);
        IngredientLayout layout=ingredientLayout();
        for(int i=0;i<layout.lines().size();i++) if(isHovering(184,(int)(82+i*9*layout.scale()),112,(int)Math.ceil(9*layout.scale()),mx,my)) {
            g.renderTooltip(font,layout.lines().get(i).tooltip(),mx,my);
        }
    }
    private final class ItemChoice extends AbstractButton {
        private final int index;private final ItemStack stack;
        ItemChoice(int index,ItemStack stack,int x,int y) {
            super(x,y,18,18,stack.isEmpty()?text("placeholder"):stack.getHoverName());this.index=index;this.stack=stack;
            active=!stack.isEmpty();
            setTooltip(Tooltip.create(stack.isEmpty()?text("next_update"):stack.getHoverName()));
        }
        @Override public void onPress(){send(100+index);}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partialTick) {
            boolean selected=!stack.isEmpty()&&index==menu.getSelectionIndex();
            g.fill(getX(),getY(),getX()+18,getY()+18,selected?GOLD:isHoveredOrFocused()?0xFFC7AFD9:0xFF63566F);
            g.fill(getX()+1,getY()+1,getX()+17,getY()+17,selected?0xFF50405E:0xFF1B1823);
            if(stack.isEmpty())g.drawCenteredString(font,Component.literal("+"),getX()+9,getY()+5,MUTED);
            else g.renderItem(stack,getX()+1,getY()+1);
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput output){defaultButtonNarrationText(output);}
    }
    private final class QuantitySlider extends AbstractSliderButton {
        private boolean dragging;
        QuantitySlider(int x,int y,int w,int h,int q){super(x,y,w,h,Component.empty(),(q-1)/63.0);updateMessage();}
        void showQuantity(int q){value=(q-1)/63.0;updateMessage();}
        @Override protected void updateMessage(){setMessage(Component.literal(""+(1+(int)Math.round(value*63))));}
        @Override protected void applyValue(){displayedQuantity=1+(int)Math.round(value*63);updateCraft();}
        @Override public boolean mouseClicked(double x,double y,int button) {
            dragging=true;
            boolean handled=super.mouseClicked(x,y,button);dragging=handled;return handled;
        }
        @Override public boolean mouseReleased(double x,double y,int button){
            boolean commit=dragging;dragging=false;
            boolean handled=super.mouseReleased(x,y,button);
            if(commit)commitQuantity();return handled;
        }
        @Override public boolean keyPressed(int key,int scanCode,int modifiers) {
            if(isFocused()&&(key==262||key==263||key==268||key==269)) {
                int q=key==268?1:key==269?64:Math.max(1,Math.min(64,displayedQuantity+(key==262?1:-1)));
                quantity(q);return true;
            }
            return super.keyPressed(key,scanCode,modifiers);
        }
        @Override public void renderWidget(GuiGraphics g,int mx,int my,float partialTick) {
            g.fill(getX(),getY(),getX()+width,getY()+height,0xFF100E17);
            g.fill(getX()+1,getY()+height/2-1,getX()+width-1,getY()+height/2+1,0xFF63566F);
            // Preserve fractional mouse position; integer quantity is only a preview/recipe value.
            g.pose().pushPose();g.pose().translate(getX()+value*(width-8),getY(),0);
            g.fill(0,1,8,height-1,isHoveredOrFocused()?0xFFE2C688:GOLD);
            g.fill(1,2,7,height-2,0xFF7F6A4B);g.pose().popPose();
            g.drawCenteredString(font,getMessage(),getX()+width/2,getY()+(height-8)/2,INK);
        }
    }
}
