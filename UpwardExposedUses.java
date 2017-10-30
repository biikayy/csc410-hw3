package csc410.hw3;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.graph.*;

class UpwardExposedUses
	extends BackwardFlowAnalysis<Unit, FlowSet<Local>> 
{
	// the empty set
	private FlowSet<Local> emptySet;
	private FlowSet<Local> old;

	// the original graph
	private UnitGraph originalGraph = null;

	//constructor
	public UpwardExposedUses(UnitGraph g) {
		// First obligation
		super(g);

		this.originalGraph = g;
		
		// Create the emptyset
		emptySet = new ArraySparseSet<Local>();
		old = new ArraySparseSet<Local>();
		
		// Second obligation
		doAnalysis();

		//Print Upwards Exposed Uses variables at entry and exit of nodes.
		try {outPut(g);}
		catch (IOException ioe) {}
	}
	
	private void outPut(UnitGraph g) throws IOException {
		// Create File
		File file = new File("exposed-uses.txt");
		file.createNewFile();
		FileWriter writer = new FileWriter(file);

		// Iterator for graph to insert all node in list
		Iterator<Unit> arrayIt = g.iterator();
		
		// List of all nodes in graph
		List<Unit> l = new ArrayList();
		arrayIt.next();	
		while (arrayIt.hasNext()) {
			l.add(arrayIt.next());
		}

		// Iterator for graph for flowAnalysis
		Iterator<Unit> unitIt = g.iterator();
		unitIt.next();

		while (unitIt.hasNext()) {
			Unit n = unitIt.next();
			
			// list of all paths from node s to any node in graph that goes from def to use
			List<List<Unit>>  allPaths = new ArrayList();
			for (Unit u: l) {
				List<Unit> path_s_u = new ArrayList();
				path_s_u = g.getExtendedBasicBlockPathBetween(n, u);
				if (!(path_s_u==null)) {
					allPaths.add(path_s_u);
				}
			}
			
			// All UEU at exit of node
			FlowSet<Local> ueu_set = getFlowAfter(n);

			// Writing to file
			writer.write(ueu_set.toString());
			for (Local ueu : ueu_set) {
				for (List<Unit> a_path: allPaths) {
					// node u
					writer.write(a_path.get(1).toString() + '\n');
					//node v
					writer.write(n.toString() + '\n');
					// ueu
					writer.write(ueu.toString() + '\n');	
				}
			}
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

	    // write	
		FlowSet<Local> writes = emptySet.clone();
		for (ValueBox def: node.getDefBoxes()) {
			if (def.getValue() instanceof Local) {
				writes.add((Local) def.getValue());
				old.add((Local) def.getValue());
			}
		}

		// read
		FlowSet<Local> reads = emptySet.clone();
		for (ValueBox use: node.getUseBoxes()) {
			if (use.getValue() instanceof Local) {
				reads.add((Local) use.getValue());
			}
		}

		// inSet = (in - write(node)) union read)

		// in - write(node)
		outSet.difference(writes, inSet);
		// union read)
		inSet.union(reads);

		// new writes in program
		FlowSet<Local> newvar = writes.clone();
		for (Local oldvar: this.old) {
			newvar.remove(oldvar);
		}
		outSet.union(newvar);
		}
}