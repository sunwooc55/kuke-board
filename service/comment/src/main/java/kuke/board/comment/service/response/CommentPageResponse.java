package kuke.board.comment.service.response;

import lombok.Getter;

import java.util.List;

// '페이징 처리된 댓글 목록 결과물' 을 담아서 클라이언트에게 전달하는 최종 응답용 DTO
@Getter
public class CommentPageResponse {
    private List<CommentResponse> comments; // DB에서 조회해온 실제 댓글 데이터들의 리스트 (CommentResponse 상태)
    private Long commentCount; // 해당 게시글에 달린 전체 댓글 개수

    // 리스트와 카운트 값을 넘겨받아 완성된 응답 객체를 만들어 반환하는 공장(Factory) 역할
    public static CommentPageResponse of(List<CommentResponse> comments, Long commentCount){
        CommentPageResponse response = new CommentPageResponse();
        response.comments = comments;
        response.commentCount = commentCount;
        return response;
    }
}
