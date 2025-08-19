package com.chattriggers.ctjs.internal.engine.module

import com.chattriggers.ctjs.api.FileLib
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.StringVisitable
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper

class ModuleListScreen : Screen(Text.translatable("ctjs.ui.modules")) {
    private lateinit var moduleList: ModuleListWidget
    private lateinit var openFolderButton: ButtonWidget
    private lateinit var deleteButton: ButtonWidget
    private lateinit var closeButton: ButtonWidget

    override fun init() {
        moduleList = ModuleListWidget(client!!, width, height - 96, 32, 36)

        openFolderButton = ButtonWidget.builder(Text.translatable("ctjs.ui.openModulesFolder")) {
            FileLib.openModulesFolder()
        }.width(200).position(width / 2 - 100, height - 56).build()

        deleteButton = ButtonWidget.builder(Text.translatable("ctjs.ui.delete")) {
            moduleList.selectedOrNull?.let {
                client!!.setScreen(ConfirmScreen(
                    { confirmed ->
                        if (confirmed) {
                            ModuleManager.deleteModule(it.module.name)
                            close()
                        } else client!!.setScreen(this)
                    },
                    Text.translatable("ctjs.ui.deleteConfirmation", it.module.name),
                    Text.translatable("ctjs.ui.noRevert").withColor(ColorHelper.fromFloats(1f, 1f, 0f, 0f)),
                    ScreenTexts.PROCEED,
                    ScreenTexts.CANCEL
                ))
            }
        }.width(128).position(width / 2 + 4, height - 32).build()

        closeButton = ButtonWidget.builder(ScreenTexts.BACK) { close() }
            .width(128)
            .position(width / 2 - 132, height - 32).build()

        addDrawableChild(openFolderButton)
        addDrawableChild(deleteButton)
        addDrawableChild(closeButton)
        addDrawableChild(moduleList)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.render(context, mouseX, mouseY, deltaTicks)
        context?.drawCenteredTextWithShadow(
            textRenderer,
            this.title,
            this.width / 2,
            20, -1
        )
        deleteButton.active = moduleList.selectedOrNull != null
    }
}

class ModuleEntry(val textRenderer: TextRenderer, val module: Module) : AlwaysSelectedEntryListWidget.Entry<ModuleEntry>() {
    override fun getNarration(): Text {
        return Text.literal(module.name)
    }

    override fun render(
        context: DrawContext,
        index: Int,
        y: Int,
        x: Int,
        entryWidth: Int,
        entryHeight: Int,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        tickProgress: Float
    ) {
        val stack = context.matrices

        stack.pushMatrix()
        stack.scale(1.25f, 1.25f)
        context.drawTextWithShadow(textRenderer, module.name, (x / 1.25f + 4).toInt(), (y / 1.25f + 4).toInt(), -1)
        stack.popMatrix()

        module.metadata.creator?.let {
            context.drawTextWithShadow(
                textRenderer,
                "by $it",
                x + 2,
                y + entryHeight - textRenderer.fontHeight - 2,
                ColorHelper.fromFloats(1f, 0.8f, 0.8f, 0.8f)
            )
        }

        module.metadata.description?.let {
            context.drawGuiTexture(
                RenderPipelines.GUI_TEXTURED,
                INFO_TEXTURE,
                x + entryWidth - 22, y + 2,
                16, 16,
            )

            // If the info icon is hovered
            if (mouseX >= x + entryWidth - 22 && mouseX <= x + entryWidth - 6 && mouseY >= y + 2 && mouseY <= y + 18) {
                context.drawTooltip(
                    textRenderer.wrapLines(StringVisitable.plain(it), entryWidth),
                    mouseX, mouseY
                )
            }
        }

        module.metadata.version?.let {
            context.drawTextWithShadow(
                textRenderer,
                it,
                x + entryWidth - textRenderer.getWidth(it) - 4,
                y + entryHeight - textRenderer.fontHeight,
                ColorHelper.fromFloats(1f, 0.4f, 0.4f, 0.4f)
            )
        }
    }

    companion object {
        val INFO_TEXTURE: Identifier = Identifier.ofVanilla("icon/info")
    }
}

class ModuleListWidget(client: MinecraftClient, width: Int, height: Int, y: Int, itemHeight: Int)
    : AlwaysSelectedEntryListWidget<ModuleEntry>(client, width, height, y, itemHeight) {
    init {
        clearEntries()
        for (module in ModuleManager.cachedModules) {
            addEntry(ModuleEntry(client.textRenderer, module))
        }
    }

    // Default is 220
    override fun getRowWidth(): Int = 320
}
