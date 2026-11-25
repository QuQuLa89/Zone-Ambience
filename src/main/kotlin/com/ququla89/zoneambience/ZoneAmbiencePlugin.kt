package com.ququla89.zoneambience

import com.ququla89.zoneambience.command.ZoneAmbienceCommand
import com.ququla89.zoneambience.engine.AreaVisualizer
import com.ququla89.zoneambience.engine.ParticleRenderEngine
import com.ququla89.zoneambience.gui.AreaGuiManager
import com.ququla89.zoneambience.integration.WorldGuardHook
import com.ququla89.zoneambience.item.ParticleItemFactory
import com.ququla89.zoneambience.listener.GuiListener
import com.ququla89.zoneambience.listener.ItemProtectionListener
import com.ququla89.zoneambience.listener.PlayerStateListener
import com.ququla89.zoneambience.listener.WandAndItemUseListener
import com.ququla89.zoneambience.selection.AreaLimitValidator
import com.ququla89.zoneambience.selection.AreaLimitsConfig
import com.ququla89.zoneambience.selection.SelectionManager
import com.ququla89.zoneambience.service.AreaService
import com.ququla89.zoneambience.storage.YamlAreaRepository
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class ZoneAmbiencePlugin : JavaPlugin() {
    private lateinit var worldGuardHook: WorldGuardHook
    private lateinit var areaService: AreaService
    private lateinit var renderEngine: ParticleRenderEngine
    private var renderTask: BukkitTask? = null
    private var visualizeTask: BukkitTask? = null

    override fun onLoad() {
        worldGuardHook = WorldGuardHook(this)
        worldGuardHook.registerFlag()
    }

    override fun onEnable() {
        saveDefaultConfig()

        val maxHistory = config.getInt("history.max-entries", 500).coerceAtLeast(1)
        val repository = YamlAreaRepository(dataFolder, logger)
        areaService = AreaService(repository, maxHistory)
        areaService.load()

        val limits = AreaLimitsConfig.fromConfig(config)
        val selectionManager = SelectionManager()
        val limitValidator = AreaLimitValidator(limits, areaService, worldGuardHook)
        val itemFactory = ParticleItemFactory(this)

        renderEngine = ParticleRenderEngine(areaService, worldGuardHook, config)
        val visualizer = AreaVisualizer(areaService, selectionManager, itemFactory, config)
        val guiManager = AreaGuiManager(areaService, itemFactory, selectionManager, limitValidator)

        val pm = server.pluginManager
        pm.registerEvents(ItemProtectionListener(itemFactory), this)
        pm.registerEvents(
            WandAndItemUseListener(
                itemFactory,
                selectionManager,
                areaService,
                limitValidator,
                visualizer,
                guiManager,
            ),
            this,
        )
        pm.registerEvents(PlayerStateListener(renderEngine, visualizer, selectionManager), this)
        pm.registerEvents(GuiListener(guiManager), this)

        val command =
            ZoneAmbienceCommand(
                areaService = areaService,
                selectionManager = selectionManager,
                limitValidator = limitValidator,
                itemFactory = itemFactory,
                renderEngine = renderEngine,
                visualizer = visualizer,
                guiManager = guiManager,
            )
        getCommand("zoneambience")?.let { cmd ->
            cmd.setExecutor(command)
            cmd.tabCompleter = command
        }

        renderTask =
            server.scheduler.runTaskTimer(
                this,
                Runnable { renderEngine.tick() },
                renderEngine.tickInterval,
                renderEngine.tickInterval,
            )
        visualizeTask =
            server.scheduler.runTaskTimer(
                this,
                Runnable { visualizer.tick() },
                visualizer.tickInterval,
                visualizer.tickInterval,
            )

        logger.info("ZoneAmbience を有効化しました (登録エリア数: ${areaService.allAreas().size})")
    }

    override fun onDisable() {
        renderTask?.cancel()
        visualizeTask?.cancel()
        if (::renderEngine.isInitialized) {
            renderEngine.resetAllOnlinePlayers()
        }
        if (::areaService.isInitialized) {
            areaService.saveAll()
        }
    }
}
