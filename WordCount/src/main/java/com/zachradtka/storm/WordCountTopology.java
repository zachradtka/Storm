package com.zachradtka.storm;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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

	public static class SplitSentence extends BaseBasicBolt {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4940443045817317217L;

		private static final String outputFieldName = "word";

		@Override
		public void execute(Tuple input, BasicOutputCollector collector) {

			StringTokenizer tokenizer = new StringTokenizer(input.getString(0));

			while (tokenizer.hasMoreTokens()) {
				collector.emit(new Values(tokenizer.nextToken()));
			}
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields(outputFieldName));
		}

	}


	public static class WordCount extends BaseBasicBolt {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3445219534213811355L;


		private static final String outputFieldName0 = "word";
		private static final String outputFieldName1 = "count";


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
			declarer.declare(new Fields(outputFieldName0, outputFieldName1));
		}

	}


	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException,
			InterruptedException {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("spout", new RandomSentenceSpout(), 5);
		builder.setBolt("split", new SplitSentence(), 8).shuffleGrouping("spout");
		builder.setBolt("count", new WordCount(), 12).fieldsGrouping("split", new Fields("word"));


		Config conf = new Config();
		conf.setDebug(true);

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
