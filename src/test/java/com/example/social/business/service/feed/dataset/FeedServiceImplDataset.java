package com.example.social.business.service.feed.dataset;

import com.example.social.app.business.dto.feed.*;
import com.example.social.app.db.entity.post.CommentsEntity;
import com.example.social.app.db.entity.post.PostEntity;
import com.example.social.app.db.entity.post.PostMediaEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.MediaType;

import java.time.LocalDateTime;
import java.util.List;

public class FeedServiceImplDataset {

    public static UsersEntity currentUser() {
        UsersEntity u = new UsersEntity();
        u.setId(1L);
        u.setKeycloakId("kc-current");
        u.setFirstName("Current");
        u.setLastName("User");
        u.setEmail("current@example.com");
        u.setRole("USER");
        return u;
    }

    public static UsersEntity postAuthor() {
        UsersEntity u = new UsersEntity();
        u.setId(2L);
        u.setKeycloakId("kc-author");
        u.setFirstName("Post");
        u.setLastName("Author");
        u.setEmail("author@example.com");
        u.setRole("USER");
        return u;
    }

    public static PostEntity existingPost() {
        PostEntity p = new PostEntity();
        p.setId(10L);
        p.setContent("Test post content");
        p.setUser(postAuthor());
        p.setCreatedAt(LocalDateTime.now().minusHours(1));
        PostMediaEntity media = new PostMediaEntity();
        media.setId(100L);
        media.setType(MediaType.IMAGE);
        media.setUrl("/uploads/test.jpg");
        media.setPost(p);
        p.setMedia(List.of(media));
        return p;
    }

    public static PostEntity userOwnPost() {
        PostEntity p = new PostEntity();
        p.setId(11L);
        p.setContent("My own post");
        p.setUser(currentUser());
        p.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        return p;
    }

    public static CommentsEntity existingComment() {
        CommentsEntity c = new CommentsEntity();
        c.setId(20L);
        c.setContent("Nice post!");
        c.setPost(existingPost());
        c.setUser(postAuthor());
        c.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        return c;
    }

    public static CommentsEntity userOwnComment() {
        CommentsEntity c = new CommentsEntity();
        c.setId(21L);
        c.setContent("My comment");
        c.setPost(existingPost());
        c.setUser(currentUser());
        c.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        return c;
    }

    public static CommentsEntity oldComment() {
        CommentsEntity c = new CommentsEntity();
        c.setId(22L);
        c.setContent("Old comment");
        c.setPost(existingPost());
        c.setUser(currentUser());
        c.setCreatedAt(LocalDateTime.now().minusHours(2));
        return c;
    }

    public static CreatePostRequest createPostRequest() {
        CreatePostRequest r = new CreatePostRequest();
        r.setContent("New post");
        MediaRequest mr = new MediaRequest();
        mr.setType("IMAGE");
        mr.setUrl("/uploads/new.jpg");
        r.setMedia(List.of(mr));
        return r;
    }

    public static CreatePostRequest createPostRequestWithoutMedia() {
        CreatePostRequest r = new CreatePostRequest();
        r.setContent("Text only post");
        return r;
    }

    public static CreateCommentRequest createCommentRequest() {
        CreateCommentRequest r = new CreateCommentRequest();
        r.setContent("Great post!");
        return r;
    }

    public static UserBriefDTO currentUserBrief() {
        UserBriefDTO d = new UserBriefDTO();
        d.setId(1L);
        d.setFirstName("Current");
        d.setLastName("User");
        d.setAvatarUrl(null);
        return d;
    }

    public static UserBriefDTO authorBrief() {
        UserBriefDTO d = new UserBriefDTO();
        d.setId(2L);
        d.setFirstName("Post");
        d.setLastName("Author");
        d.setAvatarUrl(null);
        return d;
    }
}
