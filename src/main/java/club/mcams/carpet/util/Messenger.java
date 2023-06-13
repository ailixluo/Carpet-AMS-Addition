package club.mcams.carpet.util;

import club.mcams.carpet.mixin.translations.StyleAccessor;
import club.mcams.carpet.translations.AMSTranslations;
import club.mcams.carpet.translations.Translator;
import club.mcams.carpet.util.compat.DimensionWrapper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import com.google.common.collect.ImmutableMap;

import org.jetbrains.annotations.Nullable;

//#if MC>=11900
//$$ import java.util.function.Supplier;
//#endif


/**
 * Reference: Carpet TIS Addition
 */
public class Messenger {
    private static final Translator translator = new Translator("util");

    // Compound Text
    public static BaseText c(Object... fields) {
        //#if MC>=11900
        //$$ return (MutableText) carpet.utils.Messenger.c(fields);
        //#else
        return carpet.utils.Messenger.c(fields);
        //#endif
    }

    // Simple Text
    public static BaseText s(Object text) {
        //#if MC>=11900
        //$$ return Text.literal(text.toString());
        //#else
        return new LiteralText(text.toString());
        //#endif
    }

    // Simple Text with carpet style
    public static BaseText s(Object text, String carpetStyle) {
        return formatting(s(text), carpetStyle);
    }

    // Simple Text with formatting
    public static BaseText s(Object text, Formatting textFormatting) {
        return formatting(s(text), textFormatting);
    }

    // Fancy Text
    public static BaseText fancy(String carpetStyle, BaseText displayText, BaseText hoverText, ClickEvent clickEvent) {
        BaseText text = copy(displayText);
        if (carpetStyle != null) {
            text.setStyle(parseCarpetStyle(carpetStyle));
        }
        if (hoverText != null) {
            hover(text, hoverText);
        }
        if (clickEvent != null) {
            click(text, clickEvent);
        }
        return text;
    }

    public static BaseText fancy(BaseText displayText, BaseText hoverText, ClickEvent clickEvent) {
        return fancy(null, displayText, hoverText, clickEvent);
    }

    // Translation Text
    public static BaseText tr(String key, Object... args) {
        //#if MC>=11900
        //$$ return Text.translatable(key, args);
        //#else
        return new TranslatableText(key, args);
        //#endif
    }

    public static BaseText copy(BaseText text) {
        return (BaseText) text.shallowCopy();
    }

    public static void tell(ServerCommandSource source, BaseText text) {
        Entity entity = source.getEntity();
        text = entity instanceof ServerPlayerEntity ?
                AMSTranslations.translate(text, (ServerPlayerEntity) entity) :
                AMSTranslations.translate(text);
        //#if MC>=12000
        //$$ source.sendFeedback((Supplier<Text>) text, false);
        //#else
        source.sendFeedback(text, false);
        //#endif
    }

    public static BaseText formatting(BaseText text, Formatting... formattings) {
        text.formatted(formattings);
        return text;
    }

    public static BaseText formatting(BaseText text, String carpetStyle) {
        Style textStyle = text.getStyle();
        StyleAccessor parsedStyle = (StyleAccessor) parseCarpetStyle(carpetStyle);
        textStyle =  textStyle.withColor(parsedStyle.getColorField());
        textStyle = textStyle.withBold(parsedStyle.getBoldField());
        textStyle = textStyle.withItalic(parsedStyle.getItalicField());
        ((StyleAccessor) textStyle).setUnderlinedField(parsedStyle.getUnderlineField());
        ((StyleAccessor) textStyle).setStrikethroughField(parsedStyle.getStrikethroughField());
        ((StyleAccessor) textStyle).setObfuscatedField(parsedStyle.getObfuscatedField());
        return style(text, textStyle);
    }

    public static BaseText style(BaseText text, Style style) {
        text.setStyle(style);
        return text;
    }

    public static BaseText click(BaseText text, ClickEvent clickEvent) {
        style(text, text.getStyle().withClickEvent(clickEvent));
        return text;
    }

    public static BaseText hover(BaseText text, HoverEvent hoverEvent) {
        style(text, text.getStyle().withHoverEvent(hoverEvent));
        return text;
    }

    public static BaseText hover(BaseText text, BaseText hoverText) {
        return hover(text, new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
    }

    public static BaseText entity(String style, Entity entity) {
        BaseText entityBaseName = (BaseText) entity.getType().getName();
        BaseText entityDisplayName = (BaseText) entity.getName();
        BaseText hoverText = Messenger.c(
                translator.tr("entity_type", entityBaseName, s(EntityType.getId(entity.getType()).toString())), newLine(),
                getTeleportHint(entityDisplayName)
        );
        return fancy(style, entityDisplayName, hoverText, new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(entity)));
    }

    private static BaseText getTeleportHint(BaseText dest) {
        return translator.tr("teleport_hint", dest);
    }

    public static BaseText newLine() {
        return s("\n");
    }

    private static final ImmutableMap<DimensionWrapper, BaseText> DIMENSION_NAME = ImmutableMap.of(
            DimensionWrapper.OVERWORLD, tr("createWorld.customize.preset.overworld"),
            DimensionWrapper.THE_NETHER, tr("advancements.nether.root.title"),
            DimensionWrapper.THE_END, tr("advancements.end.root.title")
    );

    public static BaseText dimension(DimensionWrapper dim) {
        BaseText dimText = DIMENSION_NAME.get(dim);
        return dimText != null ? copy(dimText) : Messenger.s(dim.getIdentifierString());
    }

    private static BaseText __coord(String style, @Nullable DimensionWrapper dim, String posStr, String command) {
        BaseText hoverText = Messenger.s("");
        hoverText.append(getTeleportHint(Messenger.s(posStr)));
        if (dim != null) {
            hoverText.append("\n");
            hoverText.append(translator.tr("teleport_hint.dimension"));
            hoverText.append(": ");
            hoverText.append(dimension(dim));
        }
        return fancy(style, Messenger.s(posStr), hoverText, new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
    }

    public static BaseText coord(String style, Vec3d pos, DimensionWrapper dim) {
        return __coord(style, dim, TextUtil.coord(pos), TextUtil.tp(pos, dim));
    }

    public static BaseText coord(String style, Vec3i pos, DimensionWrapper dim) {
        return __coord(style, dim, TextUtil.coord(pos), TextUtil.tp(pos, dim));
    }

    public static BaseText coord(String style, ChunkPos pos, DimensionWrapper dim) {
        return __coord(style, dim, TextUtil.coord(pos), TextUtil.tp(pos, dim));
    }

    public static BaseText coord(String style, Vec3d pos) {
        return __coord(style, null, TextUtil.coord(pos), TextUtil.tp(pos));
    }

    public static BaseText coord(String style, Vec3i pos) {
        return __coord(style, null, TextUtil.coord(pos), TextUtil.tp(pos));
    }

    public static BaseText coord(String style, ChunkPos pos) {
        return __coord(style, null, TextUtil.coord(pos), TextUtil.tp(pos));
    }

    public static BaseText coord(Vec3d pos, DimensionWrapper dim) {
        return coord(null, pos, dim);
    }

    public static BaseText coord(Vec3i pos, DimensionWrapper dim) {
        return coord(null, pos, dim);
    }

    public static BaseText coord(ChunkPos pos, DimensionWrapper dim) {
        return coord(null, pos, dim);
    }

    public static BaseText coord(Vec3d pos) {
        return coord(null, pos);
    }

    public static BaseText coord(Vec3i pos) {
        return coord(null, pos);
    }

    public static BaseText coord(ChunkPos pos) {
        return coord(null, pos);
    }

    public static Style parseCarpetStyle(String style) {
        return carpet.utils.Messenger.parseStyle(style);
    }
}
