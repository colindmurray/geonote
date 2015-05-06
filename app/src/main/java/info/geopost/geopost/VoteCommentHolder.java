package info.geopost.geopost;

/**
 * Created by colin on 5/5/15.
 */
public class VoteCommentHolder {
    public static final String VOTE_STATUS = "VoteStatus";
    public static final String COMMENT_NUM = "CommentsNum";

    private int commentNumber = 0;
    private int voteStatus = 0;

    public int getCommentNumber() {
        return commentNumber;
    }
    public void setCommentNumber(int commentNumber) {
        this.commentNumber = commentNumber;
    }

    public int getVoteStatus() {
        return voteStatus;
    }

    public void setVoteStatus(int voteStatus) {
        this.voteStatus = voteStatus;
    }
}
