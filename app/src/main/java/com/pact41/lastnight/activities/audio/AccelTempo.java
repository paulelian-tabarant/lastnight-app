package com.pact41.lastnight.activities.audio;

import java.util.ArrayList;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class AccelTempo {
	
	static int fe;
	static ArrayList<Double> greatestPeaks;
	static ArrayList<Integer> widths;
	public static double score = 0;

	public static int tempo(ArrayList<Double> sig, ArrayList<Double> T){
		ArrayList<Double> s = interpolate(sig, T);
		//fe = 100;
		ArrayList<Double> combed = combBank(s);
		ArrayList<Double> baseline = monotoneminimum(combed);
		ArrayList<Double> corrected = new ArrayList<Double>();
		int maximum = 0;
		double sofar = 0;
		for(int i=0; i < combed.size(); i++){
			corrected.add(combed.get(i) - baseline.get(i));
			if (combed.get(i) - baseline.get(i) >= sofar){
				sofar = combed.get(i) - baseline.get(i);

				maximum = i;
			}
		}
		//System.out.println(corrected);
		greatestPeaks = new ArrayList<Double>();
		widths = new ArrayList<Integer>();
		System.out.println(calculateScore(corrected));
		score = Math.max(score, calculateScore(corrected) );
		int beat = maximum;
		return beat + 30;
	}
	public static void insert(double height, int width){
		if ( height > 0.3)
		{
			greatestPeaks.add(height);
			widths.add(width);
		}
	}

	public static ArrayList<Double> monotoneminimum(ArrayList<Double> s){
		ArrayList<Double> d = new ArrayList<Double>();
		ArrayList<Double> base = new ArrayList<Double>();
		int n = 169;
		for(int i=0; i <n; i++)
			d.add(s.get(i+1)-s.get(i));
		d.add(d.get(d.size()-1));
		int a = 0;

		while (a < n){
			if (d.get(a) <= 0){
				int b = a+1;
				while (b < n && d.get(b) < 0){
					base.add(s.get(b-1));
					b += 1;
				}
				a = b;
				base.add(s.get(a));
			}
			else{
				int b = a+1;
				while (b < n && s.get(b) > s.get(a)){
					//mx = Math.max(mx, s.get(b));
					//if(Math.abs(s.get(b)- s.get(b-1)) <= 0.00001)
					//	plateau += 1;
					base.add(s.get(a));
					b+=1;
				}
				//insert(mx, b-a - plateau);
				a = b;
				base.add(s.get(a));
			}
		}
		base.add(base.get(base.size()-1));
		return base;
	}

	public static ArrayList<Double> combFilter(ArrayList<Double> x, int delay){
		double alpha =  Math.pow(0.5, delay*1.75/fe);
		int n = x.size();
		ArrayList<Double> y = new ArrayList<Double>();
		for(int i = 0; i<delay+n; i++)
			y.add((double) 0);
		for(int i = 0; i<n; i++)
			y.set(i+delay, alpha*y.get(i) + (1-alpha)*x.get(i));
		ArrayList<Double> z = new ArrayList<Double>();
		for(int i = 0; i<n; i++)
			z.add(y.get(i+delay));
		return z;
	}
	public static ArrayList<Double> combBank(ArrayList<Double>s){
		ArrayList<Double> combs = new ArrayList<Double>();
	    for(int bpm=30; bpm<200; bpm++){
	        double f = bpm/60.0; 
	        int delay = (int) (Math.ceil(fe/f));
	        combs.add(energy(combFilter(s, delay)));
	    }
	    return combs;
	}
	public static double energy(ArrayList<Double> s){
		double result = 0;
		for(Double x : s)
			result += x*x;
		return result;
	}

	public static ArrayList<Double> interpolate(ArrayList<Double> data, ArrayList<Double> T){
		double pas = trouver_le_pas(T);
		double tmax=T.get(T.size()-1);
		double tmin=T.get(0);
		int time_interval= (int) (tmax-tmin);
		int n= (int) ((int) time_interval/pas);
		ArrayList<Double> I = new ArrayList<Double>();
		I.add(data.get(0));
		int i=1;
		int j=1;
		while(i<n){
			if (T.get(j)>=(i*pas+tmin)){
				while (T.get(j)>=(i*pas+tmin)) {

					I.add((i*pas+tmin-T.get(j-1))*(data.get(j)-data.get(j-1))/(T.get(j)-T.get(j-1))+data.get(j-1));
					i=i+1;
				}
			}
			j=j+1;
		}
		fe =  (int) ((1000.0/pas));
		return I;
	}
	private static double trouver_le_pas(ArrayList<Double> T) {
		int maxocc = 0;
		double step = 0;
		Hashtable<Double, Integer> freq= new Hashtable<Double, Integer>();
		for (int i = 1; i<T.size() ; i++){
			double pas = (T.get(i)-T.get(i-1));
			//System.out.println(pas);
			if (freq.containsKey(pas)){
				int nocc = freq.get(pas)+1;
				freq.put(pas, nocc);
				if(nocc > maxocc){
					maxocc = nocc;
					step = pas;
				}
			}
			else
				freq.put(pas, 1);
		}

		return step;
	}


	public static double calculateScore(ArrayList<Double> s){
		double sc = 0;

		//normalisation
		double norm = 0;
		for(double value : s){
			norm = Math.max(norm, value);
		}

		//peak spotting
		int i = 0;
		while(i < s.size() ){
			int start = i;
			double msofar = 0;
			while( i < s.size() && s.get(i) > 0){
				msofar = Math.max(msofar, s.get(i));
				i += 1;
			}
			if (i > start){
				insert(msofar*1.0/norm, i - start);
			}
			i += 1;
		}

		//System.out.println(greatestPeaks);
		//System.out.println(widths);
		//score calculation
		double numerator = 0;
		double denom = 0;
		for(int k=0; k < greatestPeaks.size(); k++){
			numerator += greatestPeaks.get(k) * widths.get(k);
			denom += greatestPeaks.get(k);
		}

		//score normalisation
		sc = numerator/denom * 4.0/7;

		return sc;
	}
}
