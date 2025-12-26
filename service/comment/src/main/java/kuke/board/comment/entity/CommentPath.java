package kuke.board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable // CommentV2 entity 내부에 포함되어 데이터베이싀 컬럼으로 들어가게 됨
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {
    private String path;
    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEPTH_CHUNK_SIZE = 5; // 한 Depth 당 5글자 ex) 원댓글(00000) -> 답글 (0000000000)
    private static final int MAX_DEPTH = 5; // 원댓글 포함 최대 5개까지의 답글
    // MIN_CHUNK = "00000", MAX_CHUNK = "zzzzz"
    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE);

    public static CommentPath create(String path){ // CommentPath 객체를 생성하는 정적 팩토리 메서드
        if(isDepthOverflowed(path)){
            throw new IllegalStateException("depth overflowed"); // Depth 초과시 예외
        }
        CommentPath commentPath = new CommentPath();
        commentPath.path = path;
        return commentPath;
    }

    public static boolean isDepthOverflowed(String path){
        return calDepth(path) > MAX_DEPTH;
    }

    public static int calDepth(String path){
        return path.length() / DEPTH_CHUNK_SIZE; // 경로 문자열의 길이를 5로 나눠 현재 depth 계산 ex) 길이 10 -> depth 2
    }

    public int getDepth(){
        return calDepth(path);
    }

    public boolean isRoot(){
        return calDepth(path) == 1; // 원댓글인지 확인
    }

    public String getParentPath(){
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE); // 자신의 경로에서 뒤 5글자를 잘라내면 부모의 경로가 됨 ex) 0000A00001 -> 부모: 0000A
    }

    public CommentPath createChildCommentPath(String descendantsTopPath){ // 자식 댓글 경로 생성 / descendantsTopPath : 현재 부모 밑에 달려있는 댓글들 중 가장 마지막 댓글
        if(descendantsTopPath == null){ // 자식이 하나도 없을 때
            return CommentPath.create(path + MIN_CHUNK); // 부모 경로 뒤에 MIN_CHUNK(00000) 을 붙여서 첫 번째 자식을 만듦
        }
        // 자식이 있을 때
        String childrenTopPath = findChildrenTopPath(descendantsTopPath); // 전달받은 자손 경로에서 내 바로 아래 단계의 경로까지만 잘라냄
        return CommentPath.create(increase(childrenTopPath)); // 자식 경로 값을 1 증가시킴   ex) 기존 마지막 자식이 0000A00000 이면 새 자식 0000A00001
    }

    private String findChildrenTopPath(String descendantsTopPath){
        return descendantsTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    private String increase(String path){
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
        if(isChunkOverflowed(lastChunk)){
            throw new IllegalStateException("chunk overflowed"); // 오버플로 체크
        }

        int charsetLength = CHARSET.length();

        int value = 0;
        for (char ch : lastChunk.toCharArray()){
            value = value * charsetLength + CHARSET.indexOf(ch);
        }

        value = value + 1; // 값 1 증가

        // 다시 문자열로 변환
        String result = "";
        for (int i = 0; i < DEPTH_CHUNK_SIZE; i++){
            result = CHARSET.charAt(value % charsetLength) + result;
            value /= charsetLength;
        }

        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    private boolean isChunkOverflowed(String lastChunk){
        return MAX_CHUNK.equals(lastChunk);
    }
}
