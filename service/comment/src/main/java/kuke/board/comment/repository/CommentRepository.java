package kuke.board.comment.repository;

import kuke.board.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// 댓글 데이터를 데이터베이스에서 효율적으로 가져오기 위한 SQL 저장소
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 해당 댓글을 부모로 가지는 댓글의 개수 count
    @Query(
        value = "select count(*) from (" +
                "   select comment_id from comment " +
                "   where article_id = :articleId and parent_comment_id = :parentCommentId " +
                "   limit :limit" +
                ") t",
        nativeQuery = true
    )
    Long countBy(
            @Param("articleId") Long articleId,
            @Param("parentCommentId") Long parentCommentId,
            @Param("limit") Long limit
    );

    // 페이지 기반 목록 조회 (Covering index + 계층 정렬)
    @Query(
            value = "select comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, " +
                    "comment.writer_id, comment.deleted, comment.created_at " +
                    "from (" +
                    "   select comment_id from comment where article_id = :articleId " +
                    "   order by parent_comment_id asc, comment_id asc " + // 원댓글과 답글을 묶어서 정렬하기 위함
                    "   limit :limit offset :offset " +
                    ") t left join comment on t.comment_id = comment.comment_id",
            nativeQuery = true
    )
    List<Comment> findAll(
            @Param("articleId") Long articleId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );


    // 전체 댓글 수 조회
    @Query(
            value = "select count(*) from (" +
                    "   select comment_id from comment where article_id = :articleId limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long count(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    // 무한 스크롤 - 첫 페이지
    @Query(
            value = "select comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, " +
                    "comment.writer_id, comment.deleted, comment.created_at " +
                    "from comment " +
                    "where article_id = :articleId " +
                    "order by parent_comment_id asc, comment_id asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    // 무한 스크롤 - 다음 페이지
    @Query(
            value = "select comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, " +
                    "comment.writer_id, comment.deleted, comment.created_at " +
                    "from comment " +
                    "where article_id = :articleId and (" +
                    "   parent_comment_id > :lastParentCommentId or " + // 마지막으로 본 댓글 다음 댓글을 보기 위함
                    "   (parent_comment_id = :lastParentCommentId and comment_id > :lastCommentId)" + // 아직 대댓글이 끝나지 않았으므로 다음 대댓글을 보기 위함
                    ") " +
                    "order by parent_comment_id asc, comment_id asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("lastParentCommentId") Long lastParentCommentId,
            @Param("lastCommentId") Long lastCommentId,
            @Param("limit") Long limit
    );
}
