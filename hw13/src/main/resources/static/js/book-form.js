(() => {
    const page = document.querySelector("[data-book-form-page]");
    const bookId = page.dataset.bookId || null;
    const form = document.querySelector("[data-book-form]");
    const statusElement = document.querySelector("[data-form-status]");
    const formError = document.querySelector("[data-form-error]");
    const submitButton = document.querySelector("[data-submit-button]");
    const formMode = document.querySelector("[data-form-mode]");
    const formTitle = document.querySelector("[data-form-title]");
    const fields = {
        title: form.elements.title,
        authorId: form.elements.authorId,
        genreId: form.elements.genreId
    };
    const fieldErrors = Array.from(document.querySelectorAll("[data-field-error]"))
            .reduce((errors, element) => ({...errors, [element.dataset.fieldError]: element}), {});

    const csrfHeaders = () => {
        const header = document.querySelector("meta[name='_csrf_header']")?.content;
        const token = document.querySelector("meta[name='_csrf']")?.content;
        return header && token ? {[header]: token} : {};
    };

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

    const setFormDisabled = (disabled) => {
        Array.from(form.elements).forEach((element) => {
            element.disabled = disabled;
        });
        submitButton.disabled = disabled;
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

    const appendOption = (select, value, text) => {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = text;
        select.append(option);
    };

    const renderReferenceData = (authors, genres) => {
        authors.forEach((author) => {
            appendOption(fields.authorId, author.id, author.fullName);
        });
        genres.forEach((genre) => {
            appendOption(fields.genreId, genre.id, genre.name);
        });
    };

    const renderBook = (book) => {
        fields.title.value = book.title;
        fields.authorId.value = String(book.authorId);
        fields.genreId.value = String(book.genreId);
        document.title = `Редактирование: ${book.title}`;
        formMode.textContent = "Редактирование";
        formTitle.textContent = `Книга #${book.id}`;
    };

    const clearErrors = () => {
        Object.values(fieldErrors).forEach((element) => {
            element.textContent = "";
            setVisible(element, false);
        });
        formError.textContent = "";
        setVisible(formError, false);
    };

    const showValidationErrors = (errors) => {
        Object.entries(errors).forEach(([field, message]) => {
            const element = fieldErrors[field];
            if (element) {
                element.textContent = message;
                setVisible(element, true);
            }
        });
    };

    const numericValueOrNull = (value) => {
        return value === "" ? null : Number(value);
    };

    const buildPayload = () => ({
        title: fields.title.value,
        authorId: numericValueOrNull(fields.authorId.value),
        genreId: numericValueOrNull(fields.genreId.value)
    });

    const parseJsonSafely = async (response) => {
        try {
            return await response.json();
        } catch (error) {
            return {};
        }
    };

    const initializeForm = async () => {
        setFormDisabled(true);
        setStatus("Загрузка формы...");
        try {
            const [authors, genres] = await Promise.all([
                requestJson("/api/authors"),
                requestJson("/api/genres")
            ]);
            renderReferenceData(authors, genres);
            if (bookId) {
                const book = await requestJson(`/api/books/${bookId}`);
                renderBook(book);
            }
            hideStatus();
            setFormDisabled(false);
        } catch (error) {
            setStatus("Не удалось загрузить форму.", true);
        }
    };

    const saveBook = async (event) => {
        event.preventDefault();
        clearErrors();
        setFormDisabled(true);
        setStatus("Сохранение книги...");

        try {
            const response = await fetch(bookId ? `/api/books/${bookId}` : "/api/books", {
                method: bookId ? "PUT" : "POST",
                headers: {
                    "Accept": "application/json",
                    "Content-Type": "application/json",
                    ...csrfHeaders()
                },
                body: JSON.stringify(buildPayload())
            });
            const responseBody = await parseJsonSafely(response);

            if (response.status === 400 && responseBody.fieldErrors) {
                showValidationErrors(responseBody.fieldErrors);
                setStatus("Проверьте поля формы.", true);
                return;
            }
            if (!response.ok) {
                throw new Error(`Request failed with status ${response.status}`);
            }

            window.location.href = `/books/${responseBody.id}`;
        } catch (error) {
            formError.textContent = "Не удалось сохранить книгу.";
            setVisible(formError, true);
            setStatus("Не удалось сохранить книгу.", true);
        } finally {
            setFormDisabled(false);
        }
    };

    form.addEventListener("submit", saveBook);
    document.addEventListener("DOMContentLoaded", initializeForm);
})();
