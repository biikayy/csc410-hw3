package csc410.hw3;

import java.util.*;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ArraySparseSet;


/*** Class to hold Upward Exposed Uses analyis. Extend appropriate class. ***/

// Upward Exposed Uses is a backward flow analysis and will extend such class
public class UpwardExposedUses extends BackwardFlowAnalysis<Unit, FlowSet<Local>>  {
	
	private FlowSet<Local> emptySet;

	// Constructor for class
	public UpwardExposedUses(DirectedGraph g) {
			super(g);	
			emptySet = new ArraySparseSet<Local>();	
			doAnalysis(); }

	protected FlowSet<Local> newInitialFlow() { return new ArraySparseSet<Local>(); }
	protected FlowSet<Local> entryInitialFlow() { return new ArraySparseSet<Local>(); }

	@Override
	protected void merge(FlowSet<Local> in1, FlowSet<Local> in2, FlowSet<Local> out) {	
	in1.union(in2, out);
	}

	@Override
	protected void flowThrough(FlowSet<Local> in, Unit node, FlowSet<Local> out) {
	
	// New set to keep track of all the writes
	FlowSet<Local> writes = new ArraySparseSet<Local>();

	// 
	for (ValueBox def: node.getUseAndDefBoxes()) {
		if (def.getValue() instanceof Local) writes.add((Local) def.getValue());
	}

	out.difference(writes, in);

	for (ValueBox use: node.getUseBoxes()) {
		if (use.getValue() instanceof Local) out.add((Local) use.getValue());
	}
	}

	@Override
	protected void copy(FlowSet<Local> srcSet, FlowSet<Local> destSet) {
	srcSet.copy(destSet);
	}


}

