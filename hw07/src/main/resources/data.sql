insert into authors(id, full_name)
values (1, 'Author_1'), (2, 'Author_2'), (3, 'Author_3');

insert into genres(id, name)
values (1, 'Genre_1'), (2, 'Genre_2'), (3, 'Genre_3');

insert into books(id, title, author_id, genre_id)
values (1, 'BookTitle_1', 1, 1), (2, 'BookTitle_2', 2, 2), (3, 'BookTitle_3', 3, 3);

insert into book_comments(id, text, book_id)
values (1, 'Comment_1', 1), (2, 'Comment_2', 1), (3, 'Comment_3', 2);

alter table authors alter column id restart with 4;
alter table genres alter column id restart with 4;
alter table books alter column id restart with 4;
alter table book_comments alter column id restart with 4;
