import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CityDistanceCalculator {

    private static final double R = 6371.0; // Earth radius in kilometers
    private static Map<String, Coordinates> cityCoordinates = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            BufferedReader file = new BufferedReader(new FileReader("OriginalData.txt"));
            BufferedWriter outputFile = new BufferedWriter(new FileWriter("IntermediateFile.txt"));

            System.out.print("Enter the number of cities you want to search for: ");
            int n = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            String[] cities = new String[n];

            for (int i = 0; i < n; i++) {
                System.out.print("Enter city " + (i + 1) + ": ");
                cities[i] = scanner.nextLine();
            }

            for (String city : cities) {
                searchAndWriteToFile("OriginalData.txt", outputFile, city);
            }

            outputFile.close();
            file.close();

            BufferedReader nowFindDist = new BufferedReader(new FileReader("IntermediateFile.txt"));
            BufferedWriter distanceStore = new BufferedWriter(new FileWriter("FinalFile.txt"));

            writeDistance(nowFindDist, distanceStore);

            distanceStore.close();
            nowFindDist.close();

            // Find shortest route
            findShortestDistances(scanner);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the Scanner at the end of the main method
            scanner.close();
        }
    }

    //private static double getLatitudeForCity(String city) {
        //return cityCoordinates.get(city).getLatitude();
    //}

    //private static double getLongitudeForCity(String city) {
        //return cityCoordinates.get(city).getLongitude();
    //}

    private static void searchAndWriteToFile(String filePath, BufferedWriter outputFile, String city) throws IOException {
        try (BufferedReader file = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(city)) {
                    System.out.println(line);
                    outputFile.write(line);
                    outputFile.newLine();
                }
            }
        }
    }

    private static void writeDistance(BufferedReader nowFindDist, BufferedWriter distanceStore) throws IOException {
        String line;
    
        while ((line = nowFindDist.readLine()) != null) {
            String[] parts = line.split(" - ");
            String cityName = parts[0].trim();
            String[] coordinates = parts[1].split(" ");
            double latitude = Double.parseDouble(coordinates[0]);
            double longitude = Double.parseDouble(coordinates[1]);
    
            // Print the city name as it is added to the map
            System.out.println("Adding city: " + cityName);
    
            cityCoordinates.put(cityName, new Coordinates(latitude, longitude));
        }
    
        for (Map.Entry<String, Coordinates> entry1 : cityCoordinates.entrySet()) {
            String city1 = entry1.getKey();
            Coordinates coord1 = entry1.getValue();
    
            for (Map.Entry<String, Coordinates> entry2 : cityCoordinates.entrySet()) {
                String city2 = entry2.getKey();
                Coordinates coord2 = entry2.getValue();
    
                if (!city1.equals(city2)) {
                    double distance = haversine(coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude());
                    System.out.printf("%s to %s distance = %.2f kilometers\n", city1, city2, distance);
                    distanceStore.write(String.format("%s to %s distance = %.2f kilometers\n", city1, city2, distance));
                }
            }
        }
    }
    

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        lat1 = toRadians(lat1);
        lon1 = toRadians(lon1);
        lat2 = toRadians(lat2);
        lon2 = toRadians(lon2);

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2.0) * Math.sin(dlat / 2.0) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2.0) * Math.sin(dlon / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return R * c;
    }

    private static double toRadians(double degree) {
        return degree * (Math.PI / 180.0);
    }

    private static void findShortestDistances(Scanner scanner) {
        try {
            String initialCity, finalCity;
    
            // Print available city names for reference
            System.out.println("Available Cities:");
            for (String city : cityCoordinates.keySet()) {
                System.out.println(city);
            }
    
            // Get valid initial city
            do {
                System.out.print("Enter the initial city: ");
                initialCity = scanner.nextLine().trim();
    
                if (!cityCoordinates.containsKey(initialCity)) {
                    System.out.println("Invalid input for initial city. Please try again.");
                }
            } while (!cityCoordinates.containsKey(initialCity));
    
            // Get valid final city
            do {
                System.out.print("Enter the final city: ");
                finalCity = scanner.nextLine().trim();
    
                if (!cityCoordinates.containsKey(finalCity)) {
                    System.out.println("Invalid input for final city. Please try again.");
                }
            } while (!cityCoordinates.containsKey(finalCity));
    
            // Use Dijkstra's algorithm to find the shortest route
            List<String> shortestRoute = findShortestRoute(cityCoordinates, initialCity, finalCity);
    
            // Display and save the shortest route
            System.out.println("Shortest Route:");
            for (String city : shortestRoute) {
                System.out.println(city);
            }
    
            // Save the shortest route to a file (ShortestRoute.txt)
            saveShortestRoute(shortestRoute);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private static List<String> findShortestRoute(Map<String, Coordinates> cityCoordinates, String initialCity, String finalCity) {
        int vertices = cityCoordinates.size();
        int[][] graph = new int[vertices][vertices];
        initializeGraph(cityCoordinates, graph);
    
        int initialCityIndex = getCityIndex(cityCoordinates, initialCity);
        int finalCityIndex = getCityIndex(cityCoordinates, finalCity);
    
        double[] shortestDistancesDouble = dijkstra(graph, initialCityIndex);
    
        // Convert double distances to int
        int[] shortestDistances = new int[shortestDistancesDouble.length];
        for (int i = 0; i < shortestDistancesDouble.length; i++) {
            shortestDistances[i] = (int) shortestDistancesDouble[i];
        }
    
        return getShortestRoute(cityCoordinates, shortestDistances, finalCityIndex, initialCityIndex);
    }
    
    
    
    

    private static void initializeGraph(Map<String, Coordinates> cityCoordinates, int[][] graph) {
        int i = 0;
        Map<String, Integer> cityIndexMap = new HashMap<>();

        for (String city : cityCoordinates.keySet()) {
            cityIndexMap.put(city, i);
            i++;
        }

        for (String city1 : cityCoordinates.keySet()) {
            Coordinates coord1 = cityCoordinates.get(city1);
            int index1 = cityIndexMap.get(city1);

            for (String city2 : cityCoordinates.keySet()) {
                Coordinates coord2 = cityCoordinates.get(city2);
                int index2 = cityIndexMap.get(city2);

                double distance = haversine(coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude());
                graph[index1][index2] = (int) distance;
            }
        }
    }

    private static int getCityIndex(Map<String, Coordinates> cityCoordinates, String city) {
        int i = 0;
        for (String cityName : cityCoordinates.keySet()) {
            if (cityName.equals(city)) {
                return i;
            }
            i++;
        }
        return -1; // City not found
    }

    private static List<String> getShortestRoute(Map<String, Coordinates> cityCoordinates, int[] shortestDistances, int finalCityIndex, int initialCityIndex) {
        List<String> shortestRoute = new ArrayList<>();
        Map<Integer, String> indexCityMap = new HashMap<>();
    
        for (Map.Entry<String, Coordinates> entry : cityCoordinates.entrySet()) {
            int index = getCityIndex(cityCoordinates, entry.getKey());
            indexCityMap.put(index, entry.getKey());
        }
    
        int currentIndex = finalCityIndex;
        while (currentIndex != -1) {
            shortestRoute.add(indexCityMap.get(currentIndex));
            currentIndex = getPreviousCityIndex(shortestDistances, currentIndex, initialCityIndex);
        }
    
        Collections.reverse(shortestRoute);
    
        // Add the initial city to the route
        shortestRoute.add(0, indexCityMap.get(initialCityIndex));
    
        return shortestRoute;
    }        
    

    private static int getPreviousCityIndex(int[] shortestDistances, int currentIndex, int initialCityIndex) {
        for (int i = 0; i < shortestDistances.length; i++) {
            if (i != currentIndex && i != initialCityIndex) {
                if (Math.abs(shortestDistances[i] + haversine(0, 0, 0, 0) - shortestDistances[currentIndex]) < 0.001) {
                    return i;
                }
            }
        }
        return -1;
    }
    

private static double[] dijkstra(int[][] graph, int source) {
    int vertices = graph.length;
    double[] distances = new double[vertices];
    boolean[] visited = new boolean[vertices];

    // Initialize distances array
    Arrays.fill(distances, Double.MAX_VALUE);
    distances[source] = 0;

    for (int i = 0; i < vertices - 1; i++) {
        int minVertex = findMinVertex(distances, visited);
        visited[minVertex] = true;

        for (int j = 0; j < vertices; j++) {
            if (graph[minVertex][j] != 0 && !visited[j]) {
                double newDistance = distances[minVertex] + graph[minVertex][j];
                if (newDistance < distances[j]) {
                    distances[j] = newDistance;
                }
            }
        }
    }

    return distances;
}


    private static int findMinVertex(double[] distances, boolean[] visited) {
        int minVertex = -1;
        for (int i = 0; i < distances.length; i++) {
            if (!visited[i] && (minVertex == -1 || distances[i] < distances[minVertex])) {
                minVertex = i;
            }
        }
        return minVertex;
    }
    
    private static void saveShortestRoute(List<String> shortestRoute) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("ShortestRoute.txt"))) {
            for (String city : shortestRoute) {
                System.out.println("Adding city to ShortestRoute.txt: " + city); // Debug statement
                writer.write(city);
                writer.newLine(); // Add newline character after each city
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
       

    private static class Coordinates {
        private double latitude;
        private double longitude;
    
        //public Coordinates() {
            // Default constructor
        //}
    
        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    
        public double getLatitude() {
            return latitude;
        }
    
        //public void setLatitude(double latitude) {
            //this.latitude = latitude;
        //}
    
        public double getLongitude() {
            return longitude;
        }
    
        //public void setLongitude(double longitude) {
        //    this.longitude = longitude;
        //}
    }
    
}
