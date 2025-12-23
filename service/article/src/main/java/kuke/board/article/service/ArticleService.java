package kuke.board.article.service;

import kuke.board.article.entity.Article;
import kuke.board.article.repository.ArticleRepository;
import kuke.board.article.service.request.ArticleCreateRequest;
import kuke.board.article.service.request.ArticleUpdateRequest;
import kuke.board.article.service.response.ArticleResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // 서비스 계층임을 명시하고, 스프링 컨테이너로 하여금 스프링 빈(싱글톤)으로 등록하여 관리하게 함
@RequiredArgsConstructor // final이 붙은 필드를 매개변수로 받는 생성자를 자동으로 만들어줌
public class ArticleService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository articleRepository; // DB에 접근하는 객체

    //CREATE
    @Transactional // 메서드 내의 작업이 하나의 트랜잭션으로 묶이도록 함 (모 아니면 도)
    public ArticleResponse create(ArticleCreateRequest request){
        Article article = articleRepository.save(
                // Entity에 정의해 둔 정적 팩토리 메서드를 사용해 객체 생성
                Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );
        return ArticleResponse.from(article); // Entity를 그대로 리턴하는 것이 아닌 DTO로 변환하여 반환
    }

    //READ
    //단순히 DB에서 꺼내서 DTO로 변경
    public ArticleResponse read(Long articleId){
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    //UPDATE
    //Transactional 안에서 조회한 Entity의 값을 바꾸면 메서드가 끝날 때 JPA가 값의 변경을 감지(Dirty Checking)하고 자동으로 Update 쿼리를 날려주기 때문에 save 호출할 필요 X
    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request){
        Article article = articleRepository.findById(articleId).orElseThrow(); // ID로 게시글을 찾되 없으면 예외를 던져서 로직 중단
        article.update(request.getTitle(), request.getContent());
        return ArticleResponse.from(article);
    }

    //DELETE
    @Transactional
    public void delete(Long articleId){
        articleRepository.deleteById(articleId);
    }
}
