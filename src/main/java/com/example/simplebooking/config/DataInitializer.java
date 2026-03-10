package com.example.simplebooking.config;

import com.example.simplebooking.movie.Movie;
import com.example.simplebooking.movie.MovieRepository;
import com.example.simplebooking.seat.Seat;
import com.example.simplebooking.seat.Seat2;
import com.example.simplebooking.seat.Seat2Repository;
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
                                   Seat2Repository seat2Repository,
                                   MovieRepository movieRepository
    ) {
        return args -> seed(seatRepository, seat2Repository, movieRepository);
    }

    @Transactional
    void seed(SeatRepository seatRepository,
              Seat2Repository seat2Repository,
              MovieRepository movieRepository) {
            seat2Repository.deleteAll();
            seatRepository.deleteAll();
            movieRepository.deleteAll();

        // User alice = userRepository.save(User.builder().email("alice@example.com").displayName("Alice").build());
        // User bob = userRepository.save(User.builder().email("bob@example.com").displayName("Bob").build());
        // log.info("Seeded users {} and {}", alice.getEmail(), bob.getEmail());

        for (int i = 1; i <= 10; i++) {
            Seat seat = new Seat();
            seatRepository.save(seat);

            Seat2 seat2 = new Seat2();
            seat2Repository.save(seat2);

            Movie movie = new Movie();
            movie.setTitle("Movie " + i);
            movieRepository.save(movie);
        }
    }
}
