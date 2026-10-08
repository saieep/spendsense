-- SpendSense schema bootstrap (Day 1)
CREATE TABLE IF NOT EXISTS schema_marker (
    id INTEGER PRIMARY KEY,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

INSERT OR IGNORE INTO schema_marker (id) VALUES (1);
