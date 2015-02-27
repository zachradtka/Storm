package storm.bolts;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import storm.util.SlidingWindow;
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
	
	/** The size of the window in seconds */
	private static final int DEFAULT_WINDOW_SIZE = 60;
	
	/** The frequency to emit results, in seconds */
	private static final int DEFAULT_EMIT_FREQUENCY = 10;
	
	/** The local collector to control emitting of tuples */
	private OutputCollector collector;
	
	/** The frequency, in seconds, of when to emit a tuple */
	private final int emitFrequency;
	
	SlidingWindow<Object> elementCount;
	
	
	public SlidingWindowBolt() {
		this(DEFAULT_EMIT_FREQUENCY, DEFAULT_WINDOW_SIZE);
	}
	
	
	public SlidingWindowBolt(int emitFrequency) {
		this(emitFrequency, DEFAULT_WINDOW_SIZE);
	}
	
	public SlidingWindowBolt(int emitFrequency, int windowSize) {
		this.emitFrequency = emitFrequency;	
		elementCount = new SlidingWindow<Object>(windowSize/emitFrequency);
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		if (isTickTuple(input)) {
			
			LOG.debug("Tick encountered, emitting counts");
			
			Map<Object, Long> elements = elementCount.getCountAndAdvanceWindow();
			
			for (Entry<Object, Long> entry: elements.entrySet()) {
				Object obj = entry.getKey();
				Long count = entry.getValue();
				
				collector.emit(new Values(obj, count));
			}
		
		} else {
			Object obj = input.getValue(0);
			elementCount.incrementCount(obj);
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
