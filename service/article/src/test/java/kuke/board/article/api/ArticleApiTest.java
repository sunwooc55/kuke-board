package kuke.board.article.api;

import kuke.board.article.service.request.ArticleCreateRequest;
import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class ArticleApiTest {
    // http://localhost:9000 주소로 떠 있는 서버에 요청을 보낼 준비를 한다.
    RestClient restClient = RestClient.create("http://localhost:9000"); // RestClient : 자바 코드 내에서 http 요청을 보내고 응답 받을 수 있게 해주는 도구

    @Test
    void createTest(){
        ArticleResponse response = create(new ArticleCreateRequest(
                "hi", "my content", 1L, 1L
        ));
        System.out.println("response = " + response);
    }

    ArticleResponse create(ArticleCreateRequest request){
        return restClient.post() // HTTP POST 메서드로 요청하겠다 선언
                .uri("/v1/articles")
                .body(request) // 자바 객체(ArticleCreateRequest) 를 JSON 으로 변환하여 body에 싣음
                .retrieve() // 전송
                .body(ArticleResponse.class); // 서버가 보내준 응답 JSON을 다시 자바 객체 (ArticleResponse) 로 변환
    }

    @Test
    void readTest(){
        ArticleResponse response = read(261807201869377536L);
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId){
        return restClient.get() // HTTP GET 메서드 사용하겠다 선언
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest(){
        update(261807201869377536L);
        ArticleResponse response = read(261807201869377536L);
        System.out.println("response = " + response);
    }

    void update(Long articleId){
        restClient.put() // HTTP PUT 메서드 사용하겠다 선언
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("hi 2", "my content 22"))
                .retrieve();
    }

    @Test
    void deleteTest(){
        restClient.delete() // HTTP DELETE 메서드 사용하겠다 선언
                .uri("/v1/articles/{articleId}", 261807201869377536L)
                .retrieve();
    }


    @Test
    void readAllTest(){
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&pageSize=30&page=50000")
                .retrieve()
                .body(ArticlePageResponse.class); // 응답으로 온 JSON(게시글 리스트 + 전체 개수 정보)을 ArticlePageResponse 객체 하나로 변환

        System.out.println("response.getArticleCount() = " + response.getArticleCount());
        for (ArticleResponse article : response.getArticles()){
            System.out.println("articleId = " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScrollTest(){
        // 첫 페이지 요청
        List<ArticleResponse> articles1 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() { // 이 응답은 ArticleResponse 가 들어있는 List 라고 명확한 타입 정보 제공
                });

        System.out.println("firstPage");
        for (ArticleResponse articleResponse : articles1) {
            System.out.println("articleResponse.getArticleId() = " + articleResponse.getArticleId());
        }

        // 다음 페이지를 위한 커서 획득
        Long lastArticleId = articles1.getLast().getArticleId();

        // 두 번째 페이지 요청
        List<ArticleResponse> articles2 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5&lastArticleId=%s".formatted(lastArticleId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        System.out.println("secondPage");
        for (ArticleResponse articleResponse : articles2) {
            System.out.println("articleResponse.getArticleId() = " + articleResponse.getArticleId());
        }
    }

    @Test
    void countTest(){
        ArticleResponse response = create(new ArticleCreateRequest("h1", "content", 1L, 2L));

        Long count1 = restClient.get()
                .uri("/v1/articles/boards/{boardId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count1 = " + count1);

        restClient.delete() // HTTP DELETE 메서드 사용하겠다 선언
                .uri("/v1/articles/{articleId}", response.getArticleId())
                .retrieve();

        Long count2 = restClient.get()
                .uri("/v1/articles/boards/{boardId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count2 = " + count2);
    }

    // 테스트 코드 내에서만 데이터를 실어 보낼 목적으로 만든 임시 객체
    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest{
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest{
        private String title;
        private String content;
    }
}
