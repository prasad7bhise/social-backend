package com.example.social.app.db.entity.post;

import com.example.social.app.enums.MediaType;
import com.example.social.app.enums.converters.MediaTypeConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_media")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostMediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MediaTypeConverter.class)
    @Column(nullable = false)
    private MediaType type;

    @Column(nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_post", nullable = false)
    private PostEntity post;
}
