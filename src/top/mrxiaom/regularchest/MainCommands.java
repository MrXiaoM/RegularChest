package top.mrxiaom.regularchest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import top.mrxiaom.regularchest.ChestConfig.Chest;

public class MainCommands {
	
	RegularChest plugin;
	public MainCommands(RegularChest plugin) {
		this.plugin = plugin;
	}
	
	public void onCommand(CommandSender sender, String[] args) {
		boolean access = sender.isOp();
		if(access) {
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("send")) {
					if(args.length > 1) {
						String msg = "";
						for(int i = 1; i < args.length; i++) {
							msg += args[i] + " ";
						}
						for(Player p : Bukkit.getOnlinePlayers()) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
						}
						if(!(sender instanceof Player)) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
						}
						return;
					}
					else {
						sender.sendMessage("§7[§bRegularChest§7] §6请输入公告内容");
						return;
					}
				}
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("reload")) {
						plugin.reloadPluginConfig();
						sender.sendMessage("§7[§bRegularChest§7] §6配置文件已重载");
						return;
					}
					if(args[0].equalsIgnoreCase("list")) {
						if(plugin.getChestConfig().getChests().size() == 0) {
							sender.sendMessage("§7[§bRegularChest§7] §6暂无箱子");
							return;
						}
						sender.sendMessage("§7[§bRegularChest§7] §a箱子列表:");
						for(Chest chest : plugin.getChestConfig().getChests()){
							sender.sendMessage("§7[§bRegularChest§7] §6- §c" + chest.id + " §7(" + chest.chestWorld + ", " + chest.chestX + ", " + chest.chestY + ", " + chest.chestZ + ")");
						}
						return;
					}
				}
				if(args[0].equalsIgnoreCase("info")) {
					if(args.length == 2)
					for(Chest chest : plugin.getChestConfig().getChests()){
						if(RegularChest.debug) plugin.getLogger().info("当前箱子名称: “" + chest.id +"” 当前参数2: " + args[1]);
						if(chest.id.toLowerCase().equals(args[1].toLowerCase())) {
							String weeks = "";
							for(int i = 0 ; i < chest.weeks.size(); i++) {
								weeks += plugin.weekToString(chest.weeks.get(i)) + ((i == chest.weeks.size()-1)?"" : ", ");
							}
							
							sender.sendMessage("§7[§bRegularChest§7] §a箱子 " + chest.id + " §a的信息:\n" +
									"§7[§bRegularChest§7] §6所在世界: §c" + chest.chestWorld + "\n" +
									"§7[§bRegularChest§7] §6箱子坐标: §c" + chest.chestX + ", " + chest.chestY + ", " + chest.chestZ + "\n" +
									"§7[§bRegularChest§7] §6设定放置时间(星期): §c" + weeks + "\n" +
									"§7[§bRegularChest§7] §6设定放置时间(当天): §c" + chest.hour +":" + chest.minute + ":" + chest.second + "\n" +
									"§7[§bRegularChest§7] §6物品数量: §c" + chest.items.size() + "\n" +
									"§7[§bRegularChest§7] §6后台命令数量: §c" + chest.consoleCommands.size() + "\n");
							return;
						}
					}
					sender.sendMessage("§7[§bRegularChest§7] §6找不到箱子 " + args[1]);
					return;
				}
				if(args[0].equalsIgnoreCase("debug")) {
					if(args.length >= 2) {
						if(args[1].equalsIgnoreCase("genChest")) {
							if(args.length == 3) {
								for(Chest chest : plugin.getChestConfig().getChests()){
									if(chest.id.toLowerCase().equals(args[2].toLowerCase())) {
										chest.run(0);
										sender.sendMessage("§7[§bRegularChest§7] §6已强制执行放置箱子 " + chest.id);
										return;
									}
								}
								sender.sendMessage("§7[§bRegularChest§7] §6找不到你指定的箱子 " + args[2]);
								return;
							}
							sender.sendMessage("§7[§bRegularChest§7] §6/rc debug genChest [ID] §f- §c强制生成一个箱子(调试功能)");
							return;
						}
					}
				}
			}
			sender.sendMessage(
					"§7[§bRegularChest§7] §a作者: §b懒怠的小猫 §7| §a插件版本: §b" + plugin.getDescription().getVersion() + "\n" +
					"§7[§bRegularChest§7] §6/rc info §f- §c查看箱子详细\n" +
					"§7[§bRegularChest§7] §6/rc list §f- §c查看箱子列表\n" +
					"§7[§bRegularChest§7] §6/rc send [消息] §f- §c发送一条公告\n" +
					"§7[§bRegularChest§7] §6/rc reload §f- §c重新载入配置文件");
			return;
		}
	}
}
