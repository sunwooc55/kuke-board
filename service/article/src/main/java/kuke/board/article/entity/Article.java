package kuke.board.article.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// 단순히 데이터를 담는 그릇(DTO)와는 다르게, DB의 영속성을 담당하며 article 테이블과 1:1 로 매핑되는 데이터 객체
@Entity // 이 클래스가 DB의 테이블과 매핑되는 객체임을 명시
@Table(name = "article") // DB의 article 이라는 테이블과 연결
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 프록시 객체를 생성하기 위해 매개변수 없는 기본 생성자가 반드시 필요. 이 기본 생성자를 public으로 열어두면 개발자가 불완전한 객체를 무분별하게 생성할 위험이 있으므로 protected 로 제한
public class Article {
    @Id // articleID를 테이블의 Primary Key로 지정
    private Long articleId;
    private String title;
    private String content;
    private Long boardId; // shard key
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // 정적 팩토리 메서드
    // 캡슐화 : 외부에선 create 메서드만 호출하면 됨.
    // 안전성 : 필수 값(ID, 제목, 내용 등) 을 모두 받아야만 객체가 생성되도록 강제하여 불완전한 객체 생성 제한
    public static Article create(Long articleId, String title, String content, Long boardId, Long writerId){
        Article article = new Article();
        article.articleId = articleId;
        article.title = title;
        article.content = content;
        article.boardId = boardId;
        article.writerId = writerId;
        article.createdAt = LocalDateTime.now();
        article. modifiedAt = article.createdAt;
        return article;
    }

    public void update(String title, String content){
        this.title = title;
        this.content = content;
        modifiedAt = LocalDateTime.now();
    }
}
