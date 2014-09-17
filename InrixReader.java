import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class InrixReader {
	
	// readers and writers needed for the files
	private static FileReader _input; 
	private static BufferedReader _bufRead;
	private static FileReader _input2; 
	private static BufferedReader _bufRead2;
	private static FileWriter _output; 
	private static BufferedWriter _bufWrite;
	
	public ArrayList<InrixTripLL> readCSV(String fileName) throws FileNotFoundException{
		ArrayList<InrixTripLL> Trips = new ArrayList<InrixTripLL>();
		Scanner scanner = new Scanner(new File(fileName));
	    String line = scanner.nextLine();
	    String nextline;
	    InrixTripLL curTrip = new InrixTripLL();
	    while(scanner.hasNext()){
	    	nextline = scanner.nextLine();
	    	String[] row = line.split(",");
	    	String[] nextrow = nextline.split(",");
            if (row==null) 
            {
                row = null;
                break;
            }
	    	curTrip.add(row);
	    	if (!row[5].equals(nextrow[5])) {
	    		Trips.add(curTrip.deepCopy());
	    		curTrip.clear();
	    	}
	    	line = nextline;
	    }
	    scanner.close();
	    return Trips;
	}
	
	public ArrayList<InrixTripLL> testCSV(String fileName) throws IOException{
		ArrayList<InrixTripLL> Trips = new ArrayList<InrixTripLL>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		Scanner scanner = null;
 
        while ((line = reader.readLine()) != null) {
            scanner = new Scanner(line);
            scanner.useDelimiter(",");
            while (scanner.hasNext()) {
                String data = scanner.next();
                System.out.print(Float.parseFloat(data) + ",");
            }
            System.out.print("\n");
        }
	    scanner.close();
	    reader.close();
	    return Trips;
	}
	public static double estimateBucketSize(String fileName){
		//Get our constants for analysis
		long sysMemory = getAvailableMemory();
		double bucketsize = getFileSize(fileName)/1024;
		//To not create too many or too few buckets, we need a little trial and error.
		//We do not want to create more than 1024 temporary files on disk. Too Damn Many!
		//If the bucket size is less than half of the available system memory, cool. 
		//Let's grow it to our maximum (3/4 of the systems memory)
		//If there are way too many buckets, we might need to reallocate resources.
		if (bucketsize < sysMemory*.75)
			bucketsize = sysMemory*.75;
		else{
			if(bucketsize >= sysMemory){
				System.out.println("We will probably run out of memory.");
			}
		}
		return bucketsize;
	}
    private static long getFileSize(String filename) {
    	  //Returns file size of filename. If the file does not exist, returns a error.
	      File file = new File(filename);
	      if (!file.exists() || !file.isFile()) {
	         System.out.println("File doesn't exist");
	         return -1;
	      }
	      return file.length();
	}
    
    public static long getAvailableMemory() {
    	  Runtime runtime = Runtime.getRuntime();
    	  long totalMemory = runtime.totalMemory(); // current heap allocated to the VM process
    	  long freeMemory = runtime.freeMemory(); // out of the current heap, how much is free
    	  long maxMemory = runtime.maxMemory(); // Max heap VM can use e.g. Xmx setting
    	  long usedMemory = totalMemory - freeMemory; // how much of the current heap the VM is using
    	  long availableMemory = maxMemory - usedMemory; // available memory i.e. Maximum heap size minus the current amount used
    	  return availableMemory;
    	}
	
    static void externalSort(String fileName, int columnNumber)
	{
		try
		{
			FileReader fr = new FileReader(fileName); 
			BufferedReader br = new BufferedReader(fr);
			String [] header = br.readLine().split(",");
			String [] row = header;
			ArrayList<String[]> splitRows = new ArrayList<String[]>();
						
			int numFiles = 0;
			while (row!=null)
			{
				for (int i=0; i<1000000000; i++){
					if (splitRows.size()<=estimateBucketSize(fileName)){
					String line = br.readLine();
					if (line==null) 
					{
						row = null;
						break;
					}
					row = line.split(",");
					splitRows.add(row);
				}
					else{
						break;
					}
				}
//				for(int i=0; i<10000; i++)
//				{
//					String line = br.readLine();
//					if (line==null) 
//					{
//						row = null;
//						break;
//					}
//					row = line.split(",");
//					splitRows.add(row);
//				}
				// sort the rows
				splitRows = mergeSort(splitRows, columnNumber);
				
				// write to disk
				FileWriter fw = new FileWriter(fileName + "_chunk" + numFiles + ".csv");
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(flattenArray(header,",")+"\n");
				for(int i=0; i<splitRows.size(); i++)
				{
					bw.append(flattenArray(splitRows.get(i),",")+"\n");
				}
				bw.close();
				numFiles++;
				splitRows.clear();
			}
			
			mergeFiles(fileName, numFiles, columnNumber);
			
			
			br.close();
			fr.close();
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		
		
	}
	
	private static void mergeFiles(String fileName, int numFiles, int compareIndex)
	{
		try
		{
			ArrayList<FileReader> mergefr = new ArrayList<FileReader>();
			ArrayList<BufferedReader> mergefbr = new ArrayList<BufferedReader>();
			ArrayList<String[]> filerows = new ArrayList<String[]>(); 
			FileWriter fw = new FileWriter(fileName + "_sorted.csv");
			BufferedWriter bw = new BufferedWriter(fw);
			String [] header;
			
			boolean someFileStillHasRows = false;
			
			for (int i=0; i<numFiles; i++)
			{
				mergefr.add(new FileReader(fileName+"_chunk"+i+".csv"));
				mergefbr.add(new BufferedReader(mergefr.get(i)));
				// get each one past the header
				header = mergefbr.get(i).readLine().split(",");
								
				if (i==0) bw.write(flattenArray(header,",")+"\n");
				
				// get the first row
				String line = mergefbr.get(i).readLine();
				if (line != null)
				{
					filerows.add(line.split(","));
					someFileStillHasRows = true;
				}
				else 
				{
					filerows.add(null);
				}
					
			}
			
			
			String[] row;
			while (someFileStillHasRows)
			{
				Integer min;
				int minIndex;
				row = filerows.get(0);
				if (row!=null) {
					min = Integer.parseInt(row[compareIndex]);
					minIndex = 0;
				}
				else {
					min = null;
					minIndex = -1;
				}
				
				// check which one is min
				for(int i=1; i<filerows.size(); i++)
				{
					row = filerows.get(i);
					if (min!=null) {
						
						if(row!=null && Integer.parseInt(row[compareIndex]) < min)
						{
							minIndex = i;
							min = Integer.parseInt(filerows.get(i)[compareIndex]);
						}
					}
					else
					{
						if(row!=null)
						{
							min = Integer.parseInt(row[compareIndex]);
							minIndex = i;
						}
					}
				}
				
				if (minIndex < 0) {
					someFileStillHasRows=false;
				}
				else
				{
					// write to the sorted file
					bw.append(flattenArray(filerows.get(minIndex),",")+"\n");
					
					// get another row from the file that had the min
					String line = mergefbr.get(minIndex).readLine();
					if (line != null)
					{
						filerows.set(minIndex,line.split(","));
					}
					else 
					{
						filerows.set(minIndex,null);
					}
				}								
				// check if one still has rows
				for(int i=0; i<filerows.size(); i++)
				{
					
					someFileStillHasRows = false;
					if(filerows.get(i)!=null) 
					{
						if (minIndex < 0) 
						{
							System.out.println("mindex lt 0 and found row not null" + flattenArray(filerows.get(i)," "));
							System.exit(-1);
						}
						someFileStillHasRows = true;
						break;
					}
				}
				
				// check the actual files one more time
				if (!someFileStillHasRows)
				{
					
					//write the last one not covered above
					for(int i=0; i<filerows.size(); i++)
					{
						if (filerows.get(i) == null)
						{
							String line = mergefbr.get(i).readLine();
							if (line!=null) 
							{
								
								someFileStillHasRows=true;
								filerows.set(i,line.split(","));
							}
						}
								
					}
				}
					
			}
			
			
			
			// close all the files
			bw.close();
			fw.close();
			for(int i=0; i<mergefbr.size(); i++)
				mergefbr.get(i).close();
			for(int i=0; i<mergefr.size(); i++)
				mergefr.get(i).close();
			
			
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	// sort an arrayList of arrays based on the ith column
	private static ArrayList<String[]> mergeSort(ArrayList<String[]> arr, int index)
	{
		ArrayList<String[]> left = new ArrayList<String[]>();
		ArrayList<String[]> right = new ArrayList<String[]>();
		if(arr.size()<=1)
			return arr;
		else
		{
			int middle = arr.size()/2;
			for (int i = 0; i<middle; i++)
				left.add(arr.get(i));
			for (int j = middle; j<arr.size(); j++)
				right.add(arr.get(j));
			left = mergeSort(left, index);
			right = mergeSort(right, index);
			return merge(left, right, index);
			
		}
		
	}
	
	// merge the the results for mergeSort back together 
	private static ArrayList<String[]> merge(ArrayList<String[]> left, ArrayList<String[]> right, int index)
	{
		ArrayList<String[]> result = new ArrayList<String[]>();
		while (left.size() > 0 && right.size() > 0)
		{
			if(Integer.parseInt(left.get(0)[index]) <= Integer.parseInt(right.get(0)[index]))
			{
				result.add(left.get(0));
				left.remove(0);
			}
			else
			{
				result.add(right.get(0));
				right.remove(0);
			}
		}
		if (left.size()>0) 
		{
			for(int i=0; i<left.size(); i++)
				result.add(left.get(i));
		}
		if (right.size()>0) 
		{
			for(int i=0; i<right.size(); i++)
				result.add(right.get(i));
		}
		return result;
	}
	
	
	
	// getRow initializes the appropriate reader if it is not yet initialized, 
	// and reads the next line from the appropriate file.
	private static String[] getRow(String relation, int whichReader, String extension)
	{
		String line = "";
		FileReader f;
		BufferedReader b;
		
		String[] lineAsArray = null;
		try
		{
			if (whichReader==1)
			{
				if (_input == null)
				{
					_input = new FileReader(relation + extension);
					_bufRead = new BufferedReader(_input);
					//System.out.println(relation + " is null");
				}
				f=_input;
				b=_bufRead;
			}
			else
			{
				if (_input2 == null)
				{
					_input2 = new FileReader(relation + extension);
					_bufRead2 = new BufferedReader(_input2);
					//System.out.println(relation + " is null");
				}
				f=_input2;
				b=_bufRead2;
			}
			
			
			line = b.readLine();
			if (line!=null)
			{
				lineAsArray=line.split(",");
				lineAsArray=mergeQuotedCells(lineAsArray);
				
			}
			
		}
		catch (Exception ex)
		{
			System.out.println("A file with the name " + relation + extension + " could not be found.\n");
			ex.printStackTrace();
			System.exit(-1);
		}
						
		return lineAsArray;	
	}
	
	// just an alias to use for reading input for all left side relations
	public static String[] getRow(String relation)
	{
		return getRow(relation, 1, ".csv");
	}
	
	public static String[] getRow(String relation, String extension)
	{
		return getRow(relation, 1, extension);
	}
	public static String[] getRowInDifRelation(String relation, String extension)
	{
		return getRow(relation, 2, extension);
	}
	
	// just an alias to use for reading input for all right side relations
	public static String[] getRowInDifRelation(String relation)
	{
		return getRow(relation, 2, ".csv");
	}
	
	private static String[] mergeQuotedCells(String[] lineAsArray)
	{
		// check to see if a cell begins with a ", then merge it until we find one that ends in a "
		String[] mergingQuotedCells = new String[lineAsArray.length];
		for (int i=0; i<lineAsArray.length; i++)
		{
			String mergedCell = lineAsArray[i];
			if (lineAsArray[i].startsWith("\""))
			{
				
				int j=i+1;
				while (!lineAsArray[j].endsWith("\""))
				{
					mergedCell += "," + lineAsArray[j];
					j++;
				}
				mergedCell += "," + lineAsArray[j];
				i=j;
				//System.out.println(mergedCell);
			}
			mergingQuotedCells[i] = mergedCell;
		}
		
		lineAsArray=mergingQuotedCells.clone();
		int nonNullCnt = 0;
		for (int i=0; i<lineAsArray.length; i++)
		{
			if (lineAsArray[i]!=null)
				nonNullCnt++;
		}
		
		String[] nonNullLine = new String[nonNullCnt];
		int cnt = 0;
		for (int i=0; i<lineAsArray.length; i++)
		{
			if (lineAsArray[i]!=null)
			{
				// replace the first quote
				if(lineAsArray[i].startsWith("\""))
					lineAsArray[i]=lineAsArray[i].replaceFirst("\"","");
				// replace the last quote
				if(lineAsArray[i].endsWith("\""))
					lineAsArray[i]=lineAsArray[i].substring(0,lineAsArray[i].length()-1);
						
				nonNullLine[cnt] = lineAsArray[i];
				cnt++;
			}
		}
		return nonNullLine;
	}
	
	// putRow initializes an output file (if it has not yet been done) and prints a row to it
	public static void putRow(String relation, String row)
	{
		putRow(relation, row, ".csv");
	}
	public static void putRow(String relation, String row, String extension)
	{
			 
		try
		{
			if (_output == null)
			{
				_output = new FileWriter(relation + extension);
				_bufWrite = new BufferedWriter(_output);
				_bufWrite.write(row);
			}
			else
				_bufWrite.append(row);
			//System.out.println(row);
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
				
			
	}
	
	
	
	// just a utility function to turn arrays into strings with spaces between each element 
	private static String flattenArray(String[] arr, String delimiter)
	{
		String result = "";
		for (int i=0; i<arr.length; i++)
			result+=arr[i] + delimiter;
		
		if (result.endsWith(","))
			result=result.substring(0,result.length()-1);
		
		return result.trim();
	}
	
	// closes the output and input files, since we don't do that for ease of programming
	protected void finalize() throws Throwable
	{
		try
		{
			_input.close();
			_bufRead.close();
			_input2.close();
			_bufRead2.close();
			_output.close();
			_bufWrite.close();
		} 
		finally 
		{
			super.finalize();
		}
	}


}
