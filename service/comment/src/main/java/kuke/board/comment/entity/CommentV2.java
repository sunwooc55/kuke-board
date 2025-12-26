package kuke.board.comment.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
// 단순히 데이터를 담는 그릇(DTO)와는 다르게, DB의 영속성을 담당하며 comment_v2 테이블과 1:1 로 매핑되는 데이터 객체
@Table(name = "comment_v2") // DB의 comment_v2 라는 테이블과 연결
@Getter
@ToString
@Entity // 이 클래스가 DB의 테이블과 매핑되는 객체임을 명시
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 프록시 객체를 생성하기 위해 매개변수 없는 기본 생성자가 반드시 필요. 이 기본 생성자를 public으로 열어두면 개발자가 불완전한 객체를 무분별하게 생성할 위험이 있으므로 protected 로 제한
public class CommentV2 {
    @Id // commentID를 테이블의 Primary Key로 지정
    private Long commentId;
    private String content;
    private Long articleId; // shard key
    private Long writerId;
    @Embedded // CommentPath 안에 있는 path 필드가 테이블의 컬럼으로 들어감
    private CommentPath commentPath;
    private Boolean deleted;
    private LocalDateTime createdAt;

    public static CommentV2 create(Long commentId, String content, Long articleId, Long writerId, CommentPath commentPath){
        CommentV2 comment = new CommentV2();
        comment.commentId = commentId;
        comment.content = content;
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.commentPath = commentPath;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();

        return comment;
    }

    public boolean isRoot(){
        return commentPath.isRoot();
    }

    public void delete(){
        deleted = true;
    }
}
