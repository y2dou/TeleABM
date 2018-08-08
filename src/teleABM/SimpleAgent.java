package teleABM;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.util.ContextUtils;

@AgentAnnot(displayName = "Agent")
public abstract class SimpleAgent {

	private static final AtomicInteger idGenerator = new AtomicInteger(0);
	
	private int id;
	
	public SimpleAgent() {
		this.id = idGenerator.getAndIncrement();
	//	this(idGenerator.getAndIncrement());
	}
	
	public Integer getID() { return id; }
	
	public Color getColor() { return Color.DARK_GRAY; }

	public void die() {
		// Get the context in which the agent resides.
		Context<?> context = ContextUtils.getContext(this);

		// Remove the agent from the context if the context is not empty
		if (context.size() > 1)
			context.remove(this);
		// Otherwise if the context is empty, end the simulation
		else
			RunEnvironment.getInstance().endRun();
	}
	

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getID();
	//   result = prime*result+id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleAgent other = (SimpleAgent) obj;
		if (getID() != other.getID())
			return false;
		return true;
	}

}
