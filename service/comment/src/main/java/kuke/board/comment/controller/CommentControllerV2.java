package kuke.board.comment.controller;

import kuke.board.comment.service.CommentService;
import kuke.board.comment.service.CommentServiceV2;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentPageResponseV2;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.comment.service.response.CommentResponseV2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // // 이 클래스가 웹 페이지(View) 를 반환하는 것이 아니라 JSON을 응답하는 API Controller 임을 명시
@RequiredArgsConstructor
public class CommentControllerV2 { 
    private final CommentServiceV2 commentServiceV2; // Service 객체 주입

    // 댓글 단건 조회
    @GetMapping("/v2/comments/{commentId}")
    public CommentResponseV2 read(
            @PathVariable("commentId") Long commentId
    ){
        return commentServiceV2.read(commentId);
    }

    //--------------------------------------------------------------------------------------
    // 댓글 생성
    @PostMapping("/v2/comments")
    public CommentResponseV2 create(@RequestBody CommentCreateRequestV2 request){
        return commentServiceV2.create(request);
    }

    //--------------------------------------------------------------------------------------
    // 댓글 삭제
    @DeleteMapping("/v2/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId){
        commentServiceV2.delete(commentId);
    }

    //--------------------------------------------------------------------------------------

    @GetMapping("/v2/comments")
    public CommentPageResponseV2 readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ){
        return commentServiceV2.readAll(articleId, page, pageSize);
    }

    @GetMapping("/v2/comments/infinite-scroll")
    public List<CommentResponseV2> readAllInfiniteScroll(
            @RequestParam("articleId") Long articleId,
            @RequestParam(value = "lastPath", required = false) String lastPath, // 마지막으로 조회된 댓글의 경로
            @RequestParam("pageSize") Long pageSize
    ){
        return commentServiceV2.readAllInfiniteScroll(articleId, lastPath, pageSize);
    }

    //--------------------------------------------------------------------------------------
    @GetMapping("/v2/comments/articles/{articleId}/count")
    public Long count(
            @PathVariable("articleId") Long articleId
    ){
        return commentServiceV2.count(articleId);
    }
}
