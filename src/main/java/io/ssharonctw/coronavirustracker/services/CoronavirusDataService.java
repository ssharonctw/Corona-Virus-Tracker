package io.ssharonctw.coronavirustracker.services;

import io.ssharonctw.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;


//This is a Spring service that will make a call when application runs to fetch the data
@Service
public class CoronavirusDataService {
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    //create a list that stores the location stats, we populate the list everytime we run the request
    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    //make a http call to the raw csv file using the http client available in java
    //@PostConstruct = run when the when the application starts  (a.k.a.  run when the class is constucted)
    //@Scheduled = run every first hour of every day (PostConstruct is not sufficient as it will only run the very first time)
    @PostConstruct
    @Scheduled(cron = "0 0 */1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {
        //the local list is created to avoid concurrency issues
        //so when other people are visiting the site while the list are refreshing,
        //the user still gets the page rendered with the global allStates (that are yet to be updated once the local newStats is populated)
        List<LocationStats> newStats = new ArrayList<>();

        //creating a new client
        HttpClient client = HttpClient.newHttpClient();
        //create a http request with a class using a builder pattern
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL)) //converting the string to URI (Where to access)
                .build();
        //sending the request (synchronous send) with client to get a response
        //the second parameter is "what to do with the body"
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        //print out the response body
        //System.out.println(httpResponse.body());
        System.out.println("data fetched");

        //now we need to parse the fetched data. Here we use a java library called commons csv
        //https://commons.apache.org/proper/commons-csv/user-guide.html
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationstats = new LocationStats();
            String state = record.get("Province/State");
            String parsedState = state.isEmpty()? "Entire Country": state;
            locationstats.setState(parsedState);
            locationstats.setCountry(record.get("Country/Region"));
            //the totalCaseOfLatestDate is the "last column" in the original dataset
            //hence we use record.get(record.size()-1) to get the data of the last column
            //previous date data is hence record.get(record.size()-2)  (the second last column)
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int previousDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationstats.setLatestTotalCases(latestCases);
            locationstats.setDiffFromPreviousDay(latestCases-previousDayCases);
            //System.out.println(locationstats);
            newStats.add(locationstats);
        }

        this.allStats = newStats;
    }
}
