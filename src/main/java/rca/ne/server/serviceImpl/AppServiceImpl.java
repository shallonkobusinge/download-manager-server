package rca.ne.server.serviceImpl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import rca.ne.server.dtos.CreateLinkDTO;
import rca.ne.server.models.Link;
import rca.ne.server.models.Website;
import rca.ne.server.repository.LinkRepository;
import rca.ne.server.repository.WebsiteRepository;
import rca.ne.server.services.AppService;
import rca.ne.server.utils.ResourceNotFoundException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime currentTime = LocalDateTime.now();
        Website website = new Website();
        website.setStartDate(LocalDateTime.now());


        Optional<Website> websiteExists = websiteRepository.findByName(webpage);
        try {
            if (websiteExists.isPresent()) {
                throw new ResourceNotFoundException("Website already exists");
            }


            if (!isValid(webpage)) {
                throw new ResourceNotFoundException("Website", "url", webpage);
            }

            // Create URL object
            URL url = new URL(webpage);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = null;
            if (!(connection instanceof HttpURLConnection)) {
                throw new ResourceNotFoundException("Url is not a valid http url");
            }
            website.setName(url.getHost());
            BufferedReader readr =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            boolean fileDirectory = new File("src/main/resources/static/websites/" + url.getHost()).mkdir();

            File file = new File("src/main/resources/static/websites/" + url.getHost() + "/" + url.getHost() + ".html");
            FileWriter fileWriter;
            if (!file.exists()) {
                fileWriter = new FileWriter(file);
            } else {
                fileWriter = new FileWriter(file, true);
            }

            BufferedWriter writer = new BufferedWriter(fileWriter);

            // read each line from stream till end
            String line;
            while ((line = readr.readLine()) != null) {
                writer.write(line);
            }
            readr.close();
            writer.close();
            website.setNumberOfKilobytesDownloaded(filesize_in_kiloBytes(file));
            website.setEndDate(LocalDateTime.now());
            website.setTotalElapsedTime(website.getEndDate().minusNanos(website.getStartDate().getNano()));

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
                saveLink(element.attr("href"), website);

            }
//            System.out.println("=========================================================");
//            System.out.println("LINKKKKKKKKKKKKKKKKS");
//            for (String single : linksFromTheSite) {
//                System.out.println(single);
//            }
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
    public Website findByName(String name) throws MalformedURLException {
        URL url = new URL(name);
        //find by name if not found create a new website
        Optional<Website> website = websiteRepository.findByName(url.getHost());
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
    public Link saveLink(String name, Website website) throws IOException {
        File file = new File("C:\\Users\\B.User\\Documents\\JAVA\\index.html");

         Link link = new Link();
        link.setWebsite(website);
        link.setLinkName(name);
        link.setTotalElapsedTime(website.getEndDate().minusNanos(website.getStartDate().getNano()));
        link.setNumberOfKilobytesDownloaded(filesize_in_kiloBytes(file));
        System.out.println("=========================================================");
        System.out.println("Website Name "+link.getWebsite().getName());
        System.out.println("Link Name "+link.getLinkName());
        System.out.println("Total Elapsed Time "+link.getTotalElapsedTime());
        System.out.println("Number of Kilobytes Downloaded "+link.getNumberOfKilobytesDownloaded());
        System.out.println("=========================================================");
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
            System.out.println("To url "+new URL(url).toURI());
//            new URL(url).toURI();

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
