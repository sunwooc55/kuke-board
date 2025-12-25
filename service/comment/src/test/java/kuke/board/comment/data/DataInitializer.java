package kuke.board.comment.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kuke.board.comment.entity.Comment;
import kuke.board.common.snowflake.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest // 실제 스프링 애플리케이션 컨텍스트를 띄워야 DB에 데이터를 넣을 수 있음
public class DataInitializer {
    @PersistenceContext // EntityManager는 스레드에 안전하지 않음. 해당 @를 사용하면 현재 트랜잭션에 딱 맞는 엔티티 매니저를 연결해주어 멀티 스레드 환경에서도 안전하게 사용 가능
    EntityManager entityManager; // Entity 객체를 DB에 저장, 수정, 삭제, 조회하는 역할을 하는 관리자

    @Autowired
    TransactionTemplate transactionTemplate; // 원하는 구간만 쪼개서 트랜잭션을 걸 수 있음.
    Snowflake snowflake = new Snowflake();
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT); // 작업이 끝날 때마다 숫자를 1씩 줄여 0이 될 때까지 메인스레드를 await시켜, 모든 데이터 삽입이 끝날 때 까지 테스트가 종료되지 않게 붙잡아둠

    static final int BULK_INSERT_SIZE = 2000;
    static final int EXECUTE_COUNT = 2500;

    @Test
    void initialize() throws InterruptedException{
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 스레드 풀 생성 (동시에 일 할 스레드 10개)
        for(int i = 0; i < EXECUTE_COUNT; i++){
            executorService.submit(()-> {
                insert(); // 데이터 삽입
                latch.countDown(); // 작업 완료 신호
                System.out.println("latch.getCount() = " + latch.getCount());
            });
        }
        latch.await();  // 모든 작업 끝날 때까지 대기 / 이것이 없다면 메인 스레드는 작업을 스레드 풀에 던져만 놓고 즉시 종료되어버림. 그러면 데이터가 들어가다 말게 됨
        executorService.shutdown(); // 스레드 풀 종료
    }

    void insert(){
        // 트랜잭션 범위 설정
        transactionTemplate.executeWithoutResult(status -> {
            Comment prev = null;
            for(int i=0;i<BULK_INSERT_SIZE; i++){
                Comment comment = Comment.create(
                        snowflake.nextId(),
                        "content",
                        i % 2 == 0 ? null : prev.getCommentId(),  // 짝수면 상위 댓글은 없고, 홀수면 이전 댓글을 상위 댓글로 설정 (2-Depth)
                        1L,
                        1L
                );
                prev = comment;
                entityManager.persist(comment);
            }
        });
    }
}
