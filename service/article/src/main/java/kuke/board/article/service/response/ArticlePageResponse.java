package kuke.board.article.service.response;


import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
// 목록 조회(페이징) 결과를 담아서 클라이언트에게 전달하는 최종 포장지 역할을 하는 DTO
public class ArticlePageResponse {

    private List<ArticleResponse> articles; // ArticleRepository.findAll(..)의 결과가 DTO로 변환되어 여기에 담김
    private Long articleCount; // ArticleRepository.count(..)의 결과가 여기에 담김

    // 정적 팩토리 메서드를 사용하여, Service 에서 이 객체를 생성할 때 깔끔하게 생성할 수 있도록 해줌.
    public static ArticlePageResponse of(List<ArticleResponse> articles, Long articleCount){
        ArticlePageResponse response = new ArticlePageResponse();
        response.articles = articles;
        response.articleCount = articleCount;
        return response;
    }
}
