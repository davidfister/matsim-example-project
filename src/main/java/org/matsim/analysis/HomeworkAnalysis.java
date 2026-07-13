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
        var a = CoordUtils.calcEuclideanDistance(coord, pankowKirche);
        var b = CoordUtils.calcEuclideanDistance(coord, ossietzkyPlatz);
        return a <= 1500 || b <= 1500;
    }

    static void analyzeU2Usage(String path, String scenario){
        Population population = PopulationUtils.readPopulation(path);

        final int simulation_time_minutes = 60*24*2;
        int[] buckets_pt = new int[simulation_time_minutes];
        int[] buckets = new int[simulation_time_minutes];

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


            for (var l : legs) {

                if (l.getMode().equals("pt")) {
                    int start = (int) Math.round(l.getDepartureTime().seconds() / 60);
                    int end = (int) Math.round(start + l.getTravelTime().seconds() / 60);
                    for (int i = start; i <= end; i++) {
                        buckets_pt[i]++;
                    }

                    if (l.getRoute().getRouteDescription().contains("U2")) {
                        for (int i = start; i <= end; i++) {
                            buckets[i]++;
                        }
                    }
                }
            }
        }

        try
        {
            FileWriter writer = new FileWriter("./analysis/pt_util_"+scenario+".csv");

            writer.append("Minute");
            writer.append(',');
            writer.append("Util_U2");
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


    static void main() {
        analyzeU2Usage("output/berlin-v7.1-1pct.output_experienced_plans.xml", "base");
        analyzeU2Usage("output/berlin-v7.1-1pct-u2-extension.output_experienced_plans.xml", "policy");

        analyzeU2ExtResidents("output/berlin-v7.1-1pct.output_experienced_plans.xml", "base");
        analyzeU2ExtResidents("output/berlin-v7.1-1pct-u2-extension.output_experienced_plans.xml", "policy");
    }
}