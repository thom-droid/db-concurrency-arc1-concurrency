package com.example.simplebooking.seat;

import com.example.simplebooking.movie.Movie;
import com.example.simplebooking.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final MovieService movieService;

    /**
     * 자리를 예약한다.
     *
     * @param seatId 자리 아이디
     * @return 201 CREATED
     */
    @PostMapping("/reserve-unsafe/{seatId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> reserveUnsafe(@PathVariable Long seatId) {
        seatService.reserveUnsafe(seatId);
        return ResponseEntity.ok("created");
    }

    /**
     * <p>
     *     트랜잭션 바운더리를 테스트한다.
     * </p>
     * <p>
     *     컨트롤러 단에서 트랜잭션을 설정하더라도 트랜잭션의 바운더리는 설정이 된다.
     *     하지만 아래처럼 비즈니스 로직에서 발생한 예외를 처리하게 되면 {@code movieService.insertMovie()}
     *     가 성공하고 {@code seatService.reserveUnsafeWithoutTransactional()}가 실패했을 때 seat만
     *     롤백되어 이 API 내 트랜잭션의 원자성이 깨지게 된다.
     * </p>
     * <p>
     *     물론 기능적으로는 컨트롤러에서 트랜잭션을 관리하여 서비스와 동일하게 구현할 수도 있지만, 컨트롤러는 웹과 관련된
     *     로직만 수행하고 비즈니스 로직은 data layer와 맞물려 있으므로 데이터 로직만 처리하는 것이 좋다.
     * </p>
     */
    @PostMapping("/transaction-on-controller/{seatId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ResponseEntity<?> testTransactionBoundary(@PathVariable Long seatId, String title) {

        Movie movie = new Movie();
        movie.setTitle(title);

        movieService.insertMovie(movie);
        boolean reserved;

        // 여기서 비즈니스 예외를 처리하면 위 movieSerivce.insertMovie와의 원자성이 깨지게 된다.
        try {
            reserved = seatService.reserveUnsafeWithoutTransactional(seatId);
        } catch (IllegalStateException ex) {
            reserved = false;
        }
        List<Movie> allMovies = movieService.getAllMovies();

        Map<String, Object> test = Map.of("test", reserved, "movies", allMovies);
        return ResponseEntity.ok().body(test);
    }

    /**
     * <p>
     *      자기 자신에게 선언되어 있는 메서드를 트랜잭션에 참여시키기 위해 {@code this.txMethod()} 와 같이 호출하더라도
     *      이 메서드는 트랜잭션에 참여하지 않는다.
     * </p>
     *
     */
    @PostMapping("/self-invocation-inner-method/{seatId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> testSelfInvocationTransactionWork(@PathVariable Long seatId) {
        seatService.outerCall(seatId);
        return ResponseEntity.ok().build();
    }
}
