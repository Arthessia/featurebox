package arthessia.featurebox.stones;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import arthessia.featurebox.Plugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TeleportStone implements Listener {

    private final Plugin plugin;
    private static final int SIZE = 54;
    private static final int ITEMS_PER_PAGE = 45;

    private final Map<String, Integer> openMenus = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getType() != Material.LIGHTNING_ROD) {
            return;
        }

        String stoneKey = getStoneKeyAt(block.getLocation());
        if (stoneKey == null) {
            return;
        }

        plugin.getConfig().set("teleport.stones." + stoneKey, null);
        plugin.saveConfig();

        player.sendMessage("§cLe point de téléportation §d"
                + stoneKey.replace("_", " ") + " §ca été détruit !");
    }

    @EventHandler
    public void onBlockPlaceControl(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        Player player = event.getPlayer();

        for (BlockFace face : new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST }) {
            Block relative = placed.getRelative(face);

            if (relative.getType() == Material.LIGHTNING_ROD) {
                Location loc = relative.getLocation();
                String worldName = loc.getWorld().getName();
                int x = loc.getBlockX();
                int y = loc.getBlockY();
                int z = loc.getBlockZ();

                for (String key : plugin.getConfig().getConfigurationSection("teleport.stones").getKeys(false)) {
                    String path = "teleport.stones." + key;
                    if (plugin.getConfig().getString(path + ".world").equals(worldName)
                            && plugin.getConfig().getInt(path + ".x") == x
                            && plugin.getConfig().getInt(path + ".y") == y
                            && plugin.getConfig().getInt(path + ".z") == z) {

                        event.setCancelled(true);
                        player.sendMessage("§cImpossible de placer un bloc à côté d’un téléporteur !");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();

        if (block.getType() != Material.LIGHTNING_ROD) {
            return;
        }

        Block below1 = block.getRelative(0, -1, 0);
        Block below2 = block.getRelative(0, -2, 0);

        if (below1.getType() == Material.AIR || below2.getType() == Material.AIR) {
            return;
        }
        if (below1.getType() != below2.getType()) {
            return;
        }
        if (!areSurroundedByAir(below1) || !areSurroundedByAir(below2)) {
            return;
        }

        Location newLoc = block.getLocation();
        if (isNearAnotherTeleport(newLoc, plugin.getConfig().getInt("teleport.distance", 200))) {
            player.sendMessage("§cImpossible de créer un point de téléportation : un autre est trop proche !");
            block.getWorld().strikeLightning(block.getLocation());
            block.getWorld().createExplosion(block.getLocation(), 4f, true, true);
            event.setCancelled(true);
            return;
        }

        Biome biome = block.getWorld().getBiome(block.getLocation());

        String biomeName = biome.name().replace('_', ' ');

        int x = block.getX() >> 4;
        int z = block.getZ() >> 4;

        String stoneName = "§d" + player.getName() + " @ " + biomeName + " [" + x + ", " + z + "]";

        String stoneKey = biomeName.replace(' ', '_') + "_"
                + System.currentTimeMillis();

        plugin.getConfig().set("teleport.stones." + stoneKey + ".name", stoneName);
        plugin.getConfig().set("teleport.stones." + stoneKey + ".material", below1.getType().name());
        plugin.getConfig().set("teleport.stones." + stoneKey + ".world", block.getWorld().getName());
        plugin.getConfig().set("teleport.stones." + stoneKey + ".x", block.getX());
        plugin.getConfig().set("teleport.stones." + stoneKey + ".y", block.getY());
        plugin.getConfig().set("teleport.stones." + stoneKey + ".z", block.getZ());

        plugin.saveConfig();
        player.sendMessage("§aUn nouveau point de téléportation a été créé : " + stoneName);
    }

    private boolean areSurroundedByAir(Block block) {
        return block.getRelative(1, 0, 0).getType() == Material.AIR
                && block.getRelative(-1, 0, 0).getType() == Material.AIR
                && block.getRelative(0, 0, 1).getType() == Material.AIR
                && block.getRelative(0, 0, -1).getType() == Material.AIR;
    }

    private boolean isNearAnotherTeleport(Location newLoc, int radius) {
        if (!plugin.getConfig().isConfigurationSection("teleport.stones")) {
            return false;
        }

        int radiusSquared = radius * radius;

        for (String stoneKey : plugin.getConfig().getConfigurationSection("teleport.stones").getKeys(false)) {
            String worldName = plugin.getConfig().getString("teleport.stones." + stoneKey + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world == null)
                continue;

            double x = plugin.getConfig().getDouble("teleport.stones." + stoneKey + ".x");
            double y = plugin.getConfig().getDouble("teleport.stones." + stoneKey + ".y");
            double z = plugin.getConfig().getDouble("teleport.stones." + stoneKey + ".z");

            Location existing = new Location(world, x, y, z);

            if (existing.getWorld().equals(newLoc.getWorld())
                    && existing.distanceSquared(newLoc) <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void useStone(PlayerInteractEvent event) {
        if (plugin.getConfig().getBoolean("teleport.enabled", true) == false)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getClickedBlock() == null)
            return;

        Player player = event.getPlayer();
        String key = getStoneKeyAt(event.getClickedBlock().getLocation());
        if (key == null)
            return;

        openMenu(player, 0);
    }

    private void openMenu(Player player, int page) {
        List<String> stones = plugin.getConfig().getConfigurationSection("teleport.stones").getKeys(false).stream()
                .toList();

        int maxPage = (int) Math.ceil((double) stones.size() / ITEMS_PER_PAGE);
        if (page < 0)
            page = 0;
        if (page >= maxPage)
            page = maxPage - 1;

        Inventory inv = Bukkit.createInventory(null, SIZE, "§5Voyage rapide (Page " + (page + 1) + ")");

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, stones.size());

        for (int i = start; i < end; i++) {
            String stoneKey = stones.get(i);
            String name = plugin.getConfig().getString("teleport.stones." + stoneKey + ".name", stoneKey);
            String materialName = plugin.getConfig().getString("teleport.stones." + stoneKey + ".material",
                    "ENDER_PEARL");
            Material mat;
            try {
                mat = Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                mat = Material.ENDER_PEARL;
            }

            ItemStack icon = new ItemStack(mat);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(name);
            icon.setItemMeta(meta);

            inv.addItem(icon);
        }

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            meta.setDisplayName("§e◀ Page précédente");
            prev.setItemMeta(meta);
            inv.setItem(45, prev);
        }

        if (page < maxPage - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName("§e▶ Page suivante");
            next.setItemMeta(meta);
            inv.setItem(53, next);
        }

        openMenus.put(player.getName(), page);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (plugin.getConfig().getBoolean("teleport.enabled", true) == false)
            return;
        if (!event.getView().getTitle().startsWith("§5Voyage rapide"))
            return;
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        if (!openMenus.containsKey(player.getName()))
            return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta())
            return;

        String name = clicked.getItemMeta().getDisplayName();
        String teleportSound = plugin.getConfig().getString("teleport.sound.teleport", "block.end_portal.spawn");

        if (name.contains("◀")) {
            int currentPage = openMenus.get(player.getName());
            openMenu(player, currentPage - 1);
            return;
        }
        if (name.contains("▶")) {
            int currentPage = openMenus.get(player.getName());
            openMenu(player, currentPage + 1);
            return;
        }

        for (String stoneKey : plugin.getConfig().getConfigurationSection("teleport.stones").getKeys(false)) {
            String stoneName = plugin.getConfig().getString("teleport.stones." + stoneKey + ".name", stoneKey);
            if (!stoneName.equalsIgnoreCase(name)) {
                continue;
            }
            String worldName = plugin.getConfig().getString("teleport.stones." + stoneKey + ".world");
            teleportSound = plugin.getConfig().getString(
                    "teleport.stones." + stoneKey + ".sound.teleport",
                    teleportSound);
            World world = Bukkit.getWorld(worldName);
            double x = plugin.getConfig().getDouble("teleport.stones." + stoneKey + ".x");
            double y = plugin.getConfig().getDouble("teleport.stones." + stoneKey + ".y");
            double z = plugin.getConfig().getDouble("teleport.stones." + stoneKey + ".z");
            boolean lightningStrike = plugin.getConfig().getBoolean("teleport.stones." + stoneKey + ".lightning",
                    plugin.getConfig().getBoolean("teleport.lightning", false));

            if (world != null) {
                player.closeInventory();
                if (lightningStrike)
                    player.getWorld().strikeLightningEffect(player.getLocation());
                player.teleport(new Location(world, x + 0.5, y + 1, z + 0.5));
                if (lightningStrike)
                    player.getWorld().strikeLightningEffect(player.getLocation());
                NamespacedKey key = NamespacedKey.minecraft(teleportSound);
                Sound sound = Registry.SOUNDS.get(key);
                player.playSound(player.getLocation(), sound, 1f, 1f);
                player.sendMessage("§dTéléporté vers " + stoneName + " !");
            }
            break;
        }
    }

    private String getStoneKeyAt(Location loc) {
        for (String stoneKey : plugin.getConfig().getConfigurationSection("teleport.stones").getKeys(false)) {
            String worldName = plugin.getConfig().getString("teleport.stones." + stoneKey + ".world");
            World world = Bukkit.getWorld(worldName);
            int x = plugin.getConfig().getInt("teleport.stones." + stoneKey + ".x");
            int y = plugin.getConfig().getInt("teleport.stones." + stoneKey + ".y");
            int z = plugin.getConfig().getInt("teleport.stones." + stoneKey + ".z");

            if (world == null)
                continue;
            if (!world.equals(loc.getWorld()))
                continue;

            if (loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z) {
                return stoneKey;
            }
        }
        return null;
    }
}
