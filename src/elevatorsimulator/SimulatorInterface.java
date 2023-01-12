package elevatorsimulator;

import java.io.*;
import java.util.*;

public class SimulatorInterface {
    private static final String path = System.getProperty("user.dir") + "\\src\\source";

    public SimulatorInterface() {

    }

    private class SimulatorParams {
        boolean generateType;
        int distributionType = 0;
        int algorithmType = 0;
        SimulatorParams(boolean generateType, int distributionType, int algorithmType) {
            this.generateType = generateType;
            this.distributionType = distributionType;
            this.algorithmType = algorithmType;
        }
    }

    public static List<List<String>> readCSV(String filePath) {
        List<List<String>> csvList = new ArrayList<List<String>>();
        File csv = new File(filePath);
        BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new FileReader(csv));
            while ((line = br.readLine()) != null) {
                List<String> aLine = new ArrayList<String>();
                String[] lineArr = line.split(";");
                if (lineArr[0].equals("sep="))
                    continue;
                aLine = Arrays.asList(lineArr);
                csvList.add(aLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return csvList;
    }

    public Simulator createSimulator() {
        SimulatorParams params = inputInterface();
        String fileName = "\\BuildingConfigure.csv";

        String name;
        int numElevator;
        ElevatorConfiguration elevatorConfiguration;
        int[] floorResidents;
        TrafficProfile trafficProfile;
        double timeStep;

        switch (params.distributionType) {
            default:
                trafficProfile = TrafficProfiles.WEEK_DAY_PROFILE;
                break;
            case 2:
                trafficProfile = TrafficProfiles.UNIFORM_PROFILE;
                break;
            case 3:
                trafficProfile = TrafficProfiles.NORMAL_PROFILE;
                break;
        }

        List<List<String>> csv = readCSV(path + fileName);

        Iterator<List<String>> line = csv.iterator();
        List<String> words = line.next();
        if(words.get(0).equals("name")) {
            name = words.get(1);
        } else return null;
        words = line.next();

        if(words.get(0).equals("numElevator")) {
            numElevator = Integer.parseInt(words.get(1));
        } else return null;
        words = line.next();

        if(words.get(0).equals("elevatorConfig")) {
            elevatorConfiguration = new ElevatorConfiguration(Integer.parseInt(words.get(1)),
                    Double.parseDouble(words.get(2)), Double.parseDouble(words.get(3)),
                    Double.parseDouble(words.get(4)), Double.parseDouble(words.get(5)));
        } else return null;

        words = line.next();
        Iterator<String> floorIterator = words.iterator();
        String floorStr = floorIterator.next();
        if(floorStr.equals("floorResidents")) {
            List<Integer> floorList = new ArrayList<Integer>();
            while (floorIterator.hasNext()) {
                floorStr = floorIterator.next();
                floorList.add(Integer.parseInt(floorStr));
            }
            floorResidents = floorList.stream().mapToInt(Integer::intValue).toArray();
        } else return null;

        words = line.next();
        if(words.get(0).equals(("trafficProfile"))) {

        } else return null;

        words = line.next();
        if(words.get(0).equals("simulatorTimeStep")) {
            timeStep = Double.parseDouble(words.get(1));
        } else return null;

        return new Simulator(new Scenario(name, numElevator, elevatorConfiguration, floorResidents, trafficProfile),
                new SimulatorSettings(timeStep, 24 * 60 * 60), SchedulerCreators.creators.get(params.algorithmType - 1),
                params.generateType);
    }

    public SimulatorParams inputInterface() {
        Scanner sc = new Scanner(System.in);
        boolean generateType;
        int distributionType = 0;
        int algorithmType = 0;

        System.out.println("****Check BuildingConfigure.csv in src/source****");
        System.out.println("How generate passengers?");
        System.out.println("1. by List");
        System.out.println("2. by Random Generate");
        System.out.print("> ");
        int type = sc.nextInt();
        generateType = type == 1 ? true : false;

        if (!generateType) {
            System.out.println("Which distribution do you want to use?");
            System.out.println("1. Default Profile");
            System.out.println("2. Uniform distribution");
            System.out.println("3. Normal distribution");
            System.out.print("> ");
            distributionType = sc.nextInt();
        }

        System.out.println("Which \"Algorithm\" do you want to use?");
        System.out.println("1. Collective Control");
        System.out.println("2. Longest Queue First");
        System.out.println("3. Zoning");
        System.out.println("4. Round Robin");
        System.out.println("5. Round Robin(Up-peak)");
        System.out.println("6. Three Passage Group");
        System.out.print("> ");
        algorithmType = sc.nextInt();

        return new SimulatorParams(generateType, distributionType, algorithmType);
    }

    public static Queue<Passenger> createPre_madePassengerList(Simulator simulator) {
        Queue<Passenger> passengerList = new LinkedList<Passenger>();
        String fileName = "\\passenger_list.csv";

        List<List<String>> csv = readCSV(path + fileName);

        Iterator<List<String>> words = csv.iterator();

        words.next();
        while(words.hasNext()) {
            List<String> data = words.next();
            passengerList.add(new Passenger(Long.parseLong(data.get(1)), Integer.parseInt(data.get(2)),
                    Integer.parseInt(data.get(3)), 1, simulator.getClock().timeFromFormattedTime(data.get(0))));
        }

        return passengerList;
    }
}
