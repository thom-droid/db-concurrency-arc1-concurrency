package com.example.simplebooking.config;

import com.example.simplebooking.movie.Movie;
import com.example.simplebooking.movie.MovieRepository;
import com.example.simplebooking.seat.Seat;
import com.example.simplebooking.seat.SeatRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    ApplicationRunner seedDatabase(SeatRepository seatRepository,
                                   MovieRepository movieRepository
    ) {
        return args -> seed(seatRepository, movieRepository);
    }

    @Transactional
    void seed(SeatRepository seatRepository, MovieRepository movieRepository) {
        if (seatRepository.count() > 0 || movieRepository.count() > 0) {
            log.info("Skipping seed; data already present.");
            return;
        }

        // User alice = userRepository.save(User.builder().email("alice@example.com").displayName("Alice").build());
        // User bob = userRepository.save(User.builder().email("bob@example.com").displayName("Bob").build());
        // log.info("Seeded users {} and {}", alice.getEmail(), bob.getEmail());

        // for (int i = 1; i <= 10; i++) {
        Seat seat = new Seat();
        seatRepository.save(seat);

        Movie movie = new Movie();
        movieRepository.save(movie);
        // }
        log.info("Seeded {} seats", seatRepository.count());
    }
}
