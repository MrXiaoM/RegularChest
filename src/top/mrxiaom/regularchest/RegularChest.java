package top.mrxiaom.regularchest;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class RegularChest extends JavaPlugin{

	private ChestTasks tasks;
	private ChestConfig chestConfig;
	private MainCommands commands;
	
	public static boolean debug = false;
	private String sunday = "Sunday";
	private String monday = "Monday";
	private String tuesday = "Tuesday";
	private String wednesday = "Wednesday";
	private String thursday = "Thursday";
	private String friday = "Friday";
	private String saturday = "Saturaday";
	
	@Override
	public void onEnable() {
		// 载入主配置文件
		this.loadConfig();
		// 初始化箱子配置文件
		this.chestConfig = new ChestConfig(this);
		// 初始化命令
		this.commands = new MainCommands(this);
		// 设置中国地区，定时器需要用来判断星期几，好有个标准
		Locale.setDefault(Locale.CHINA);
		// 初始化并运行计划任务 (10tick执行一次，即半秒执行一次)
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this.tasks = new ChestTasks(this), 10, 10);
		
		this.getLogger().info("小猫出品，必属精品");
		this.getLogger().info("小猫的b站主页: https://space.bilibili.com/330771760");
		this.getLogger().info("插件已启用");
	}
	
	/*
	 * 星期文本转数字
	 * -1为无法转换
	 * */
	public int stringToWeek(String week) {
		switch(week.toLowerCase()) {
			case "sunday": return 1;
			case "sun": return 1;
			case "monday": return 2;
			case "mon": return 2;
			case "tuesday": return 3;
			case "tue": return 3;
			case "wednesday": return 4;
			case "wed": return 4;
			case "thursday": return 5;
			case "thu": return 5;
			case "friday": return 6;
			case "fri": return 6;
			case "saturday": return 7;
			case "sat": return 7;
			default: return -1;
		}
	}

	/*
	 * 星期数字转String
	 * (1是周末)
	 * */
	public String weekToString(int week) {
		switch(week) {
			case 0: return "§c[DEBUG]§r";
		    case 1: return this.sunday;
		    case 2: return this.monday;
		    case 3: return this.tuesday;
		    case 4: return this.wednesday;
		    case 5: return this.thursday;
		    case 6: return this.friday;
		    case 7: return this.saturday;
		    default: return "";
		}
	}
	public void reloadPluginConfig() {
		this.reloadConfig();
		this.chestConfig.reloadConfig();
		this.loadConfig();
	}
	
	public void loadConfig() {
		this.saveDefaultConfig();
		FileConfiguration config = this.getConfig();
		debug = config.getBoolean("debug");

		this.sunday = config.getString("string-sunday");
		this.monday = config.getString("string-monday");
		this.tuesday = config.getString("string-tuesday");
		this.wednesday = config.getString("string-wednesday");
		this.thursday = config.getString("string-thursday");
		this.friday = config.getString("string-friday");
		this.saturday = config.getString("string-saturday");
	}
	
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		tasks.stop();
		this.getLogger().info("插件已卸载");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		commands.onCommand(sender, args);
		return true;
	}
	
	public ChestConfig getChestConfig() {
		return this.chestConfig;
	}
}
