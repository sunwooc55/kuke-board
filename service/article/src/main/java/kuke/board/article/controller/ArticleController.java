package kuke.board.article.controller;

import kuke.board.article.service.ArticleService;
import kuke.board.article.service.request.ArticleCreateRequest;
import kuke.board.article.service.request.ArticleUpdateRequest;
import kuke.board.article.service.response.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController // Controller에 ResponseBody가 합쳐진 것. 메서드가 반환하는 객체를 자동으로 JSON 형식으로 반환하여 HTTP 응답 본문에 넣어줌
@RequiredArgsConstructor
// 사용자의 요청을 받아 Service에 전달하고 처리된 결과를 다시 사용자에게 Response
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping("/v1/articles")
    // 사용자가 보낸 JSON 데이터를 ArticleCreateRequest 객체로 변환하여 매개변수에 넣어줌
    public ArticleResponse create(@RequestBody ArticleCreateRequest request){
        return articleService.create(request);
    }

    @GetMapping("/v1/articles/{articleId}")
    // {articleId} 에 있는 값을 추출하여 Long articleId 변수에 넣어줌
    public ArticleResponse read(@PathVariable Long articleId){
        return articleService.read(articleId);
    }

    @PutMapping("/v1/articles/{articleId}")
    // 어떤 글을 (@PathVariable Long articleID) 어떤 내용으로 (@RequestBody ArticleUpdateRequest request) 고칠지 두 가지 정보가 필요
    public ArticleResponse update(@PathVariable Long articleId, @RequestBody ArticleUpdateRequest request){
        return articleService.update(articleId, request);
    }

    @DeleteMapping("/v1/articles/{articleId}")
    public void delete(@PathVariable Long articleId){
        articleService.delete(articleId);
    }
}
