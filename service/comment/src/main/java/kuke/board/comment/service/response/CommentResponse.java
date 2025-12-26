package kuke.board.comment.service.response;

import kuke.board.comment.entity.Comment;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

// 서비스 계층에서 처리가 완료된 댓글 데이터를 클라이언트(프론트엔드)에게 보내주기 위해 포장하는 '응답용 DTO'
@Getter
@ToString
public class CommentResponse {
    private Long commentId;
    private String content;
    private Long parentCommentId;
    private Long articleId;
    private Long writerId;
    private Boolean deleted;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment){
        CommentResponse response = new CommentResponse();
        response.commentId = comment.getCommentId();
        response.content = comment.getContent();
        response.parentCommentId = comment.getParentCommentId();
        response.articleId = comment.getArticleId();
        response.writerId = comment.getWriterId();
        response.deleted = comment.getDeleted();
        response.createdAt = comment.getCreatedAt();

        return response;
    }
}
