package be.elmital.fixmcstats.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
    // Fix https://bugs.mojang.com/browse/MC-144005
    @Inject(method = "placeFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consumeAndReturn(ILnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    public void addCookerNBT(LivingEntity entity, ItemStack food, int cookTime, CallbackInfoReturnable<ItemStack> cir) {
        CustomData.update(DataComponents.CUSTOM_DATA, cir.getReturnValue(), nbt -> nbt.putUUID("cooker", entity.getUUID()));
    }

    // Fix https://bugs.mojang.com/browse/MC-144005
    @Inject(method = "cookTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"))
    private static void ensureHolder(Level world, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity, CallbackInfo ci, @Local(ordinal = 0) ItemStack itemStack, @Local(ordinal = 1) ItemStack itemStack2) {
        CustomData nbt = itemStack.getComponents().get(DataComponents.CUSTOM_DATA);
        UUID cooker = nbt != null ? nbt.copyTag().getUUID("cooker") : null;
        if (cooker != null) {
            Player playerEntity = world.getServer().getPlayerList().getPlayer(cooker);

            if (playerEntity != null) itemStack2.onCraftedBy(world, playerEntity, 1);
        }
    }
}
