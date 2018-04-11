package de.chle.j7unzip;

import java.util.concurrent.ConcurrentHashMap.KeySetView;

public class ProgressTask implements Runnable {

	private KeySetView<String, Boolean> tasks;
	private final double totalCount;

	public ProgressTask(KeySetView<String,Boolean> tasks) {
		this.tasks = tasks;
		this.totalCount = tasks.size();
	}
	
	@Override
	public void run() {
		
		int lastPercent = -1;
		
		while(!tasks.isEmpty()) {
			
			int remaining = tasks.size();
			double doneCount = totalCount - remaining;
			int percent = (int) ((doneCount / totalCount) * 100.0);
			
			if(lastPercent != percent) {
				System.out.println(percent + " %");
				lastPercent = percent;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

}
