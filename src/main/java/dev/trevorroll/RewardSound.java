package dev.trevorroll;
public enum RewardSound {
    NONE("Off",""), DRAGON_LEVEL("Dragon + Level-up",""),
    DRAGON("Dragon roar","entity.ender_dragon.growl"), CHIME("Golden chime","block.note_block.chime"),
    LEVEL_UP("Level-up","entity.player.levelup"), WITHER("Wither fanfare","entity.wither.spawn"),
    FIRE("Fire crackle","block.fire.ambient"), WATER("Flowing water","block.water.ambient"),
    SPLASH("Water splash","entity.generic.splash"), DROPLETS("Water droplets","block.pointed_dripstone.drip_water"),
    LIGHTNING("Lightning strike","entity.lightning_bolt.impact"), THUNDER("Thunder","entity.lightning_bolt.thunder"),
    AMETHYST("Amethyst","block.amethyst_block.chime"), BELL("Bell","block.bell.use"),
    ENCHANT("Enchanting","block.enchantment_table.use"), PORTAL("Portal travel","block.portal.travel"),
    EXPERIENCE("XP pickup","entity.experience_orb.pickup");
    public final String label,id;
    RewardSound(String label,String id){this.label=label;this.id=id;}
    public int notes(){return this==NONE?0:this==CHIME?3:1;}
}
