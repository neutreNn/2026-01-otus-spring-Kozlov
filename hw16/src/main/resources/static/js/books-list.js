(() => {
    const statusElement = document.querySelector("[data-books-status]");
    const tableSection = document.querySelector("[data-books-table]");
    const tableBody = document.querySelector("[data-books-table-body]");
    const emptyState = document.querySelector("[data-books-empty]");

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

    const createCell = (content) => {
        const cell = document.createElement("td");
        if (content instanceof Node) {
            cell.append(content);
        } else {
            cell.textContent = content;
        }
        return cell;
    };

    const createLink = (href, text, className) => {
        const link = document.createElement("a");
        link.href = href;
        link.textContent = text;
        if (className) {
            link.className = className;
        }
        return link;
    };

    const createDeleteButton = (book) => {
        const button = document.createElement("button");
        button.className = "button button--danger";
        button.type = "button";
        button.textContent = "Удалить";
        button.addEventListener("click", () => deleteBook(book));
        return button;
    };

    const createActions = (book) => {
        const actions = document.createElement("div");
        actions.className = "actions";
        actions.append(
                createLink(`/books/${book.id}`, "Открыть", "button button--ghost"),
                createLink(`/books/${book.id}/edit`, "Редактировать", "button button--ghost"),
                createDeleteButton(book)
        );
        return actions;
    };

    const renderBooks = (books) => {
        tableBody.replaceChildren();
        books.forEach((book) => {
            const row = document.createElement("tr");
            row.append(
                    createCell(book.id),
                    createCell(createLink(`/books/${book.id}`, book.title)),
                    createCell(book.authorFullName),
                    createCell(book.genreName),
                    createCell(createActions(book))
            );
            tableBody.append(row);
        });

        setVisible(tableSection, books.length > 0);
        setVisible(emptyState, books.length === 0);
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

    const loadBooks = async () => {
        setStatus("Загрузка книг...");
        setVisible(tableSection, false);
        setVisible(emptyState, false);
        try {
            const books = await requestJson("/api/books");
            renderBooks(books);
            hideStatus();
        } catch (error) {
            setStatus("Не удалось загрузить список книг.", true);
        }
    };

    const deleteBook = async (book) => {
        if (!confirm(`Удалить книгу "${book.title}"?`)) {
            return;
        }

        setStatus("Удаление книги...");
        try {
            const response = await fetch(`/api/books/${book.id}`, {
                method: "DELETE"
            });
            if (!response.ok) {
                throw new Error(`Request failed with status ${response.status}`);
            }
            await loadBooks();
        } catch (error) {
            setStatus("Не удалось удалить книгу.", true);
        }
    };

    document.addEventListener("DOMContentLoaded", loadBooks);
})();
