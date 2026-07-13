package ru.otus.hw.models.mongo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "book_comments")
public class BookCommentDocument {
    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    private Long id;

    @ToString.Include
    private String text;

    @Field(name = "book_id")
    private Long bookId;
}
