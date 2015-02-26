package storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.TopologyBuilder;

public class TrendingTopology {

	/** The name of the topology */
	public static final String TOPOLOGY_NAME = "trendingTopology";
	
	/** The name of the spout */
	public static final String SPOUT_NAME = "spout";
	
	
	public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException {
		
		boolean runLocally = true;
		
		// Create a new topology
		TopologyBuilder builder = new TopologyBuilder();

		// Set the spout
		builder.setSpout(SPOUT_NAME, new TestWordSpout(), 5);


		// Turn debug on/off to see output from all bolts
		Config conf = new Config();
		conf.setDebug(true);

		// The topology can be run locally or remotely 
		if (runLocally) {
			conf.setMaxTaskParallelism(3);
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology());
			Thread.sleep(10000);
			cluster.shutdown();
		} else {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopologyWithProgressBar(TOPOLOGY_NAME, conf, builder.createTopology());
		}
	}

}
