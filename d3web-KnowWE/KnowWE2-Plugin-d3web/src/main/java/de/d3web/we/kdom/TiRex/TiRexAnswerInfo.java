package de.d3web.we.kdom.TiRex;

public class TiRexAnswerInfo {

		 String answerText;
		 String answerID;
		 String kbid;
		 String strategy;
		 String qid;
		 double rating;

		public TiRexAnswerInfo(String questionText, String answerID,
				String kbid, String strategy, double rating, String qid) {
			this.answerID = answerID;
			this.answerText = questionText;
			this.kbid = kbid;
			this.strategy = strategy;
			this.rating = rating;
			this.qid = qid;

		}

		public String getAnswerText() {
			return answerText;
		}

		public String getAnswerID() {
			return answerID;
		}

		public String getKbid() {
			return kbid;
		}

		public String getStrategy() {
			return strategy;
		}

		public String getQid() {
			return qid;
		}

		public double getRating() {
			return rating;
		}
}
