insert into authors(id, full_name)
values (1, 'Author_1'), (2, 'Author_2'), (3, 'Author_3');

insert into genres(id, name)
values (1, 'Genre_1'), (2, 'Genre_2'), (3, 'Genre_3');

insert into app_users(id, username, password, enabled, managed_genre_id)
values (1, 'reader', '{bcrypt}$2y$10$eYSfdNpih6cQlJgI2zrM4OoeNnnPx0ZvKsWwWhe.Omxfj/OCqHZay', true, null),
       (2, 'editor', '{bcrypt}$2y$10$eYSfdNpih6cQlJgI2zrM4OoeNnnPx0ZvKsWwWhe.Omxfj/OCqHZay', true, 1),
       (3, 'admin', '{bcrypt}$2y$10$eYSfdNpih6cQlJgI2zrM4OoeNnnPx0ZvKsWwWhe.Omxfj/OCqHZay', true, null);

insert into user_roles(user_id, role)
values (1, 'USER'),
       (2, 'USER'),
       (2, 'EDITOR'),
       (3, 'USER'),
       (3, 'EDITOR'),
       (3, 'ADMIN');

insert into books(id, title, author_id, genre_id)
values (1, 'BookTitle_1', 1, 1), (2, 'BookTitle_2', 2, 2), (3, 'BookTitle_3', 3, 3);

insert into book_comments(id, text, book_id)
values (1, 'Comment_1', 1), (2, 'Comment_2', 1), (3, 'Comment_3', 2);

alter table authors alter column id restart with 4;
alter table genres alter column id restart with 4;
alter table app_users alter column id restart with 4;
alter table books alter column id restart with 4;
alter table book_comments alter column id restart with 4;
