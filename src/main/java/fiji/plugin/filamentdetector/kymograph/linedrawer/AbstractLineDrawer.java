package fiji.plugin.filamentdetector.kymograph.linedrawer;

public abstract class AbstractLineDrawer implements LineDrawer {

	protected String name;
	protected String description;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
