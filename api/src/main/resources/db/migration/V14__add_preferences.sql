ALTER TABLE account
    ADD COLUMN list_page_size INTEGER,
    ADD COLUMN list_sort_by VARCHAR(50),
    ADD COLUMN list_sort_desc BOOLEAN;