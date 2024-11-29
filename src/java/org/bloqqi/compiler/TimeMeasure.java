package org.bloqqi.compiler;
import java.util.Collection;
import java.util.ArrayList;

public class TimeMeasure {
	protected final static int runs = 10;
	protected final static int K = 10;

	protected TimeWork work;
	public TimeMeasure(TimeWork work) {
		this.work = work;
	}

	protected void measure() {
		work.printInfo();
		
		ArrayList<Long> times = new ArrayList<Long>();
		int i = 0;
		boolean run = true;
		while (run) {
			work.preWork();
			System.gc();
			
			long time = execute();
			times.add(time);
			System.out.print("Run " + i + ": " + time + " ms");
			if (times.size() >= K)  {
				Collection<Long> xs = times.subList(times.size()-K, times.size());
				double cov = cov(xs);
				double mean = mean(xs);
				System.out.print(", cov=" + cov);
				if (cov < 0.02) {
					run = false;
				} else if (mean < 20.0 && cov < 0.05) {
					run = false;
				} /*else if (times.size() > 40 && cov < 0.1) {
					run = false;
				}*/
			}
			System.out.println();
			i++;
		}
		Collection<Long> xs = times.subList(times.size()-K, times.size());
		System.out.println("Average: " + mean(xs) + " ms");

		work.check();
	}
	
	protected long execute() {
		long start = System.currentTimeMillis();
		work.doWork();
		long end = System.currentTimeMillis();
		long time = end-start;
		return time;
	}

	protected double mean(Collection<Long> xs) {
		long sum = 0;
		for (long x: xs) sum += x;
		return sum / (double) xs.size();
	}

	protected double sd(Collection<Long> xs) {
		int n = xs.size();
		double xmean = mean(xs);
		double s = 0;
		for (long x: xs) 
			s += Math.pow(x - xmean, 2);
		s = s / (n - 1);
		s = Math.sqrt(s);
		return s;
	}

	protected double cov(Collection<Long> xs) {
		return sd(xs) / mean(xs);
	}
}

class TimeJITMeasure extends TimeMeasure {
	final static int N_BEFORE = 10;
	final static int N = 20;
	
	public TimeJITMeasure(TimeWork work) {
		super(work);
	}
	
	@Override
	protected void measure() {
		work.printInfo();
		
		// Reach steady-state
		for (int i = 0; i < N_BEFORE; i++) {
			work.preWork();
			System.gc();
			
			long time = execute();
			System.out.println("Pre-run " + i + ": " + time + "ms");
		}
		
		// Run measurements
		ArrayList<Long> times = new ArrayList<Long>(N);
		for (int i = 0; i < N; i++) {
			work.preWork();
			System.gc();
			
			long time = execute();
			times.add(time);
			System.out.println("Run " + i + ": " + time + " ms");
		}

		System.out.println("Average: " + mean(times) + " ms");
		System.out.println("Cov: " + cov(times));
		
		work.check();
	}
}

interface TimeWork {
	void printInfo();
	void preWork();
	void doWork();
	void check();
}
