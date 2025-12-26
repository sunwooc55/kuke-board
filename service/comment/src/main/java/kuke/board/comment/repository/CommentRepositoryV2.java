package kuke.board.comment.repository;

import kuke.board.comment.entity.CommentV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepositoryV2 extends JpaRepository<CommentV2, Long> {

    // 경로로 단일 조회
    @Query("select c from CommentV2 c where c.commentPath.path = :path") //JPQL : 테이블이 아닌 엔티티를 대상으로 조회
    Optional<CommentV2> findByPath(@Param("path") String path);

    // 가장 마지막 자식 찾기
    @Query(
            value = "select path from comment_v2 " +
                    "where article_id = :articleId and path > :pathPrefix and path like :pathPrefix% " + // pathPrefix로 시작하는 모든 자식을 찾지만 부모 본인은 제외
                    "order by path desc limit 1", // 자식들 중 경로 문자열이 가장 큰 (마지막인) 댓글 하나만 가져옴
            nativeQuery = true
    )
    Optional<String> findDescendantsTopPath(
            @Param("articleId") Long articleId,
            @Param("pathPrefix") String pathPrefix
    );

    // 전체 조회 - Covering index
    @Query(
            value = "select comment_v2.comment_id, comment_v2.content, comment_v2.path, comment_v2.article_id, " +
                    "comment_v2.writer_id, comment_v2.deleted, comment_v2.created_at " +
                    "from (" + // select comment_id 만 조회
                    "   select comment_id from comment_v2 where article_id = :articleId " +
                    "   order by path asc " +
                    "   limit :limit offset :offset" +
                    ") t left join comment_v2 on t.comment_id = comment_v2.comment_id", // 서브 쿼리에서 가져온 소수의 Id를 가지고 실제 테이블과 조인하여 나머지 컬럼들까지 가져옴
            nativeQuery = true
    )
    List<CommentV2> findAll(
            @Param("articleId") Long articleId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    // 개수 조회
    @Query(
            value = "select count(*) from (" +
                    "   select comment_id from comment_v2 where article_id = :articleId limit :limit " +
                    ") t",
            nativeQuery = true
    )
    Long count(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    // 무한 스크롤
    // 첫 번째 페이지 조회
    @Query(
            value = "select comment_v2.comment_id, comment_v2.content, comment_v2.path, comment_v2.article_id, " +
                    "comment_v2.writer_id, comment_v2.deleted, comment_v2.created_at " +
                    "from comment_v2 " +
                    "where article_id = :articleId " +
                    "order by path asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<CommentV2> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    // 두 번째 페이지 조회
    @Query(
            value = "select comment_v2.comment_id, comment_v2.content, comment_v2.path, comment_v2.article_id, " +
                    "comment_v2.writer_id, comment_v2.deleted, comment_v2.created_at " +
                    "from comment_v2 " +
                    "where article_id = :articleId and path > :lastPath " +
                    "order by path asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<CommentV2> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("lastPath") String lastPath,
            @Param("limit") Long limit
    );
}
