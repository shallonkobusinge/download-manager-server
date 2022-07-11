package rca.ne.server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rca.ne.server.dtos.CreateLinkDTO;
import rca.ne.server.models.Website;
import rca.ne.server.services.AppService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AppController {
    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }
    @GetMapping("/")
    public String getHello() {
        return appService.getHello();
    }
    @GetMapping("/websites")
    public ResponseEntity<?> getAllWebsitesDownloaded() {
        return ResponseEntity.ok(appService.getAllWebsitesDownloaded());
    }
    @GetMapping("/links")
    public ResponseEntity<?> getAllLinksDownloaded() {
        return ResponseEntity.ok(appService.getAllLinksDownloaded());
    }
//    @PostMapping("/links")
//    public ResponseEntity<?> saveLink(String link) throws IOException {
//        return ResponseEntity.ok(appService.saveLink(link));
//    }
    //download a webpage give a url
    @PostMapping("/websites")
    public ResponseEntity<?> downloadWebpage(@RequestBody String webpage) {
        System.out.println("Url "+ webpage);
        return ResponseEntity.ok(appService.downloadWebpage(webpage));
    }
}
