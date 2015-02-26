package storm.bolts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class SlidingWindowBolt extends BaseRichBolt {

	/** Use serialVersionUID for interoperability */
	private static final long serialVersionUID = 1574673259100368930L;
	
	private static final Logger LOG = Logger.getLogger(SlidingWindowBolt.class);

	/** The id of the first output field */
	private static final String OUTPUT_ID_0 = "word";

	/** The id of the second output field */
	private static final String OUTPUT_ID_1 = "count";
	
	/** The local collector to control emitting of tuples */
	private OutputCollector collector;
	
	/** The frequency, in seconds, of when to emit a tuple */
	private final int emitFrequency;
	
	
	Map<String, Long> wordCount;
	
	public SlidingWindowBolt(int emitFrequency) {
		this.emitFrequency = emitFrequency;
		wordCount = new HashMap<String, Long>();

	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {

		this.collector = collector;
		
	}

	@Override
	public void execute(Tuple input) {
		if (isTickTuple(input)) {
			
			Set<String> keys = wordCount.keySet();
						
			// Output the count and remove the entry to reset it's value
			for (String key : keys) {
				
				LOG.info("Emit " + key + ": " + wordCount.get(key));

				collector.emit(new Values(key, wordCount.get(key)));
			}
			
			wordCount.clear();
			
		} else {
			// add to a tuples count
			// The word being processed
			String word = input.getString(0);
						
			// The number of times a word has been seen
			Long currCount = null;

			// Initialize a count if it doesn't exist
			if ((currCount = wordCount.get(word)) == null) {
				currCount = 0L;
			}

			// Update the word count
			currCount++;
			
			LOG.info(word + ": " + currCount);
			wordCount.put(word, currCount);
			
			// Ack the tuple to it is never counted twice
			collector.ack(input);
		}
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields(OUTPUT_ID_0, OUTPUT_ID_1));
	}

	/**
	 * Determine if a tuple is a tick tuple. A tick tuple is a tuple sent from the __system 
	 * component and __tick stream
	 * 
	 * @param tuple
	 * 		The tuple in question
	 * @return
	 * 		<code>true</code> if the tuple is a tick tuple, <code>false</code> otherwise
	 */
	private static boolean isTickTuple(Tuple tuple) {
		  return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
		    && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
	}
	
	
	@Override
	public Map<String, Object> getComponentConfiguration() {
	  Config conf = new Config();
	  conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, this.emitFrequency);
	  return conf;
	}
}
