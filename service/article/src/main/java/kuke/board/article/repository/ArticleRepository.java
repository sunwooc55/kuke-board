package kuke.board.article.repository;

import kuke.board.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// DB와 직접 대화하며 실제 데이터를 조회하거나 저장하는 '창구' 역할
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> { // 제네릭 지정으로 Article 엔티티를 다루며, PK 타입은 Long 임을 명시

    // 인덱스만 태워서 페이징 위치를 찾고, 실제 데이터는 나중에 붙이는 "커버링 인덱스" 방식으로 조회 성능 극대화
    // 뒤쪽으로 갈수록 느려지는 문제 해결
    @Query(
            value = "select article.article_id, article.title, article.content, article.board_id, article.writer_id, article.created_at, article.modified_at " +
                    "from (" +
                    "   select article_id from article " +
                    "   where board_id = :boardId " + // :{} : 변수가 들어갈 자리
                    "   order by article_id desc " +
                    "   limit :limit offset :offset " +
                    ") t left join article on t.article_id = article.article_id  ",
            nativeQuery = true // JPA는 서브쿼리를 지원하지 않으므로 직접 SQL 작성한다고 선언
    )
    List<Article> findAll(
            @Param("boardId") Long boardId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    // 필요한 만큼만 세는 "Bounded Count" 방식
    @Query(
            value = "select count(*) from (" +
                    "   select article_id from article where board_id = :boardId limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long count(@Param("boardId") Long boardId, @Param("limit") Long limit);

    // 기준점 없이 무한 스크롤
    @Query(
            value = "select article.article_id, article.title, article.content, article.board_id, article.writer_id, " +
                    "article.created_at, article.modified_at " +
                    "from article " +
                    "where board_id = :boardId " +
                    "order by article_id desc limit :limit",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(@Param("boardId") Long boardId, @Param("limit") Long limit);

    // 기준점을 가지는 무한 스크롤
    @Query(
            value = "select article.article_id, article.title, article.content, article.board_id, article.writer_id, " +
                    "article.created_at, article.modified_at " +
                    "from article " +
                    "where board_id = :boardId and article_id < :lastArticleId " +
                    "order by article_id desc limit :limit",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(@Param("boardId") Long boardId, @Param("limit") Long limit, @Param("lastArticleId") Long lastArticleId);

}
