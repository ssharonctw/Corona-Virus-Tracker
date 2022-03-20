package io.ssharonctw.coronavirustracker.controllers;

import io.ssharonctw.coronavirustracker.models.LocationStats;
import io.ssharonctw.coronavirustracker.services.CoronavirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

//if it's a restController, use @RestController. All methods in this controller will return a REST response. So it has to be converted into a JSON response and then return back
@Controller //makes this class a String controller (NOT RestController) that will directly return a HTML UI, NOT a rest controller
public class HomeController {
    @Autowired
    CoronavirusDataService coronavirusDataService;

    @GetMapping("/") //map this to the root url, so whenever there's a getMapping /, the home template will be returned (Themeleaf feature)
    public String home(Model model){
        //Spring offers using "model" to fetch data or do whateever and set it in the context that's
        //rendering the page (the home.html template) when calling the controller
        //then in the html, you can access the model and construct things there
        //for example for below, you can access the attrvalue by using ${attrName}
        //model.addAttribute("attrName", "attr value");

        //allStats gets the locationStats from the models (database)
        List<LocationStats> allStats = coronavirusDataService.getAllStats();
        //the below converts the list of object(allStats) into a stream and maps each object to its Integervalue of total cases, and then sum if up
        int totalReportedCases = allStats.stream().mapToInt(stat-> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat-> stat.getDiffFromPreviousDay()).sum();
        model.addAttribute("locationStats", allStats);
        model.addAttribute("totalReportedCases", totalReportedCases);
        model.addAttribute("totalNewCases", totalNewCases);
        //

        return "home"; //this has to be a name that points to the template
    }
}
