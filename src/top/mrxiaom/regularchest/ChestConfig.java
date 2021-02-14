package top.mrxiaom.regularchest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.minecraft.server.v1_12_R1.NBTTagCompound;

public class ChestConfig {
	private File file;
	private YamlConfiguration config;
	private RegularChest plugin;
	
    private	List<Chest> chests = new ArrayList<Chest>();
	
	public ChestConfig(RegularChest plugin) {
		this.plugin = plugin;
		file = new File(this.plugin.getDataFolder(), "chest.yml");
		this.reloadConfig();
	}
	
	/*
	 * 理论上这是无用的代码，因为本插件只读不写
	 * */
	public void saveConfig() {
		try {
			this.config.save(file);
		} catch(Throwable t){
			plugin.getLogger().warning("在保存配置文件 chest.yml 时出现了一个异常: ");
			plugin.getLogger().warning(t.getLocalizedMessage());
		}
	}
	
	public void reloadConfig() {
		plugin.getLogger().info("正在载入配置文件 chest.yml ...");
		int errors = 0;
		if (!file.exists()) plugin.saveResource("chest.yml", false);
		config = YamlConfiguration.loadConfiguration(file);
		this.chests = new ArrayList<ChestConfig.Chest>();
		this.chests.clear();
		for (String key : config.getKeys(false)) {
			if(RegularChest.debug) plugin.getLogger().info("[DEBUG] 发现箱子 " + key);
			ConfigurationSection session = config.getConfigurationSection(key);
			List<Integer> weeks = new ArrayList<Integer>();
			for(String week : session.getStringList("weeks")) {
				int weekInt = plugin.stringToWeek(week);
				if(weekInt == -1) {
					plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的星期 " + week + " 无法识别，已阻止该项载入，请检查你的配置文件");
					errors++;
					continue;
				}
				weeks.add(weekInt);
			}
			int hour = session.getInt("hour");
			int minute = session.getInt("minute");
			int second = session.getInt("second");
			
			String chestWorld = session.getString("chestWorld");
			if(Bukkit.getWorld(chestWorld) == null) {
				plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的世界 " + chestWorld + " 不存在，已阻止该项载入，请检查你的配置文件");
				errors++;
				continue;
			}
			int chestX = session.getInt("chestX");
			int chestY = session.getInt("chestY");
			int chestZ = session.getInt("chestZ");
			
			List<String> consoleCommands = session.getStringList("console-commands");
			Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
			for(String itemKey : session.getConfigurationSection("itemStacks").getKeys(false)) {
				if(RegularChest.debug) plugin.getLogger().info("[DEBUG] 发现物品 " + itemKey);
				ConfigurationSection chestSession = session.getConfigurationSection("itemStacks").getConfigurationSection(itemKey);
				String materialString = chestSession.getString("material");
				Material material = Material.getMaterial(materialString);
				if (material == null) {
					plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的物品 " + itemKey + " 中的物品材质 " + materialString + " 不存在，已阻止该物品载入，请检查你的配置文件");
					errors++;
					continue;
				}
				
				int slot = chestSession.getInt("slot");
				int amount = chestSession.getInt("amount");
				if (amount < 1 || amount > 64) {
					plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的物品 " + itemKey + " 中的物品数量过大或过小，已阻止该物品载入，请检查你的配置文件");
					errors++;
					continue;
				}
				ItemStack itemStack = new ItemStack(material, amount);
				ItemMeta meta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : CraftItemFactory.instance().getItemMeta(material);
				String displayName = ChatColor.translateAlternateColorCodes('&', chestSession.getString("displayName"));
				List<String> lore = chestSession.getStringList("lores");
				
				if(!displayName.toUpperCase().equals("NONE")) {
					meta.setDisplayName(displayName);
				}
				if(!lore.isEmpty()) {
					List<String> newLore = new ArrayList<String>();
					for(String stringLine : lore) {
						newLore.add(ChatColor.translateAlternateColorCodes('&', stringLine));
					}
					meta.setLore(newLore);
				}
				itemStack.setItemMeta(meta);
				List<String> enchList = chestSession.getStringList("enchs");
				for(String ench : enchList) {
					if(ench.contains(":")) {
						String enchString = ench.split(":")[0];
						String levelString = ench.split(":")[1];
						Enchantment enchantment = Enchantment.getByName(enchString.toUpperCase());
						if(enchantment == null) {
							plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的物品 " + itemKey + " 中的附魔 " + ench + " 的名称无效，已阻止该附魔载入，请检查你的配置文件");
							errors++;
							continue;
						}
						int level;
						try {
							level = Integer.valueOf(levelString);
						}catch (NumberFormatException  e) {
							plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的物品 " + itemKey + " 中的附魔 " + ench + " 的等级无效，已阻止该附魔载入，请检查你的配置文件");
							errors++;
							continue;
						}
						itemStack.addUnsafeEnchantment(enchantment, level);
					}
					else {
						plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的物品 " + itemKey + " 中的附魔 " + ench + " 的无效(没有冒号)，已阻止该附魔载入，请检查你的配置文件");
						errors++;
						continue;
					}
				}
				net.minecraft.server.v1_12_R1.ItemStack itemNMS = CraftItemStack.asNMSCopy(itemStack);
				NBTTagCompound nbtTag = itemNMS.hasTag() ? itemNMS.getTag() : new NBTTagCompound();
				List<String> nbtStringList = chestSession.getStringList("nbt-string");
				for(String nbtString : nbtStringList) {
					if(nbtString.contains(":")) {
						String nbtKey = nbtString.substring(0, nbtString.indexOf(":"));
						String nbtValue = nbtString.substring(nbtString.indexOf(":") + 1);
						nbtTag.setString(nbtKey, nbtValue);
					}
					else {
						plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的物品 " + itemKey + " 中的nbt-string值 " + nbtString + " 无效(没有冒号)，已阻止该nbt载入，请检查你的配置文件");
						errors++;
						continue;
					}
				}
				List<String> nbtIntList = chestSession.getStringList("nbt-int");
				for(String nbtString : nbtIntList) {
					if(nbtString.contains(":")) {
						String nbtKey = nbtString.substring(0, nbtString.indexOf(":"));
						String nbtValue = nbtString.substring(nbtString.indexOf(":") + 1);
						int nbtValueInt;
						try {
							nbtValueInt = Integer.valueOf(nbtValue);
						}catch (NumberFormatException  e) {
							plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的物品 " + itemKey + " 中的nbt-int存在 " + nbtString + " 的值不是整数型，已阻止该nbt载入，请检查你的配置文件");
							errors++;
							continue;
						}
						nbtTag.setInt(nbtKey, nbtValueInt);
					}
					else {
						plugin.getLogger().warning("载入配置文件时，箱子 " + key + " 设定的物品 " + itemKey + " 中的nbt-int值 " + nbtString + " 无效(没有冒号)，已阻止该nbt载入，请检查你的配置文件");
						errors++;
						continue;
					}
				}
				if(!nbtTag.isEmpty()) {
					itemNMS.setTag(nbtTag);
					itemStack = CraftItemStack.asBukkitCopy(itemNMS);
				}
				items.put(slot, itemStack);
			}
			
			chests.add(new Chest(key, weeks, hour, minute, second, items, chestX, chestY, chestZ, chestWorld, consoleCommands));
		}
		
		plugin.getLogger().info("配置文件 chest.yml 载入完成，已找到 " + chests.size() + " 个箱子" + (errors == 0 ? "" : "，§e已发现 " + errors + " 个可被插件捕捉到的错误"));
	}
	
	public List<Chest> getChests(){
		return this.chests;
	}
	
	public class Chest {
		
		String id;
		
		List<Integer> weeks;
		int hour;
		int minute;
		int second;
		
		Map<Integer, ItemStack> items;
		
		int chestX;
		int chestY;
		int chestZ;
		String chestWorld;
		
		List<String> consoleCommands;
		
		public Chest(String id, List<Integer> weeks, int hour, int minute, int second, Map<Integer, ItemStack> items, int chestX,int chestY,int chestZ,String chestWorld,
				List<String> consoleCommands) {
			this.id = id;
			this.weeks = weeks;
			this.hour = hour;
			this.minute = minute;
			this.second = second;
			this.items = items;
			this.chestX = chestX;
			this.chestY = chestY;
			this.chestZ = chestZ;
			this.chestWorld = chestWorld;
			this.consoleCommands = consoleCommands;
		}
		
		public void run(int week) {
			World world = Bukkit.getWorld(chestWorld);
			if(world == null) {
				plugin.getLogger().log(Level.WARNING, "在放置箱子时出现一个错误: 找不到世界 " + chestWorld, new NullPointerException());
				return;
			}
			if(RegularChest.debug) plugin.getLogger().info("[DEBUG] 正在生成箱子");
			Location location = new Location(world, chestX, chestY, chestZ);
			location.getBlock().setType(Material.CHEST);
			CraftBlock chest = (CraftBlock) location.getBlock();
			
			if(RegularChest.debug) plugin.getLogger().info("[DEBUG] 正在清空箱子内容");
			((org.bukkit.block.Chest)chest.getState()).getBlockInventory().clear();

			if(RegularChest.debug) plugin.getLogger().info("[DEBUG] 正在为箱子添加物品");
			for(Integer slot : items.keySet()) {
				((org.bukkit.block.Chest)chest.getState()).getBlockInventory().setItem(slot, items.get(slot));
			}
			if(RegularChest.debug) plugin.getLogger().info("[DEBUG] 正在执行后台命令");
			for(String cmd : consoleCommands) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', cmd)
						.replace("%week%", String.valueOf(week))
						.replace("%hour%", String.valueOf(hour))
						.replace("%minute%", String.valueOf(minute))
						.replace("%second%", String.valueOf(second))
						.replace("%items_amount%", String.valueOf(items.size()))
						.replace("%chestX%", String.valueOf(chestX))
						.replace("%chestY%", String.valueOf(chestY))
						.replace("%chestZ%", String.valueOf(chestZ))
						.replace("%chestWorld%", chestWorld));
			}
		}
		
		
		public boolean isInRunWeeks(int week) {
			return weeks.contains(week);
		}
		
		public List<Integer> getRunWeeks() {
			return weeks;
		}
		
		public int getRunHour() {
			return hour;
		}
		
		public int getRunMinute() {
			return minute;
		}
		
		public int getRunSecond() {
			return second;
		}
	}
}
