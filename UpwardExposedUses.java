package csc410.hw3;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.graph.*;

class UpwardExposedUses
	extends BackwardFlowAnalysis<Unit, FlowSet<Local>> 
{
	private FlowSet<Local> emptySet;

	public UpwardExposedUses(UnitGraph g) {
		// First obligation
		super(g);
		
		// Create the emptyset
		emptySet = new ArraySparseSet<Local>();

		// Second obligation
		doAnalysis();

		//Print Upwards Exposed Uses variables at entry and exit of nodes.
		try {outPut(g);}
		catch (IOException ioe) {}

	}
	
	private void outPut(UnitGraph g) throws IOException {
		Iterator<Unit> unitIt = g.iterator();
		
				File file = new File("exposes-uses.txt");
				file.createNewFile();
				FileWriter writer = new FileWriter(file);
		
					while (unitIt.hasNext()) {
						
						Unit s = unitIt.next();
				
						writer.write(s.toString());
			
						
						writer.write('\n');
						
							
						FlowSet<Local> set = getFlowBefore(s);
				
						writer.write("\t[entry: ");
						for (Local local: set) {
							writer.write(local+" ");
							writer.write('\n');
						}
				
						set = getFlowAfter(s);
							
						writer.write("]\t[exit: ");
							for (Local local: set) {
								writer.write(local+" ");
							}
						writer.write("]");
						writer.write('\n');
					}
				writer.close();
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
	protected void flowThrough(FlowSet<Local> inSet, 
		Unit node, FlowSet<Local> outSet) {

		// outSet is the set at entry of the node
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

		for (ValueBox use: node.getUseBoxes()) {
			if (use.getValue() instanceof Local) {
				outSet.add((Local) use.getValue());				
			}
		}
	}
}

