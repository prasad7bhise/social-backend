package com.example.social.app.business.mapper;

import com.example.social.app.business.dto.feed.*;
import com.example.social.app.db.entity.post.CommentsEntity;
import com.example.social.app.db.entity.post.PostEntity;
import com.example.social.app.db.entity.post.PostMediaEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FeedMapper {

    UserBriefDTO toUserBrief(UsersEntity user);

    MediaDTO toMediaDTO(PostMediaEntity media);

    List<MediaDTO> toMediaDTOList(List<PostMediaEntity> media);

    default PostDTO toPostDTO(PostEntity post, UserBriefDTO userBrief, List<MediaDTO> media,
                              long likeCount, long commentCount, boolean likedByMe,
                              List<CommentDTO> recentComments) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setUser(userBrief);
        dto.setMedia(media);
        dto.setLikeCount(likeCount);
        dto.setCommentCount(commentCount);
        dto.setLikedByMe(likedByMe);
        dto.setRecentComments(recentComments);
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        return dto;
    }

    default CommentDTO toCommentDTO(CommentsEntity comment, UserBriefDTO userBrief) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setUser(userBrief);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setEditable(comment.getCreatedAt().plusMinutes(15).isAfter(LocalDateTime.now()));
        return dto;
    }
}
