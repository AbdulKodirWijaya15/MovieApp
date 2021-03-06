package com.KamilIsmail.MovieApp.scheduled.TVGuideController;

import com.KamilIsmail.MovieApp.Constants;
import com.KamilIsmail.MovieApp.DAO.TVGuideDao;
import com.KamilIsmail.MovieApp.dictionery.TVChanels;
import com.KamilIsmail.MovieApp.scheduled.TVGuideBean.MovieBean;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.BufferOverflowException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.zip.GZIPInputStream;

//http://epg.koditvepg2.com/PL/guide.xml.gz
public class ParseTVGuide {

    private Logger log = LoggerFactory.getLogger(ParseTVGuide.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss"); //20180916000500

    /**
     * Przetworzenie pobranego pliku xml zawierającego program telewizyjny.
     */
    public ArrayList<MovieBean> run() {
        ArrayList<MovieBean> movieBeanList;
        try {
            URL url = new URL(Constants.getTvGuideUrlAlternative()); //wykorzystanie alternatywnego lacza
            URLConnection connection = url.openConnection();
            InputStream stream = connection.getInputStream();
            stream = new GZIPInputStream(stream);
            InputSource is = new InputSource(stream);
            InputStream input = new BufferedInputStream(is.getByteStream());
            OutputStream output = new FileOutputStream("/Users/kamilismail/Downloads/guide.xml");
            byte data[] = new byte[2097152];
            long total = 0;
            int count;

            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (BufferOverflowException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            File file = new File("/Users/kamilismail/Downloads/guide.xml");
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = documentBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("programme");
            movieBeanList = new ArrayList<>();
            TVChanels tvChanels = new TVChanels();
            for (int i = 0; i < nodeList.getLength(); i++) {
                MovieBean temp = getMovie(nodeList.item(i), tvChanels);
                if (temp != null) {
                    movieBeanList.add(temp);
                    log.info("\nADDED NEW MOVIE: " + temp.toString());
                }
            }
            return getBestMovies(movieBeanList);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
    }

    /**
     * Wybranie z listy najlepszych filmów.
     * @param list
     * @return
     */
    private ArrayList<MovieBean> getBestMovies(ArrayList<MovieBean> list) {
        list.sort((movie1, movie2) -> Float.compare(movie2.getRating(), movie1.getRating()));
        return list;
    }

    /**
     * Pobranie tagów i atrybutów kolejnych elementów pliku xml oraz utworzenie z pobranych danych nowego obiektu
     * przechowujacego dane filmu.
     * @param node
     * @param tvChanels
     * @return
     */
    private MovieBean getMovie(Node node, TVChanels tvChanels){
        String date, channel, title, description = "";
        MovieBean movieBean = null;

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            //sprawdzenie czy jest to film + dodanie ratingu
            Element element = (Element) node;
            try {
                channel = getAttribute("channel", element);
                if (tvChanels.ifContainsAlternative(channel)) { //alternatywne
                    date = getAttribute("start", element);
                    Date parsedDate = parseStringToDate(date);
                    //parsedDate = DateUtils.addHours(parsedDate, 2);
                    if (!checkEmissionTime(parsedDate))
                        return null;
                    title = getTagValue("title", element);
                    if (getTagValue("desc", element) != null)
                        description = getTagValue("desc", element);
                    String productionYear = getTagValue("date", element); //alternatywne
                    movieBean = new MovieBean(parsedDate, channel.substring(0,channel.indexOf(".")), title, description);
                    movieBean.setProductionYear(Integer.parseInt(productionYear));
                    if (tvChanels.excludeProductions(movieBean.getDescription()))
                        return null;
                } else
                    return null;
            } catch (Exception e) {
                return null;
            }
        }
        //movieBean.parseYear();
        if (movieBean.setFilmwebRating())
            return movieBean;
        else return null;
    }

    /**
     * Sprawdzenie czy czas rozpoczecia emisji jest w tym samym dniu co aktualny czas na serwerze oraz sparwdzenie
     * czy rozpoczęcie emisji jest między godzinami 18-24.
     * @param date
     * @return
     */
    private Boolean checkEmissionTime(Date date) {
        Date newDate = new Date();
        LocalDate localDate = newDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate emissionDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (localDate.getDayOfMonth() == emissionDate.getDayOfMonth()) {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);   // assigns calendar to given date
            int time = calendar.get(Calendar.HOUR_OF_DAY);
            if (time >= 18 && time <= 24)
                return true;
            else return false;
        } else return false;
    }

    /**
     * Konwersja Stringa na typ Date
     * @param date
     * @return
     */
    private Date parseStringToDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Pobranie atrybutów kolejnych elementów pliku .xml
     * @param tag
     * @param element
     * @return
     */
    private String getAttribute(String tag, Element element) {
        return element.getAttribute(tag);
    }

    /**
     * Pobranie tagów kolejnych elementów pliku .xml
     * @param tag
     * @param element
     * @return
     */
    private String getTagValue(String tag, Element element) {
        try {
            return element.getElementsByTagName(tag).item(0).getChildNodes().item(0).getNodeValue();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
