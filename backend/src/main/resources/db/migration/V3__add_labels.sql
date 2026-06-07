CREATE TABLE labels (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(7) NOT NULL,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE task_labels (
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    label_id UUID NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, label_id)
);

CREATE INDEX idx_labels_project_id ON labels(project_id);
CREATE INDEX idx_task_labels_task_id ON task_labels(task_id);
