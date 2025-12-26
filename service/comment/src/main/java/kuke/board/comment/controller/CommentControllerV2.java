package kuke.board.comment.controller;

import kuke.board.comment.service.CommentService;
import kuke.board.comment.service.CommentServiceV2;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.comment.service.response.CommentResponseV2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentControllerV2 {
    private final CommentServiceV2 commentServiceV2;

    @GetMapping("/v2/comments/{commentId}")
    public CommentResponseV2 read(
            @PathVariable("commentId") Long commentId
    ){
        return commentServiceV2.read(commentId);
    }

    //--------------------------------------------------------------------------------------

    @PostMapping("/v2/comments")
    public CommentResponseV2 create(@RequestBody CommentCreateRequestV2 request){
        return commentServiceV2.create(request);
    }

    //--------------------------------------------------------------------------------------

    @DeleteMapping("/v2/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId){
        commentServiceV2.delete(commentId);
    }

    //--------------------------------------------------------------------------------------
}
