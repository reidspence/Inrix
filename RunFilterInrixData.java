import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class RunFilterInrixData {

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		InrixReader IR = new InrixReader();
		ArrayList<InrixTripLL> trips = IR.readCSV("C:/Users/SDM/Desktop/INRIX/InrixSample.csv");
		System.out.print(trips);
		int i = 0;
//		for (InrixTripLL trip : trips) {
//			if (trip.getAvgTimeDelay() < 13 || trip.getTripDuration() < 120) {
//				trips.remove(i);
//			}
//			i++;
//		}
	}

}
