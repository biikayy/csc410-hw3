package csc410.hw3;

import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ArraySparseSet;

class UpwardExposedUses
	extends BackwardFlowAnalysis<Unit, FlowSet<Local>> 
{
	
	private FlowSet<Local> emptySet;

	@SuppressWarnings("unchecked")
	public UpwardExposedUses(DirectedGraph g) {
		// First obligation
		super(g);
		
		// Create the emptyset
		emptySet = new ArraySparseSet<Local>();
		
		// Second obligation
		doAnalysis();

	}
	

	// This method performs the joining of successor nodes
	// Since live variables is a may analysis we join by union 
	@Override
	protected void merge(FlowSet<Local> inSet1, 
		FlowSet<Local> inSet2, 
		FlowSet<Local> outSet) 
	{
		inSet1.union(inSet2, outSet);
	}


	@Override
	protected void copy(FlowSet<Local> srcSet, 
		FlowSet<Local> destSet) 
	{
		srcSet.copy(destSet);
	}

	
	// Used to initialize the in and out sets for each node. In
	// our case we build up the sets as we go, so we initialize
	// with the empty set.
	@Override
	protected FlowSet<Local> newInitialFlow() {
		return emptySet.clone();
	}


	// Returns FlowSet representing the initial set of the entry
	// node. In our case the entry node is the last node and it
	// should contain the empty set.
	@Override
	protected FlowSet<Local> entryInitialFlow() {
		return emptySet.clone();
	}

	
	// Sets the outSet with the values that flow through the 
	// node from the inSet based on reads/writes at the node
	// Set the outSet (entry) based on the inSet (exit)
	@Override
	@SuppressWarnings("unchecked")
	protected void flowThrough(FlowSet<Local> inSet, 
		Unit node, FlowSet<Local> outSet) throws FileNotFoundException {
		
		// outSet is the set at enrty of the node
		// inSet is the set at exit of the node
		// out <- (in - write(node)) union read(node)
		
		// out <- (in - write(node))

		FlowSet writes = (FlowSet)emptySet.clone();

		for (ValueBox def: node.getUseAndDefBoxes()) {
			if (def.getValue() instanceof Local) {
				writes.add(def.getValue());
			}
		}
		inSet.difference(writes, outSet);

		// out <- out union read(node)

		File fout = new File ("exposed-uses.txt");
		FileOutputStream fos = new FileOutputStream(fout);
		
		BufferedWriter bw = new BufferedWriter (new OutputStreamWriter(fos));
		
		for (ValueBox use: node.getUseBoxes()) {
			if (use.getValue() instanceof Local) {
				outSet.add((Local) use.getValue());
				bw.write("hello");
			}
		}
	}
	
	private void outPut (Flowset<Local> outSet)
	
}


