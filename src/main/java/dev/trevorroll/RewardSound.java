package dev.trevorroll;
public enum RewardSound {
    DRAGON_LEVEL("Dragon + Level-up"), DRAGON("Dragon roar"), CHIME("Golden chime"), LEVEL_UP("Level-up"), WITHER("Wither fanfare");
    public final String label;
    RewardSound(String label){this.label=label;}
    public int notes(){return this==CHIME?3:1;}
}
