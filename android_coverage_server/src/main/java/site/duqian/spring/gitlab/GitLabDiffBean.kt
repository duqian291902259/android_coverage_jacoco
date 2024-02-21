package site.duqian.spring.gitlab

import com.google.gson.annotations.SerializedName

/*@NoArgsConstructor
@Data*/
class GitLabDiffBean {
    @SerializedName("commit")
    var commit: CommitDTO? = null

    @SerializedName("commits")
    var commits: List<CommitsDTO>? = null

    @SerializedName("diffs")
    var diffs: List<DiffsDTO>? = null

    @SerializedName("compare_timeout")
    var compareTimeout: Boolean? = null

    @SerializedName("compare_same_ref")
    var compareSameRef: Boolean? = null

    class CommitDTO {
        @SerializedName("id")
        val id: String? = null

        @SerializedName("short_id")
        val shortId: String? = null

        @SerializedName("created_at")
        val createdAt: String? = null

        @SerializedName("parent_ids")
        val parentIds: List<String>? = null

        @SerializedName("title")
        val title: String? = null

        @SerializedName("message")
        val message: String? = null

        @SerializedName("author_name")
        val authorName: String? = null

        @SerializedName("author_email")
        val authorEmail: String? = null

        @SerializedName("authored_date")
        val authoredDate: String? = null

        @SerializedName("committer_name")
        val committerName: String? = null

        @SerializedName("committer_email")
        val committerEmail: String? = null

        @SerializedName("committed_date")
        val committedDate: String? = null
    }

    /* @NoArgsConstructor
    @Data*/
    class CommitsDTO {
        @SerializedName("id")
        val id: String? = null

        @SerializedName("short_id")
        val shortId: String? = null

        @SerializedName("created_at")
        val createdAt: String? = null

        @SerializedName("parent_ids")
        val parentIds: List<String>? = null

        @SerializedName("title")
        val title: String? = null

        @SerializedName("message")
        val message: String? = null

        @SerializedName("author_name")
        val authorName: String? = null

        @SerializedName("author_email")
        val authorEmail: String? = null

        @SerializedName("authored_date")
        val authoredDate: String? = null

        @SerializedName("committer_name")
        val committerName: String? = null

        @SerializedName("committer_email")
        val committerEmail: String? = null

        @SerializedName("committed_date")
        val committedDate: String? = null
    }

    /* @NoArgsConstructor
    @Data*/
    class DiffsDTO {
        @SerializedName("old_path")
        val oldPath: String? = null

        @SerializedName("new_path")
        val newPath: String? = null

        @SerializedName("a_mode")
        val aMode: String? = null

        @SerializedName("b_mode")
        val bMode: String? = null

        @SerializedName("new_file")
        val newFile: Boolean? = null

        @SerializedName("renamed_file")
        val renamedFile: Boolean? = null

        @SerializedName("deleted_file")
        val deletedFile: Boolean? = null

        @SerializedName("diff")
        val diff: String? = null
    }
}