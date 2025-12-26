package kuke.board.comment.controller;

import kuke.board.comment.service.CommentService;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 이 클래스가 웹 페이지(View) 를 반환하는 것이 아니라 JSON을 응답하는 API Controller 임을 명시
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService; // Service 객체 주입

    // 댓글 단건 조회
    @GetMapping("/v1/comments/{commentId}")
    public CommentResponse read(
            @PathVariable("commentId") Long commentId
    ){
        return commentService.read(commentId);
    }

    //--------------------------------------------------------------------------------------
    // 댓글 생성
    @PostMapping("/v1/comments")
    public CommentResponse create(@RequestBody CommentCreateRequest request){
        return commentService.create(request);
    }

    //--------------------------------------------------------------------------------------
    // 댓글 삭제
    @DeleteMapping("/v1/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId){
        commentService.delete(commentId);
    }

    //--------------------------------------------------------------------------------------
    // 페이지 기반 댓글 목록 조회
    @GetMapping("/v1/comments")
    public CommentPageResponse readAll( // 댓글 데이터 리스트 뿐만 아니라 전체 댓글 수 등의 메타 데이터를 포함하여 반환
            @RequestParam("articleId") Long articleId, // 댓글은 독립적으로 존재하지 않고 반드시 게시글에 종속됨
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ){
        return commentService.readAll(articleId, page, pageSize);
    }

    // 무한 스크롤 기반 댓글 목록 조회
    @GetMapping("/v1/comments/infinite-scroll")
    public List<CommentResponse> readAll(
            @RequestParam("articleId") Long articleId,
            // 무한 스크롤 시 lastParentCommendId와 lastCommentId 두 가지 기준점 필요
            // 첫 페이지 로딩할 땐 기준점이 필요 없으므로 required = false
            @RequestParam(value = "lastParentCommentId", required = false) Long lastParentCommentId,
            @RequestParam(value = "lastCommentId", required = false) Long lastCommentId,
            @RequestParam("pageSize") Long pageSize
    ){
        return commentService.readAll(articleId, lastParentCommentId, lastCommentId, pageSize);
    }
}
