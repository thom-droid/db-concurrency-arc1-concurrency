package com.example.simplebooking.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie insertMovie(Movie movie) {
        if (movie.getTitle().isBlank()) {
            throw new IllegalArgumentException("no title must be empty");
        }
        return movieRepository.save(movie);
    }

}
