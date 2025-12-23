package kuke.board.article.service.response;

import kuke.board.article.entity.Article;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
// DTO / 보여줄 데이터만 추려서 담는 그릇에 비유
public class ArticleResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    //Entity(Article) 를 받아서 응답 객체(ArticleResponse로 변환)
    public static ArticleResponse from(Article article){
        ArticleResponse response = new ArticleResponse();
        response.articleId = article.getArticleId();
        response.title = article.getTitle();
        response.content = article.getContent();
        response.boardId = article.getBoardId();
        response.writerId = article.getWriterId();
        response.createdAt = article.getCreatedAt();
        response.modifiedAt = article.getModifiedAt();

        return response;
    }
}
