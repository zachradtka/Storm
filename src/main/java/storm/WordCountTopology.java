package storm;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import storm.bolts.PrinterBolt;
import storm.spouts.RandomSentenceSpout;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class WordCountTopology {

	public static final String SPOUT_NAME = "spout";
	public static final String BOLT_NAME_SPLIT_SENTENCE = "split";
	public static final String BOLT_NAME_WORD_COUNT = "count";
	public static final String BOLT_NAME_DISPLAY = "display";



	public static class SplitSentence extends BaseBasicBolt {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4940443045817317217L;

		private static final String OUTPUT_FIELD_NAME = "word";

		@Override
		public void execute(Tuple input, BasicOutputCollector collector) {

			StringTokenizer tokenizer = new StringTokenizer(input.getString(0));

			while (tokenizer.hasMoreTokens()) {
				collector.emit(new Values(tokenizer.nextToken()));
			}
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields(OUTPUT_FIELD_NAME));
		}

	}


	public static class WordCount extends BaseBasicBolt {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3445219534213811355L;


		private static final String OUTPUT_FIELD_NAME_0 = "word";
		private static final String OUTPUT_FIELD_NAME_1 = "count";


		private Map<String, Integer> wordCount = new HashMap<String, Integer>();


		@Override
		public void execute(Tuple input, BasicOutputCollector collector) {
			String word = input.getString(0);
			Integer currCount = null;

			// Initialize a count if it doesn't exist
			if ((currCount = wordCount.get(word)) == null) {
				currCount = 0;
			}

			// Update the word count
			currCount++;
			wordCount.put(word, currCount);

			// Output the new count
			collector.emit(new Values(word, currCount));
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields(OUTPUT_FIELD_NAME_0, OUTPUT_FIELD_NAME_1));
		}

	}


	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException,
			InterruptedException {

		TopologyBuilder builder = new TopologyBuilder();
		
		// Set the spout
		builder.setSpout(SPOUT_NAME, new RandomSentenceSpout(), 5);
		
		// The first bolt will split the lines based on spaces
		builder.setBolt(BOLT_NAME_SPLIT_SENTENCE, 
				new SplitSentence(), 
				8).shuffleGrouping(SPOUT_NAME);
		
		// This bolt will keep a running count of how many times a word has been seen
		builder.setBolt(BOLT_NAME_WORD_COUNT, 
				new WordCount(), 
				12).fieldsGrouping(BOLT_NAME_SPLIT_SENTENCE, 
						new Fields(WordCount.OUTPUT_FIELD_NAME_0));
		
		// Display the results of the previous bolt
		builder.setBolt(BOLT_NAME_DISPLAY, 
				new PrinterBolt(), 
				1).shuffleGrouping(BOLT_NAME_WORD_COUNT);


		Config conf = new Config();
		// Turn debug on/off to see output from all bolts
		// conf.setDebug(true);

		if (args != null && args.length > 0) {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
		} else {
			conf.setMaxTaskParallelism(3);
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("word-count", conf, builder.createTopology());
			Thread.sleep(10000);
			cluster.shutdown();
		}

	}

}
