package net.kafgchan.kitsunebi.menu;

import net.kafgchan.kitsunebi.block.altar.AltarCraftingEngine;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.kafgchan.kitsunebi.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative selection and atomic, explicit crafting into a paid output buffer. */
public class KitsunebiAltarMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT_COUNT=15, RESULT_SLOT=15, INV_SLOT_START=16, USE_ROW_SLOT_END=52;
    public static final int CRAFT_BUTTON=2000;
    private final ContainerLevelAccess access;
    private final ContainerData data=new SimpleContainerData(3);
    private boolean crafting;
    private final Container inputContainer=new SimpleContainer(INPUT_SLOT_COUNT) {
        @Override public void setChanged() {
            super.setChanged();
            if(!crafting) KitsunebiAltarMenu.this.slotsChanged(this);
        }
    };
    private final Container output=new SimpleContainer(1);

    public KitsunebiAltarMenu(int id,Inventory inventory) {this(id,inventory,ContainerLevelAccess.NULL);}
    public KitsunebiAltarMenu(int id,Inventory inventory,ContainerLevelAccess access) {
        super(ModMenus.KITSUNEBI_ALTAR_MENU.get(),id);
        this.access=access;
        data.set(2,1);
        addDataSlots(data);
        for(int row=0;row<5;row++) for(int col=0;col<3;col++)
            addSlot(new Slot(inputContainer,col+row*3,12+col*18,46+row*18));
        addSlot(new Slot(output,0,274,50) {
            @Override public boolean mayPlace(ItemStack stack){return false;}
        });
        for(int row=0;row<3;row++) for(int col=0;col<9;col++)
            addSlot(new Slot(inventory,col+row*9+9,12+col*18,154+row*18));
        for(int col=0;col<9;col++) addSlot(new Slot(inventory,col,12+col*18,212));
    }

    private AltarCraftingEngine.Category getRecipeCategory(){
        return getCategoryIndex()==1?AltarCraftingEngine.Category.PHANTOM:
                getSelectionIndex()==7?AltarCraftingEngine.Category.ARCHITECTURAL:AltarCraftingEngine.Category.KITSUNEBI;
    }
    private int getRecipeSelection(){return getCategoryIndex()==0&&getSelectionIndex()==7?0:getSelectionIndex();}
    public boolean isEquipment(){return getCategoryIndex()==2;}
    public int getCategoryIndex(){return Math.max(0,Math.min(2,data.get(0)));}
    public int getSelectionIndex(){return Math.max(0,Math.min(getSelectionCount()-1,data.get(1)));}
    public int getQuantity(){return isEquipment()?1:Math.max(1,Math.min(64,data.get(2)));}
    public int getSelectionCount(){return switch(getCategoryIndex()){case 0->8;case 1->17;default->5;};}
    public ItemStack getSelectionStack(int index) {
        return switch(getCategoryIndex()) {
            case 0->index==7?new ItemStack(ModBlocks.ARCHITECTURAL_KITSUNEBI.get()):new ItemStack(ModItems.getItem(ModBlocks.COLORS[Math.max(0,Math.min(6,index))]).get());
            case 1->AltarCraftingEngine.getPhantomOutputStack(index,1);
            default->ItemStack.EMPTY;
        };
    }
    public AltarCraftingEngine.RecipeResult getCurrentResult(){
        return getCurrentResult(getQuantity());
    }
    /** Client preview may use the local drag value; crafting always uses synchronized state. */
    public AltarCraftingEngine.RecipeResult getCurrentResult(int quantity){
        if(isEquipment()) return new AltarCraftingEngine.RecipeResult(ItemStack.EMPTY,java.util.List.of(),false);
        return AltarCraftingEngine.calculate(inputContainer,getRecipeCategory(),getRecipeSelection(),quantity);
    }
    public boolean canCraft(){
        return canCraft(getQuantity());
    }
    public boolean canCraft(int quantity){
        var recipe=getCurrentResult(quantity);
        if(!recipe.canCraft()||recipe.result().isEmpty()) return false;
        ItemStack result=recipe.result(),stored=output.getItem(0);
        return stored.isEmpty()?result.getCount()<=result.getMaxStackSize():
                ItemStack.isSameItemSameComponents(stored,result)&&stored.getCount()+result.getCount()<=stored.getMaxStackSize();
    }

    @Override public boolean clickMenuButton(Player player,int id) {
        if(player.level().isClientSide||!stillValid(player)) return false;
        if(id>=0&&id<=2){data.set(0,id);data.set(1,0);if(isEquipment())data.set(2,1);}
        else if(id>=100&&id<100+getSelectionCount()) data.set(1,id-100);
        else if(id>=1001&&id<=1064&&!isEquipment()) data.set(2,id-1000);
        else if(id==CRAFT_BUTTON) return craft(player);
        else return false;
        broadcastChanges();
        return true;
    }

    private boolean craft(Player player) {
        if(crafting||!canCraft()) return false;
        ItemStack produced=getCurrentResult().result().copy();
        crafting=true;
        try {
            // All checks and writes occur together on the server thread. Output is real inventory,
            // never a free recipe preview; partial extraction cannot trigger another ingredient charge.
            AltarCraftingEngine.consumeIngredients(inputContainer,getRecipeCategory(),getRecipeSelection(),getQuantity());
            ItemStack stored=output.getItem(0);
            if(stored.isEmpty()) output.setItem(0,produced.copy());
            else {stored.grow(produced.getCount());output.setChanged();}
            produced.getItem().onCraftedBy(produced,player.level(),player);
            inputContainer.setChanged();
        } finally {crafting=false;}
        broadcastChanges();
        return true;
    }

    @Override public void slotsChanged(Container container){if(!crafting) broadcastChanges();}
    @Override public boolean stillValid(Player player){return stillValid(access,player,ModBlocks.KITSUNEBI_ALTAR.get());}
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0||index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if(!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),before=stack.copy();
        if(index<INV_SLOT_START) {
            if(!moveItemStackTo(stack,INV_SLOT_START,USE_ROW_SLOT_END,true)) return ItemStack.EMPTY;
        } else if(!moveItemStackTo(stack,0,INPUT_SLOT_COUNT,false)) return ItemStack.EMPTY;
        if(stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if(stack.getCount()==before.getCount()) return ItemStack.EMPTY;
        slot.onTake(player,stack);
        broadcastChanges();
        return before;
    }
    @Override public void removed(Player player) {
        super.removed(player);
        access.execute((level,pos)->{clearContainer(player,inputContainer);clearContainer(player,output);});
    }
}
