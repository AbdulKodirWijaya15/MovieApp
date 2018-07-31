package com.KamilIsmail.MovieApp.service;

import com.KamilIsmail.MovieApp.Constants;
import com.KamilIsmail.MovieApp.DTO.DiscoverDTO;
import com.KamilIsmail.MovieApp.DTO.DiscoverMovieDTO;
import com.KamilIsmail.MovieApp.DTO.DiscoverSeriesDTO;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.tv.TvSeries;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class DiscoverServiceImpl implements DiscoverService {

    /**
     * Metoda zwracajaca w jsonie liste popularnych filmow, popularnych seriali, aktualnie granych filmow w kinach oraz
     * tych nadchodzacych.
     *
     * @return
     * @throws IOException
     */
    @Override
    public DiscoverDTO getJSON() throws IOException {
        Constants constants = new Constants();
        TmdbApi tmdbApi = new TmdbApi(constants.getTmdbAPI());
        MovieResultsPage upcoming = tmdbApi.getMovies().getUpcoming("pl", 1, "");
        MovieResultsPage nowPlaying = tmdbApi.getMovies().getNowPlayingMovies("pl", 1, "");
        MovieResultsPage popularMovies = tmdbApi.getMovies().getPopularMovies("pl", 1);
        TvResultsPage popularSeries = tmdbApi.getTvSeries().getPopular("pl", 1);
        ArrayList<DiscoverMovieDTO> upcomingList = new ArrayList<>();
        ArrayList<DiscoverMovieDTO> nowPlayingList = new ArrayList<>();
        ArrayList<DiscoverMovieDTO> popularMoviesList = new ArrayList<>();
        ArrayList<DiscoverSeriesDTO> popularSeriesList = new ArrayList<>();
        for (MovieDb movie : upcoming.getResults()) {
            upcomingList.add(new DiscoverMovieDTO(movie.getMediaType().toString(), Integer.toString(movie.getId()),
                    movie.getTitle(), movie.getPosterPath(), movie.getReleaseDate()));
        }
        for (MovieDb movie : nowPlaying.getResults()) {
            nowPlayingList.add(new DiscoverMovieDTO(movie.getMediaType().toString(), Integer.toString(movie.getId()),
                    movie.getTitle(), movie.getPosterPath(), movie.getReleaseDate()));
        }
        for (MovieDb movie : popularMovies.getResults()) {
            popularMoviesList.add(new DiscoverMovieDTO(movie.getMediaType().toString(), Integer.toString(movie.getId()),
                    movie.getTitle(), movie.getPosterPath(), movie.getReleaseDate()));
        }
        for (TvSeries series : popularSeries.getResults()) {
            popularSeriesList.add(new DiscoverSeriesDTO(Integer.toString(series.getId()), series.getName(),
                    series.getPosterPath()));
        }
        return (new DiscoverDTO(upcomingList, nowPlayingList, popularMoviesList, popularSeriesList));
    }
}