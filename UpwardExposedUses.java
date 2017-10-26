package csc410.hw3;

/*** Class to hold Upward Exposed Uses analyis. Extend appropriate class. ***/

// Upward Exposed Uses is a backward flow analysis and will extend such class
public class UpwardExposedUses extends BackwardsFlowAnalysis {
	
	// Constructor for class
	public UpwardExposedUses(DirectedGraph g) {
			super(g);		
			doAnalysis();
	}

	protected FlowSet<Local> newInitialFlow() { return new ArraySparseSet<Local>(); }
	protected FlowSet<Local> entryInitialFlow() { return new ArraySparseSet<Local>(); }

	protected void merge(FlowSet<Local> in1, FlowSet<Local> in2, FlowSet<Local> out) {	
	in1.union(in2, out);
	}


	protected void flowThrough(FlowSet<Local> in, Unit node, FlowSet<Local> out) {
	
	// New set to keep track of all the writes
	FlowSet<Local> writes = new ArraySet<Local>();

	// 
	for (ValueBox def: node.getUseAndDefBoxes()) {
		if (def.getValue() instanceof Local) writes.add((Local) def.getValue());
	}

	out.difference(writes, in);

	for (ValueBox use: node.getUseBoxes()) {
		if (use.getValue() instanceof Local) out.add((Local) use.getValue());
	}
	}

	protected void copy(FlowSet<Local> srcSet, FlowSet<Local> destSet) {
	srcSet.copy(destSet);
	}


    public static void main(String[] args) {
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.dfa", new AnalysisTransformer()));
		soot.Main.main(sootArgs);
	}
}

