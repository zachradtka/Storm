package com.zachradtka.storm.bolts;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class PrinterBolt extends BaseBasicBolt {

	/**
	* 
	*/
	private static final long serialVersionUID = 3529002732935593370L;

	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		System.out.println(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {}


}
