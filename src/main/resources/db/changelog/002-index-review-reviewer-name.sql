--liquibase formatted sql

--changeset flairquery:002-index-review-reviewer_name runInTransaction:false
--comment: No index for review.reviewer_name: every call scans review and keeps ~61 of 3000003 rows (FlairQuery finding missing-index-8ed01c2b). CONCURRENTLY: writes to review are not blocked while the index builds.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_review_reviewer_name ON review (reviewer_name);
--rollback DROP INDEX CONCURRENTLY IF EXISTS idx_review_reviewer_name;
