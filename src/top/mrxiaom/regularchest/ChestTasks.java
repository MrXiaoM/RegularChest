package top.mrxiaom.regularchest;

import java.util.Calendar;
import java.util.List;

import top.mrxiaom.regularchest.ChestConfig.Chest;

public class ChestTasks implements Runnable{

	private int lastSecond = 0;
	private boolean stop = false; 
	RegularChest plugin;
	public ChestTasks(RegularChest plugin) {
		this.plugin = plugin;
	}
	
	// 无意义
	public void stop() {
		this.stop = true;
	}
	
	@Override
	public void run() {
		if(this.stop) return;
		
		// 半秒执行一次是为了防止掉秒，就是某一秒突然就没有执行了，就怕这种情况
		// 所以这里判定一下今天时间的总秒数，如果与上次记录的秒数不等才执行，保证每秒只执行一次

		Calendar c = Calendar.getInstance();
		
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		// 1-Sunday
		// 2-Monday
		// 3-Tuesday
		// 4-Wednesday
		// 5-Thursday
		// 6-Friday
		// 7-Saturday
		int week = c.get(Calendar.DAY_OF_WEEK);
		
		int now = hour * 3600 + minute * 60 + second;
		if(now == lastSecond) return;
		lastSecond = now;
		
		List<Chest> chests = plugin.getChestConfig().getChests();
		for(Chest chest : chests) {
			if(chest.isInRunWeeks(week)) {
				if(hour == chest.getRunHour()) {
					if(minute == chest.getRunMinute()) {
						if(second == chest.getRunSecond()) {
							chest.run(week);
						}
					}
				}
			}
		}
	}
}
