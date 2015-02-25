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

/**
 * This topology has a single spout and three bolts. The purpose of this topology is to record the 
 * number of times a word has been seen or processed. The Spout emits random sentences or lines of 
 * text. The first bolt, take a line of text, splits on spaces, and emits tuples that consist of 
 * single words. The second bolt reads in words from the first bolt and keeps a running count of 
 * how many time each word was seen. When the total for a word has been computed, the second bolt
 * emits a tuple {word, number of occurrences}. The third and final bolt, prints the tuple, from 
 * the second bolt, to STDOUT.
 * 
 * @author Zachary Radtka
 *
 */
public class WordCountTopology {

	/** The name of the spout */
	public static final String SPOUT_NAME = "spout";
	
	/** The bolt that splits sentences */
	public static final String BOLT_NAME_SPLIT_SENTENCE = "split";
	
	/** The bolt that counts the words */
	public static final String BOLT_NAME_WORD_COUNT = "count";
	
	/** The bolt that outputs to STDOUT */
	public static final String BOLT_NAME_DISPLAY = "display";

	/**
	 * The <code>SplitSentence</code> class is a simple bolt that takes a line of text and splits
	 * the line into words on spaces. The resulting words are output as single element tuples.
	 * <p>
	 * Input:	{sentence}
	 * <p>
	 * Output:	{word}
	 * 
	 * @author Zachary Radtka
	 *
	 */
	public static class SplitSentence extends BaseBasicBolt {

		/** use serialVersionUID for interoperability */
		private static final long serialVersionUID = 4940443045817317217L;

		/** The name of the output field */
		private static final String OUTPUT_FIELD_NAME = "word";

		@Override
		public void execute(Tuple input, BasicOutputCollector collector) {

			// Split the incoming line on spaces
			StringTokenizer tokenizer = new StringTokenizer(input.getString(0));

			// Put each 
			while (tokenizer.hasMoreTokens()) {
				collector.emit(new Values(tokenizer.nextToken()));
			}
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields(OUTPUT_FIELD_NAME));
		}

	}

	/**
	 * The <code>WordCount</code> class is a simple bolt that takes a word and determines how many 
	 * times that word has appeared. Once the calculation is made a tuple consisting of the word
	 * and its respective count it output. 
	 * <p>
	 * Input:	{word}
	 * <p>
	 * Output:	{word, wordCount}
	 * 
	 * @author Zachary Radtka
	 *
	 */
	public static class WordCount extends BaseBasicBolt {

		/** use serialVersionUID for interoperability */
		private static final long serialVersionUID = 3445219534213811355L;

		/** The name of the first output field */
		private static final String OUTPUT_FIELD_NAME_0 = "word";
		
		/** The name of the second output field */
		private static final String OUTPUT_FIELD_NAME_1 = "count";

		/** (word, count) combinations */
		private Map<String, Integer> wordCount = new HashMap<String, Integer>();

		@Override
		public void execute(Tuple input, BasicOutputCollector collector) {
			
			// The word being processed
			String word = input.getString(0);
			
			// The number of times a word has been seen
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

	/**
	 * Submits (runs) the topology.
     *
     * Usage: "WordCountTopology [options]"
     *
     * By default, the topology is run locally under the name "word-count".
     *
     * Examples:
	 * 
     * <pre>
     *    <code>
     * # Runs in local mode (LocalCluster), with topology name "appendTopology"
     * $ storm jar storm-examples-jar-with-dependencies.jar storm.WordCountTopology
     *
     * # Runs in remote/cluster mode, with topology name "wordCount"
     * $ storm jar storm-examples-jar-with-dependencies.jar storm.WordCountTopology wordCount 
     *  </code>
     * </pre>
	 * @param args
	 * 			One positional argument is possible: TOPOLOGY_NAME
	 * @throws AlreadyAliveException
	 * @throws InvalidTopologyException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException,
			InterruptedException {

		// Create a new topology
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

		// Turn debug on/off to see output from all bolts
		Config conf = new Config();

		// If arguments are supplied, the topology will be run remotely
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
