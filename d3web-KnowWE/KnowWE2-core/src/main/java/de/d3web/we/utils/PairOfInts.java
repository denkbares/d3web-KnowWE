package de.d3web.we.utils;

public class PairOfInts implements Comparable<PairOfInts> {

		private int first;
		private int second;
		
		public PairOfInts(int first, int second) {
			super();
			this.first = first;
			this.second = second;
		}
		public int getFirst() {
			return first;
		}
		
		public Integer getFirstInteger() {
			return first;
		}
		public void setFirst(int first) {
			this.first = first;
		}
		public int getSecond() {
			return second;
		}
		public void setSecond(int second) {
			this.second = second;
		}
	
		@Override
		public String toString () {
			return "[" + first + "," + second + "]";
		}

		@Override
		public int compareTo(PairOfInts o) {
			return this.getFirstInteger().compareTo(o.getFirstInteger());
		}
		
}
