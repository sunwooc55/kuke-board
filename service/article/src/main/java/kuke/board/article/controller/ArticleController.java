package kuke.board.article.controller;

import kuke.board.article.service.ArticleService;
import kuke.board.article.service.request.ArticleCreateRequest;
import kuke.board.article.service.request.ArticleUpdateRequest;
import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Controller에 ResponseBody가 합쳐진 것. 메서드가 반환하는 객체를 자동으로 JSON 형식으로 반환하여 HTTP 응답 본문에 넣어줌
@RequiredArgsConstructor // final 이 붙은 필드를 매개변수로 받는 생성자를 자동으로 만들어줌 (ArticleService)
// 사용자의 요청을 받아 Service에 전달하고 처리된 결과를 다시 사용자에게 Response
public class ArticleController {
    private final ArticleService articleService; // Controller는 요청을 받을 뿐, 실제 업무는 Service가 담당

    @GetMapping("/v1/articles/{articleId}")
    // {articleId} 에 있는 값을 추출하여 Long articleId 변수에 넣어줌
    public ArticleResponse read(@PathVariable Long articleId){
        return articleService.read(articleId); // Service의 read 메서드를 호출하여 데이터를 가져온 뒤, 그 결과인 ArticleResponse 객체를 반환
    }

    // 패이지 기반 목록 조회
    @GetMapping("/v1/articles")
    public ArticlePageResponse readAll(
            @RequestParam("boardId") Long boardId, // @RequestParam("") : URL 뒤에 붙는 ?key=value 형태의 데이터를 받아서 변수에 넣어줌
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize

    ){
        // 서비스 계층에 boardId, page, pageSize를 넘기고 ArticlePageResponse를 받아 클라이언트에 반환
        return articleService.readAll(boardId, page, pageSize);
    }

    // 무한 스크롤 기반 목록 조회
    @GetMapping("/v1/articles/infinite-scroll")
    public List<ArticleResponse> readAllInfiniteScroll(
            @RequestParam("boardId") Long boardId,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastArticleId", required = false) Long lastArticleId // 무한 스크롤은 lastArticleId 다음의 데이터를 가져와야 하는데 첫 요청 시엔 없으므로 false 설정
    ){
        return articleService.readAllInfiniteScroll(boardId, pageSize, lastArticleId);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    @PostMapping("/v1/articles")
    // 사용자가 보낸 JSON 데이터를 ArticleCreateRequest 객체로 변환하여 매개변수에 넣어줌
    public ArticleResponse create(@RequestBody ArticleCreateRequest request){
        return articleService.create(request);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    @PutMapping("/v1/articles/{articleId}")
    // 어떤 글을 (@PathVariable Long articleID) 어떤 내용으로 (@RequestBody ArticleUpdateRequest request) 고칠지 두 가지 정보가 필요
    public ArticleResponse update(
            @PathVariable Long articleId, // URL 에서 수정할 대상을 추출
            @RequestBody ArticleUpdateRequest request // 수정할 내용을 담은 JSON을 자바 객체로 변환
    ){
        return articleService.update(articleId, request);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    @DeleteMapping("/v1/articles/{articleId}")
    public void delete(@PathVariable Long articleId){
        articleService.delete(articleId);
    }
}
