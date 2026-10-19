package net.kafgchan.kitsunebi.api;

import java.util.List;
import net.kafgchan.kitsunebi.block.altar.AltarCraftingEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Read-only GUI contract. No crafting, repair or packet handler is implemented here. */
public interface AltarEquipmentView {
    int SELECT_CRAFT=3000;
    int SELECT_REPAIR=3001;
    int EXECUTE_REPAIR=2001;

    /** Implementation must publish only synchronized server state, including output capacity. */
    State kitsunebiEquipmentView();

    record State(int tab, int selection, List<ItemStack> choices,
                 AltarCraftingEngine.RecipeResult preview, boolean ready, boolean canExecute,
                 Component status, int repairTargetSlot) {
        public State {
            choices=List.copyOf(choices);
            if(tab<0||tab>1) throw new IllegalArgumentException("equipment tab");
        }
    }
}
