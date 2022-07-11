package rca.ne.server.services;

import rca.ne.server.dtos.CreateLinkDTO;
import rca.ne.server.models.Link;
import rca.ne.server.models.Website;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface AppService {

    //download a webpage give a url
    Website downloadWebpage(String url);
    Website findByName(String name) throws MalformedURLException;
    //get all websites downloaded
    List<Website> getAllWebsitesDownloaded();
    //get all links downloaded
    List<Link> getAllLinksDownloaded();
    Link saveLink(String name, Website website) throws IOException;

    String getHello();
}
