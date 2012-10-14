package logisticspipes.ticks;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class QueuedTasks implements ITickHandler {
	
	private static LinkedList<Callable> queue = new LinkedList<Callable>();
	
	public static void queueTask(Callable task) {
		synchronized (queue) {
			queue.add(task);
		}
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		synchronized (queue) {
			for(Callable call:queue) {
				try {
					call.call();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			queue.clear();
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "LogisticsPipes QueuedTask";
	}

}
