package arthessia.featurebox.objects;

public enum PowerType {
    ENDER_TELEPORT,
    HEAL,
    FIREBALL,
    LIGHTNING_STRIKE,
    ARROW_LIGHTNING,
    ARROW_EXPLOSIVE,
    ARROW_HEAL;

    public static PowerType fromString(String s) {
        try {
            return PowerType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
