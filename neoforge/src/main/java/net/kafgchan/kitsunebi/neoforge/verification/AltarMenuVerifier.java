package net.kafgchan.kitsunebi.neoforge.verification;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.kafgchan.kitsunebi.menu.KitsunebiAltarMenu;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Real registered menu and vanilla click behavior, not a duplicate crafting algorithm. */
final class AltarMenuVerifier {
    static void verify(net.minecraft.server.level.ServerLevel level) {
        BlockPos pos=new BlockPos(-48,250,0);
        level.setBlock(pos,ModBlocks.KITSUNEBI_ALTAR.get().defaultBlockState(),3);
        Player player=new Player(level,pos,0,new GameProfile(UUID.randomUUID(),"AltarVerifier")) {
            @Override public boolean isSpectator(){return false;}
            @Override public boolean isCreative(){return false;}
        };
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        for(int q:new int[]{1,2,4,8,16,32,64}) {
            var menu=new KitsunebiAltarMenu(1,player.getInventory(),ContainerLevelAccess.create(level,pos));
            check(menu.clickMenuButton(player,1000+q),"quantity rejected");
            int bamboo=4*q,lapis=2*q,slot=0;
            while(bamboo>0){int n=Math.min(64,bamboo);menu.getSlot(slot++).set(new ItemStack(Items.BAMBOO,n));bamboo-=n;}
            while(lapis>0){int n=Math.min(64,lapis);menu.getSlot(slot++).set(new ItemStack(Items.LAPIS_LAZULI,n));lapis-=n;}
            menu.getSlot(slot++).set(new ItemStack(Items.GLOW_INK_SAC,q));
            menu.getSlot(slot++).set(new ItemStack(Items.AMETHYST_SHARD,q));
            menu.getSlot(slot).set(new ItemStack(Items.WHITE_DYE,q));
            check(menu.canCraft(),"materials do not fit: "+q);
            check(menu.clickMenuButton(player,KitsunebiAltarMenu.CRAFT_BUTTON),"craft rejected");
            check(menu.getSlot(KitsunebiAltarMenu.RESULT_SLOT).getItem().getCount()==q,"wrong output count");
            for(int i=0;i<KitsunebiAltarMenu.INPUT_SLOT_COUNT;i++) check(!menu.getSlot(i).hasItem(),"unconsumed ingredient");
            check(!menu.clickMenuButton(player,KitsunebiAltarMenu.CRAFT_BUTTON),"duplicate without materials");
            if(q==64) {
                menu.clicked(KitsunebiAltarMenu.RESULT_SLOT,1,ClickType.PICKUP,player);
                check(menu.getCarried().getCount()==32&&menu.getSlot(KitsunebiAltarMenu.RESULT_SLOT).getItem().getCount()==32,"right-click output loss");
                for(int i=0;i<36;i++) player.getInventory().setItem(i,new ItemStack(Items.STONE,64));
                player.getInventory().setItem(0,new ItemStack(ModBlocks.getBlock("white").get().asItem(),63));
                menu.quickMoveStack(player,KitsunebiAltarMenu.RESULT_SLOT);
                check(menu.getSlot(KitsunebiAltarMenu.RESULT_SLOT).getItem().getCount()==31,"partial shift-click lost paid output");
                player.getInventory().clearContent();
            }
        }
        var menu=new KitsunebiAltarMenu(2,player.getInventory(),ContainerLevelAccess.create(level,pos));
        // Simulate the actual data-packet path used on clients.
        menu.setData(0,1);menu.setData(1,6);menu.setData(2,4);
        check(menu.getCategoryIndex()==1&&menu.getSelectionIndex()==6&&menu.getQuantity()==4,"synced state not visible");
        menu.getSlot(0).set(new ItemStack(ModBlocks.PHANTOM_SLAB.get(),2));
        menu.getSlot(1).set(new ItemStack(ModBlocks.PHANTOM_VERTICAL_SLAB.get(),4));
        check(menu.canCraft(),"mixed halves rejected");
        check(menu.clickMenuButton(player,2000),"mid stair craft failed");
        check(menu.getSlot(KitsunebiAltarMenu.RESULT_SLOT).getItem().is(ModBlocks.PHANTOM_STAIRS_MID.get().asItem()),"wrong selected shape");
        // Even valid materials cannot be consumed if the output is occupied by another item.
        menu.getSlot(0).set(new ItemStack(ModBlocks.PHANTOM_SLAB.get(),6));
        menu.getSlot(KitsunebiAltarMenu.RESULT_SLOT).set(new ItemStack(Items.STONE,64));
        check(!menu.clickMenuButton(player,2000)&&menu.getSlot(0).getItem().getCount()==6,"full output consumed materials");
        check(!menu.clickMenuButton(player,1000)&&!menu.clickMenuButton(player,1065)&&!menu.clickMenuButton(player,199),"invalid payload accepted");
        check(menu.clickMenuButton(player,0)&&menu.getSelectionCount()==8,"fire category missing eighth item");
        check(menu.clickMenuButton(player,107)&&menu.getSelectionStack(7).is(ModBlocks.ARCHITECTURAL_KITSUNEBI.get().asItem()),"foxfire block not inside fire");
        check(menu.getCurrentResult().ingredients().get(0).requiredCount()==8*menu.getQuantity(),"foxfire block recipe mapping");
        menu.getSlot(14).set(new ItemStack(Items.DIAMOND));
        menu.quickMoveStack(player,14);
        check(!menu.getSlot(14).hasItem()&&player.getInventory().countItem(Items.DIAMOND)==1,"last input slot shift-click");
        check(menu.slots.size()==52&&menu.getSlot(14).x==48&&menu.getSlot(14).y==118,"3 by 5 input layout");
        check(menu.clickMenuButton(player,2)&&menu.getQuantity()==1,"equipment quantity must be one");
        check(!menu.clickMenuButton(player,1064)&&!menu.clickMenuButton(player,2000),"reserved equipment can craft or accept quantities");
        check(menu.getSelectionCount()==5&&menu.getSelectionStack(0).isEmpty(),"equipment placeholders");
        player.setPos(0,250,100);
        check(!menu.clickMenuButton(player,0),"distant menu access accepted");
        player.discard();
        System.out.println("PASS: altar presets, 15-slot layout and last-slot transfer, nested foxfire block, reserved equipment without quantity, atomic output and validation.");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
