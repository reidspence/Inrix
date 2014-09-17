import java.io.IOException;


public class RunFilterInrixData {
	
	public static void main(String[] args) throws IOException{
		//File to be analyzed
		String filePath = "/Users/Stephen/Documents/Java/DataManagement/bin/SFPoints.csv";
		
		//Test Objects
		//1MB = 1,048,576 Bytes
		System.out.println("System Available Memory: " + InrixReader.getAvailableMemory()/1048576 + "MB");
		
		InrixReader.externalSort(filePath,5);
	}

}