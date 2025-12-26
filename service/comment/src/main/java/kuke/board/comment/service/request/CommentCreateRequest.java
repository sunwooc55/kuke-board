package kuke.board.comment.service.request;

import lombok.Getter;

// '댓글 작성 요청서' 역할을 하는 DTO
@Getter
public class CommentCreateRequest {
    private Long articleId;
    private String content;
    private Long parentCommentId;
    private Long writerId;
}
