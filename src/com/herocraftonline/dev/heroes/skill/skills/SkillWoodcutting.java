package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.block.Block;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.HBlockListener;
import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import com.herocraftonline.dev.heroes.util.Util;

public class SkillWoodcutting extends PassiveSkill {

    public SkillWoodcutting(Heroes plugin) {
        super(plugin, "Woodcutting");
        setDescription("You know about the things of the earth!");
        setEffectTypes(EffectType.BENEFICIAL);
        setTypes(SkillType.KNOWLEDGE, SkillType.EARTH, SkillType.BUFF);
        
        registerEvent(Type.BLOCK_BREAK, new SkillBlockListener(), Priority.Monitor);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.CHANCE_LEVEL.node(), .001);
        return node;
    }

    public class SkillBlockListener extends BlockListener {

        @Override
        public void onBlockBreak(BlockBreakEvent event) {
            if (event.isCancelled())
                return;

            Block block = event.getBlock();
            if (HBlockListener.placedBlocks.containsKey(block.getLocation()))
                return;

            int extraDrops = 0;
            switch (block.getType()) {
                case LOG:
                    break;
                default:
                    return;
            }

            Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
            if (!hero.hasEffect("Woodcutting") || Util.rand.nextDouble() > getSetting(hero.getHeroClass(), "chance-per-level", .001) * hero.getLevel())
                return;

            if (extraDrops != 0) {
                extraDrops = Util.rand.nextInt(extraDrops) + 1;
            } else {
                extraDrops = 1;
            }

            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), extraDrops, (short) 0, block.getData()));
        }
    }
}
