
public class InrixNode {
	public int type;
	public float time;
	public float lat;
	public float lon;
	public int source;
	public float ID;
	int acData;
	public InrixNode next;
	public InrixNode previous;
	public InrixNode(String[] data) {
		this.type = Integer.parseInt(data[0]);
		this.time = Float.parseFloat(data[1]);
		this.lat = Float.parseFloat(data[2]);
		this.lon = Float.parseFloat(data[3]);
		this.source = Integer.parseInt(data[4]);
		this.ID = Float.parseFloat(data[5]);
		this.acData = Integer.parseInt(data[6]);
	}
	public String toString() {
		return type+","+time+","+lat+","+lon+","+source+","+ID+","+acData;
	}
}
