package kuke.board.comment.service.request;

import lombok.Getter;

// '댓글 작성 요청서' 역할을 하는 DTO
@Getter
public class CommentCreateRequestV2 {
    private Long articleId;
    private String content;
    private String parentPath;
    private Long writerId;
}
