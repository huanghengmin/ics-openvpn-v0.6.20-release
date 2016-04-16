package com.zd.vpn.base;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.Activity;

public class ActivityStack implements Iterable<Activity> {
	private static ActivityStack instance = null;
	private LinkedList<Activity> activityStack;

	private ActivityStack() {
		activityStack = new LinkedList<Activity>();
	}

	public static synchronized ActivityStack getInstance() {
		if (instance == null)
			instance = new ActivityStack();
		return instance;
	}

	public LinkedList<Activity> getStack() {
		return activityStack;
	}

	public void push(Activity act) {
		synchronized (instance) {
			activityStack.addFirst(act);
		}
	}

	public Activity look() {
		if (activityStack.size() > 0)
			return activityStack.getFirst();
		throw null;
	}

	public int length() {
		return activityStack.size();
	}

	public void clear() {
		synchronized (instance) {
			activityStack.clear();
		}
	}

	public boolean remove(Activity activity) {
		synchronized (instance) {
			for (Iterator<Activity> ite = activityStack.iterator(); ite
					.hasNext();) {
				Activity act = ite.next();
				if (act.equals(activity)) {
					ite.remove();
					return true;
				}
			}
			return false;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("");
		for (Iterator<Activity> ite = activityStack.iterator(); ite.hasNext();) {
			sb.append(ite.next().getClass().getSimpleName()).append(" , ");
		}
		return sb.toString();
	}

	@Override
	public Iterator<Activity> iterator() {
		return activityStack.iterator();
	}
}