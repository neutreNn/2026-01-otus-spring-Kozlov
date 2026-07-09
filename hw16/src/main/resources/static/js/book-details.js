(() => {
    const page = document.querySelector("[data-book-details]");
    const bookId = page.dataset.bookId;
    const statusElement = document.querySelector("[data-book-status]");
    const titleElement = document.querySelector("[data-book-title]");
    const authorElement = document.querySelector("[data-book-author]");
    const genreElement = document.querySelector("[data-book-genre]");
    const commentsList = document.querySelector("[data-comments-list]");
    const commentsEmpty = document.querySelector("[data-comments-empty]");
    const deleteButton = document.querySelector("[data-delete-book]");

    const setVisible = (element, visible) => {
        element.classList.toggle("is-hidden", !visible);
    };

    const setStatus = (message, isError = false) => {
        statusElement.textContent = message;
        statusElement.classList.toggle("status-message--error", isError);
        setVisible(statusElement, true);
    };

    const hideStatus = () => {
        setVisible(statusElement, false);
    };

    const requestJson = async (url) => {
        const response = await fetch(url, {
            headers: {
                "Accept": "application/json"
            }
        });
        if (!response.ok) {
            throw new Error(`Request failed with status ${response.status}`);
        }
        return response.json();
    };

    const renderBook = (book) => {
        document.title = book.title;
        titleElement.textContent = book.title;
        authorElement.textContent = book.authorFullName;
        genreElement.textContent = book.genreName;
    };

    const renderComments = (comments) => {
        commentsList.replaceChildren();
        comments.forEach((comment) => {
            const item = document.createElement("li");
            const id = document.createElement("span");
            const text = document.createElement("span");
            id.className = "comment-list__id";
            id.textContent = `#${comment.id}`;
            text.textContent = comment.text;
            item.append(id, text);
            commentsList.append(item);
        });
        setVisible(commentsEmpty, comments.length === 0);
    };

    const loadBookDetails = async () => {
        setStatus("Загрузка книги...");
        try {
            const book = await requestJson(`/api/books/${bookId}`);
            const comments = await requestJson(`/api/books/${bookId}/comments`);
            renderBook(book);
            renderComments(comments);
            hideStatus();
        } catch (error) {
            setStatus("Не удалось загрузить книгу.", true);
        }
    };

    const deleteBook = async () => {
        if (!confirm("Удалить книгу?")) {
            return;
        }

        setStatus("Удаление книги...");
        try {
            const response = await fetch(`/api/books/${bookId}`, {
                method: "DELETE"
            });
            if (!response.ok) {
                throw new Error(`Request failed with status ${response.status}`);
            }
            window.location.href = "/books";
        } catch (error) {
            setStatus("Не удалось удалить книгу.", true);
        }
    };

    deleteButton.addEventListener("click", deleteBook);
    document.addEventListener("DOMContentLoaded", loadBookDetails);
})();
