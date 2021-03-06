package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.common.SneakEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;

public class SkillSneak extends ActiveSkill {

    private boolean damageCancels;
    private boolean attackCancels;

    public SkillSneak(Heroes plugin) {
        super(plugin, "Sneak");
        setDescription("You crouch into the shadows.");
        setUsage("/skill stealth");
        setArgumentRange(0, 0);
        setIdentifiers("skill sneak");
        setTypes(SkillType.BUFF, SkillType.PHYSICAL, SkillType.STEALTHY);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEventListener(), plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.DURATION.node(), 600000); // 10 minutes in milliseconds
        node.set("damage-cancels", true);
        node.set("attacking-cancels", true);
        node.set("refresh-interval", 5000); // in milliseconds
        return node;
    }

    @Override
    public void init() {
        super.init();
        damageCancels = SkillConfigManager.getRaw(this, "damage-cancels", true);
        attackCancels = SkillConfigManager.getRaw(this, "attacking-cancels", true);
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        if (hero.hasEffect("Sneak")) {
            hero.removeEffect(hero.getEffect("Sneak"));
        } else {
            Messaging.send(hero.getPlayer(), "You are now sneaking");

            int duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 600000, false);
            int period = SkillConfigManager.getUseSetting(hero, this, "refresh-interval", 5000, true);
            hero.addEffect(new SneakEffect(this, period, duration));
        }
        return SkillResult.NORMAL;
    }

    public class SkillEventListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !damageCancels || event.getDamage() == 0) {
                return;
            }
            Player player = null;
            if (event.getEntity() instanceof Player) {
                player = (Player) event.getEntity();
            } else if (attackCancels && event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                if (subEvent.getDamager() instanceof Player) {
                    player = (Player) subEvent.getDamager();
                } else if (subEvent.getDamager() instanceof Projectile) {
                    if (((Projectile) subEvent.getDamager()).getShooter() instanceof Player) {
                        player = (Player) ((Projectile) subEvent.getDamager()).getShooter();
                    }
                }
            }
            if (player == null) {
                return;
            }

            Hero hero = plugin.getHeroManager().getHero(player);
            if (hero.hasEffect("Sneak")) {
                player.setSneaking(false);
                hero.removeEffect(hero.getEffect("Sneak"));
            }
        }
        
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
            Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
            if (hero.hasEffect("Sneak")) {
                event.getPlayer().setSneaking(true);
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String getDescription(Hero hero) {
        return getDescription();
    }
}
