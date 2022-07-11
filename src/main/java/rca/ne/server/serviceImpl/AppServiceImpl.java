package rca.ne.server.serviceImpl;

import org.springframework.stereotype.Service;
import rca.ne.server.dtos.CreateLinkDTO;
import rca.ne.server.models.Link;
import rca.ne.server.models.Website;
import rca.ne.server.repository.LinkRepository;
import rca.ne.server.repository.WebsiteRepository;
import rca.ne.server.services.AppService;
import rca.ne.server.utils.ResourceNotFoundException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;

@Service
public class AppServiceImpl implements AppService {
    private final WebsiteRepository websiteRepository;
    private final LinkRepository linkRepository;

    public AppServiceImpl(WebsiteRepository websiteRepository, LinkRepository linkRepository) {
        this.websiteRepository = websiteRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    public Website downloadWebpage(String webpage) {
        System.out.println("Downloading webpage: " + webpage);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime currentTime = LocalDateTime.now();
        Website website = findByName(webpage);
        try {
            if(!isValid(webpage)){
                throw new ResourceNotFoundException("Website", "url", webpage);
            }
            website.setStartDate(currentTime);
            // Create URL object
            URL url = new URL(webpage);
            BufferedReader readr =
                    new BufferedReader(new InputStreamReader(url.openStream()));
            // Enter filename in which you want to download
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter("index.html"));

            // read each line from stream till end
            String line;
            while ((line = readr.readLine()) != null) {
                writer.write(line);
            }
            readr.close();
            writer.close();
            website.setEndDate(LocalDateTime.now());
            File file = new File("index.html");
            website.setNumberOfKilobytesDownloaded(filesize_in_kiloBytes(file));
            website.setTotalElapsedTime(website.getEndDate().minusNanos(website.getStartDate().getNano()));
            System.out.println("Name "+website.getName());
            System.out.println("Start Date "+website.getStartDate());
            System.out.println("End Date "+website.getEndDate());
            System.out.println("Number of Kilobytes Downloaded "+website.getNumberOfKilobytesDownloaded());
            System.out.println("Total Elapsed Time "+website.getTotalElapsedTime());

             websiteRepository.save(website);
            Set<String> linksFromTheSite = new HashSet<>();
            Document doc = Jsoup.connect(webpage)
                    .data("query", "Java")
                    .userAgent("Mozilla")
                    .userAgent("Chrome")
                    .userAgent("Firefox")
                    .userAgent("Safari")
                    .userAgent("Opera")
                    .userAgent("IE")
                    .cookie("auth", "token")
                    .timeout(3000)
                    .get();
            Elements elements = doc.select("a[href]");

            CreateLinkDTO createLinkDTO = new CreateLinkDTO();
            for (Element element : elements) {
                linksFromTheSite.add(element.attr("href"));
                createLinkDTO.setLinkName(element.attr("href"));
                createLinkDTO.setWebsiteName(webpage);

            }
            for (String single : linksFromTheSite) {
                System.out.println(single);
            }
        }

        // Exceptions
        catch (MalformedURLException mue) {
            System.out.println("Malformed URL Exception raised");
        }
        catch (IOException ie) {
            System.out.println("IOException raised");
        }
        return website;
    }

    @Override
    public Website findByName(String name) {
        //find by name if not found create a new website
        Optional<Website> website = websiteRepository.findByName(name);
        if(website.isEmpty()){
            downloadWebpage(name);
        }
      return website.get();
    }


    @Override
    public List<Website> getAllWebsitesDownloaded() {
        return websiteRepository.findAll();
    }

    @Override
    public List<Link> getAllLinksDownloaded() {
        return linkRepository.findAll();
    }

    @Override
    public Link saveLink(CreateLinkDTO dto) throws IOException {
        Website website = findByName(dto.getWebsiteName());
         Link link = new Link();
        link.setWebsite(website);
        link.setTotalElapsedTime(website.getEndDate().minusNanos(website.getStartDate().getNano()));
        link.setNumberOfKilobytesDownloaded(filesize_in_kiloBytes(new File(link.getLinkName())));
        linkRepository.save(link);
        return link;
    }

    @Override
    public String getHello() {
        return "Hello";
    }


    public static boolean isValid(String url) {
        /* Try creating a valid URL */
        try {

            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }


    private static String filesize_in_kiloBytes(File file) {
        return (double) file.length()/1024+"  kb";
    }


   //current total elapsed time
    private static long getTotalElapsedTime(Website website) {
        return website.getEndDate().getNano() - website.getStartDate().getNano();
    }
}