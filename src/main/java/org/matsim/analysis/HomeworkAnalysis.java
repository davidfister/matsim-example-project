package org.matsim.analysis;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeworkAnalysis {
    static boolean isCloseToU2(Coord coord){
        Coord pankowKirche = new Coord(798738,5833686);
        Coord ossietzkyPlatz = new Coord(798323,5834760);
        Coord stockholmer = new Coord(797147,5832492);
        Coord Wollankstraße = new Coord(797727,5832901);
        Coord Rathaus = new Coord(798288,5833457);
        Coord Kirche = new Coord(798815,5833690);
        Coord Klaustaler = new Coord(799601,5833981);
        Coord Heinersdorf = new Coord(799991,5834432);


        var a = CoordUtils.calcEuclideanDistance(coord, pankowKirche);
        var b = CoordUtils.calcEuclideanDistance(coord, ossietzkyPlatz);
        var c = CoordUtils.calcEuclideanDistance(coord, stockholmer);
        var d = CoordUtils.calcEuclideanDistance(coord, Wollankstraße);
        var e = CoordUtils.calcEuclideanDistance(coord, Rathaus);
        var f = CoordUtils.calcEuclideanDistance(coord, Kirche);
        var g = CoordUtils.calcEuclideanDistance(coord, Klaustaler);
        var h = CoordUtils.calcEuclideanDistance(coord, Heinersdorf);



        return a <= 500 || b <= 500 || c <= 500 || d <= 500 || e <= 500 || f <= 500 || g <= 500 || h <= 500;
    }

    static void analyzeUsage(String path, String scenario, String keyword){
        Population population = PopulationUtils.readPopulation(path);

        System.out.println(population.getPersons().size());
        final int simulation_time_minutes = 60*24*2;
        int[] buckets_pt = new int[simulation_time_minutes];
        int[] buckets = new int[simulation_time_minutes];

        for (Person person : population.getPersons().values()) {
            List<Activity> activities = TripStructureUtils.getActivities(person.getSelectedPlan(), TripStructureUtils.StageActivityHandling.StagesAsNormalActivities);
            List<Leg> legs = TripStructureUtils.getLegs(person.getSelectedPlan());




            for (var l : legs) {
                if (l.getMode().equals("pt")) {
                    int start = (int) Math.round(l.getDepartureTime().seconds() / 60);
                    int end = (int) Math.round(start + l.getTravelTime().seconds() / 60);
                    for (int i = start; i <= end; i++) {
                        buckets_pt[i]++;
                    }

                    if (l.getRoute().getRouteDescription().contains(keyword)) {
                        System.out.println(l.getRoute().getRouteDescription());
                        for (int i = start; i <= end; i++) {
                            buckets[i]++;
                        }
                    }
                }
            }
        }

        try
        {
            FileWriter writer = new FileWriter("./analysis/pt_util_"+scenario+"_"+keyword+".csv");

            writer.append("Minute");
            writer.append(',');
            writer.append("Util_%s".formatted(keyword));
            writer.append(',');
            writer.append("Util_PT");
            writer.append('\n');

            for(int i = 0; i < buckets.length; i++){
                writer.append(Integer.toString(i));
                writer.append(',');
                writer.append(Integer.toString(buckets[i]));
                writer.append(',');
                writer.append(Integer.toString(buckets_pt[i]));
                writer.append('\n');
            }

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    static void analyzeU2ExtResidents(String path, String scenario){
        Population population = PopulationUtils.readPopulation(path);

        Map<String,Integer> travelTimes = new HashMap<String,Integer>();

        for (Person person : population.getPersons().values()) {
            List<Activity> activities = TripStructureUtils.getActivities(person.getSelectedPlan(), TripStructureUtils.StageActivityHandling.StagesAsNormalActivities);
            List<Leg> legs = TripStructureUtils.getLegs(person.getSelectedPlan());
            boolean isClose = false;

            try{
                Activity home = activities.getFirst();
                if(home != null){
                    var homeCoord = home.getCoord();
                    isClose = isCloseToU2(homeCoord);
                }
            } catch (Exception _) {

            }
            if(isClose){
                int totalTravelTime = 0;
                boolean usesPT = false;
                for (var l : legs) {
                    totalTravelTime += (int) (l.getTravelTime().seconds());
                    /*if (l.getMode().equals("pt")) {
                        usesPT = true;
                    }*/
                }
                //if(usesPT){
                travelTimes.put(person.getId().toString(),totalTravelTime);
                //}

            }

        }
        try
        {
            FileWriter writer = new FileWriter("./analysis/persons_"+scenario+".csv");

            writer.append("ID");
            writer.append(',');
            writer.append("TravelTime");
            writer.append('\n');

            for(var i : travelTimes.entrySet()){
                writer.append(i.getKey());
                writer.append(',');
                writer.append(Integer.toString(i.getValue()));
                writer.append('\n');

            }

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }


    }

    static void analyze500m(String path, String scenario){
        Population population = PopulationUtils.readPopulation(path);

        Map<String,Integer> travelTimes = new HashMap<String,Integer>();

        try
        {
            FileWriter writer = new FileWriter("./analysis/agentList_"+scenario+".csv");

            for (Person person : population.getPersons().values()) {
                List<Activity> activities = TripStructureUtils.getActivities(person.getSelectedPlan(), TripStructureUtils.StageActivityHandling.StagesAsNormalActivities);
                List<Leg> legs = TripStructureUtils.getLegs(person.getSelectedPlan());
                boolean isClose = false;

                try{
                    Activity home = activities.getFirst();
                    if(home != null){
                        var homeCoord = home.getCoord();
                        isClose = isCloseToU2(homeCoord);
                    }
                } catch (Exception _) {

                }
                if(isClose){
                    writer.append(person.getId().toString());
                    writer.append(",");
                    writer.append(activities.getFirst().getType());
                    writer.append('\n');

                }

            }

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }


    }


    static void main() {
        //for (var s : new String[]{"U2", "U9", "S41", "S42", "S25","S85","S2","S26"}){
        for (var s : new String[]{"M1-"}){
            analyzeUsage("output/berlin-v7.1-1pct.output_experienced_plans.xml", "base",s);
            analyzeUsage("output/berlin-v7.1-1pct-u2-u9-extension.output_experienced_plans.xml", "policy",s);
        }
        //analyzeU2Usage("output/berlin-v7.1-1pct-u2-extension.output_experienced_plans.xml", "policy");

        //analyzeU2ExtResidents("output/berlin-v7.1-1pct.output_experienced_plans.xml", "base");
        //analyzeU2ExtResidents("output/berlin-v7.1-1pct-u2-extension.output_experienced_plans.xml", "policy");

        //analyze500m("output/berlin-v7.1-1pct-u2-u9-extension.output_experienced_plans.xml", "u2u9policy");
    }
}