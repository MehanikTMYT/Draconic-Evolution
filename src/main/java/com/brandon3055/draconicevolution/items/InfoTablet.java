package com.brandon3055.draconicevolution.items;

import com.brandon3055.brandonscore.integration.PIHelper;

import com.brandon3055.draconicevolution.DraconicEvolution;

import net.minecraft.ChatFormatting;


import net.minecraft.network.chat.ClickEvent;

import net.minecraft.network.chat.Component;

import net.minecraft.network.chat.HoverEvent;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.world.InteractionHand;

import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.Item;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;

import net.neoforged.api.distmarker.OnlyIn;



/**
 * Created by brandon3055 on 22/09/2016.
 */
public class InfoTablet extends Item {

    public InfoTablet(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand) { // port-26.1.2: InteractionResultHolder удалён
        if (worldIn.isClientSide()) {
            openPIGui();
        }
        return super.use(worldIn, playerIn, hand);
    }


    public static void openPIGui() {
        if (PIHelper.isInstalled()) {
            PIHelper.openMod(null, DraconicEvolution.MODID);
        } else {
            // port-26.1.2: клиентский код вынесен в DEClientHooks (dist-clean)
            com.brandon3055.draconicevolution.client.DEClientHooks.sendPIRequiredMessage();
        }
    }
}
