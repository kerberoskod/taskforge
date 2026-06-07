package com.taskforge.label.dto;

import java.util.List;
import java.util.UUID;

public class SetTaskLabelsRequest {

    private List<UUID> labelIds;

    public List<UUID> getLabelIds() { return labelIds; }
    public void setLabelIds(List<UUID> labelIds) { this.labelIds = labelIds; }
}
