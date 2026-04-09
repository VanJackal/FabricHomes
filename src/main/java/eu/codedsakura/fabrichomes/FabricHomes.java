package eu.codedsakura.fabrichomes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.codedsakura.fabrichomes.components.HomeComponent;
import eu.codedsakura.mods.ConfigUtils;
import eu.codedsakura.mods.TextUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static eu.codedsakura.fabrichomes.components.PlayerComponentInitializer.HOME_DATA;

public class FabricHomes implements ModInitializer {
    public static final Logger logger = LogManager.getLogger("FabricHomes");
    private static final String CONFIG_NAME = "FabricHomes.properties";

    private static final Style GoldStyle = Style.EMPTY.withColor(ChatFormatting.GOLD);
    private static final Style LightPurpleStyle = Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);

    private final HashMap<UUID, Long> recentRequests = new HashMap<>();
    private ConfigUtils config;

    @Override
    public void onInitialize() {
        logger.info("Initializing...");

        config = new ConfigUtils(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME).toFile(), logger, Arrays.asList(new ConfigUtils.IConfigValue[] {
                new ConfigUtils.IntegerConfigValue("stand-still", 3, new ConfigUtils.IntegerConfigValue.IntLimits(0),
                        new ConfigUtils.Command("Stand-Still time is %s seconds", "Stand-Still time set to %s seconds")),
                new ConfigUtils.IntegerConfigValue("cooldown", 30, new ConfigUtils.IntegerConfigValue.IntLimits(0),
                        new ConfigUtils.Command("Cooldown is %s seconds", "Cooldown set to %s seconds")),
                new ConfigUtils.BooleanConfigValue("bossbar", true,
                        new ConfigUtils.Command("Boss-Bar on: %s", "Boss-Bar is now: %s")),
                new ConfigUtils.IntegerConfigValue("max-homes", 2, new ConfigUtils.IntegerConfigValue.IntLimits(0),
                        new ConfigUtils.Command("Max available homes is %s", "Max homes set to %s"))
        }));

        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> {
            dispatcher.register(Commands.literal("home")
                    .executes(ctx -> homeInit(ctx, null))
                    .then(Commands.argument("name", StringArgumentType.greedyString()).suggests(this::getHomeSuggestions)
                            .executes(ctx -> homeInit(ctx, StringArgumentType.getString(ctx, "name")))));

            dispatcher.register(Commands.literal("sethome")
                    .executes(ctx -> homeSet(ctx, null))
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                            .executes(ctx -> homeSet(ctx, StringArgumentType.getString(ctx, "name")))));

            dispatcher.register(Commands.literal("delhome")
                            .then(Commands.argument("name", StringArgumentType.greedyString()).suggests(this::getHomeSuggestions)
                                    .executes(ctx -> homeDel(ctx, StringArgumentType.getString(ctx, "name")))));

            dispatcher.register(Commands.literal("homes")
                    .executes(this::homeList)
                    .then(Commands.literal("list")
                            .executes(this::homeList)
                            .then(Commands.argument("player", EntityArgument.player())
                                    .executes(ctx -> homeList(ctx, EntityArgument.getPlayer(ctx, "player")))))
                    .then(Commands.literal("gui").requires(req -> false)
                            .executes(ctx -> 0)) // TODO
                    .then(Commands.literal("delete")
                            .then(Commands.argument("name", StringArgumentType.greedyString()).suggests(this::getHomeSuggestions)
                                    .executes(ctx -> homeDel(ctx, StringArgumentType.getString(ctx, "name")))))
                    //.then(config.generateCommand("config",requirePermissionLevel(4)))
            );
        });
    }

    private boolean checkCooldown(ServerPlayer tFrom) {
        if (recentRequests.containsKey(tFrom.getUUID())) {
            long diff = Instant.now().getEpochSecond() - recentRequests.get(tFrom.getUUID());
            if (diff < (int) config.getValue("cooldown")) {
                tFrom.sendSystemMessage(Component.translatable("You cannot make teleport home for %s more seconds!", String.valueOf((int) config.getValue("cooldown") - diff)), false);
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Suggestions> getHomeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String start = builder.getRemaining().toLowerCase();
        HOME_DATA.get(context.getSource().getPlayer()).getHomes().stream()
                .map(HomeComponent::getName)
                .sorted(String::compareToIgnoreCase)
                .filter(v -> v.toLowerCase().startsWith(start))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    int homeInit(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (name == null) name = "main";

        String finalName = name;
        Optional<HomeComponent> home = HOME_DATA.get(player).getHomes()
                .stream().filter(v -> v.getName().equals(finalName)).findFirst();

        if (home.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("This home does not exist"));
            return 0;
        }

        if (checkCooldown(player)) return 1;

        Identifier dimId = home.get().getDimID();
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimId);
        ServerLevel level = ctx.getSource().getServer().getLevel(dimension);

        if(level != null) {
            HomeComponent homeComponent = home.get();
            TeleportTransition transition = new TeleportTransition(
                    level,
                    new Vec3(homeComponent.getX(), homeComponent.getY(), homeComponent.geyZ()),
                    Vec3.ZERO,
                    homeComponent.getYaw(),
                    homeComponent.getPitch(),
                    TeleportTransition.DO_NOTHING
            );
            player.teleport(transition);
            recentRequests.put(player.getUUID(), Instant.now().getEpochSecond());
        } else {
            ctx.getSource().sendFailure(Component.literal("Home entry invalid!"));
        }
        return 1;
    }

    int homeSet(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        if (name == null) name = "main";

        if (HOME_DATA.get(ctx.getSource().getPlayer()).getHomes().size() >= (int) config.getValue("max-homes")) {
            ctx.getSource().sendFailure(Component.literal("Home limit reached!"));
            return 1;
        }

        if (HOME_DATA.get(ctx.getSource().getPlayer()).addHome(new HomeComponent(
                ctx.getSource().getPosition(),
                ctx.getSource().getPlayer().getXRot(),
                ctx.getSource().getPlayer().getYRot(),
                ctx.getSource().getLevel().dimension().identifier(),
                name))) {

            String finalName = name;
            Optional<HomeComponent> home = HOME_DATA.get(ctx.getSource().getPlayer()).getHomes()
                    .stream().filter(v -> v.getName().equals(finalName)).findFirst();

            if (home.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Something went wrong adding the home!"));
                return 1;
            }

            Style hover = Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(home.get().toText(ctx.getSource().getServer())));

            ctx.getSource().sendFailure(Component.translatable("Home %s added successfully!",
                    Component.literal(finalName).setStyle(hover
                            .withColor(ChatFormatting.GOLD))).setStyle(LightPurpleStyle));
        } else {
            ctx.getSource().sendFailure(Component.literal("Couldn't add the home (probably already exists)!"));
            return 1;
        }
        return 1;
    }

    int homeDel(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        if (HOME_DATA.get(ctx.getSource().getPlayer()).removeHome(name)) {
            Optional<HomeComponent> home = HOME_DATA.get(ctx.getSource().getPlayer()).getHomes()
                    .stream().filter(v -> v.getName().equals(name)).findFirst();

            if (home.isPresent()) {
                ctx.getSource().sendFailure(Component.literal("Something went wrong removing the home!"));
                return 1;
            }

            ctx.getSource().sendSystemMessage(Component.translatable("Home %s deleted successfully!",
                    Component.literal(name).setStyle(GoldStyle).setStyle(LightPurpleStyle)));
        } else {
            ctx.getSource().sendFailure(Component.literal("Couldn't remove the home!"));
            return 1;
        }
        return 1;
    }


    int homeList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return homeList(ctx, ctx.getSource().getPlayer());
    }
    int homeList(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        List<HomeComponent> homes = HOME_DATA.get(player).getHomes();
        List<MutableComponent> list = new ArrayList<>();
        homes.stream().sorted((h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName())).forEach(h ->

                list.add(Component.literal(h.getName()).setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent.RunCommand("/home " + h.getName()))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.empty().append(Component.literal("Click to teleport.\n").withStyle(ChatFormatting.ITALIC))
                                                .append(h.toText(ctx.getSource().getServer()))))
                                .withColor(ChatFormatting.GOLD))));
        ctx.getSource().sendSystemMessage(Component.translatable("%s/%s:\n", homes.size(), config.getValue("max-homes")).append(TextUtils.join(list, Component.literal(", "))));
        return 1;
    }
}
